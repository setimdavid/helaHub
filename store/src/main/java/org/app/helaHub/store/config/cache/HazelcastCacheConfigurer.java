package org.app.helaHub.store.config.cache;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.memory.MemorySize;
import com.hazelcast.memory.MemoryUnit;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.hazelcast.topic.TopicOverloadPolicy;
import org.app.helaHub.config.properties.HazelCastCacheProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

import java.io.File;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableHazelcastRepositories
@ConditionalOnProperty(prefix = "using.spring.hazelcast", name = "enabled", havingValue = "true")
public class HazelcastCacheConfigurer {
    private final HazelCastCacheProperties properties;

    @Autowired
    public HazelcastCacheConfigurer(HazelCastCacheProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Config hazelCastConfig() {
        Config config = new Config();
        config.setInstanceName(properties.getDefaultInstanceName());
        config.setProperty("hazelcast.logging.type", properties.getLoggingType());
        config.setProperty("hazelcast.diagnostics.enabled", properties.getDiagnosticsEnabled());
        config.setProperty("hazelcast.phone.home.enabled", properties.getPhoneHomeEnabled());
        config.setProperty("hazelcast.socket.server.bind.any", "false");
        NetworkConfig networkConfig = config.getNetworkConfig();
        InterfacesConfig interfacesConfig = networkConfig.getInterfaces();

        interfacesConfig.setInterfaces(Collections.singletonList(properties.getInterfacesAllowedInterface())).setEnabled(true);


        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig()
                .setLoopbackModeEnabled(false)
                .setEnabled(properties.isMulticastEnabled());

        joinConfig.getTcpIpConfig().setMembers(Arrays.asList(properties.getTcpipMembers())).setEnabled(properties.isTcpIpEnabled());

        MapConfig defaultCache = new MapConfig();
        defaultCache.setName(properties.getDefaultName());
        defaultCache.setTimeToLiveSeconds(properties.getDefaultTtl());

        defaultCache.setEvictionPolicy(EvictionPolicy.LFU);
        defaultCache.setBackupCount(properties.getDefaultBackupCount());
        defaultCache.getMaxSizeConfig().setSize(properties.getDefaultSizeMax());
        NearCacheConfig nearCacheConfig = new NearCacheConfig();

        nearCacheConfig.setMaxIdleSeconds(properties.getNearCacheMaxIdleSeconds()).setTimeToLiveSeconds(properties.getNearCacheTtl());
        defaultCache.setNearCacheConfig(nearCacheConfig);
        config.getMapConfigs().put(properties.getDefaultName(), defaultCache);


        MapConfig ussdCache = new MapConfig();
            ussdCache
                .setName(properties.getPostsName())
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setStatisticsEnabled(true)
                .setTimeToLiveSeconds(properties.getPostsTtl())
                .setEvictionPolicy(EvictionPolicy.LFU)
                .setBackupCount(properties.getPostsBackupCount())
                .setNearCacheConfig(nearCacheConfig)
                .setMergePolicy("com.hazelcast.map.merge.PutIfAbsentMapMergePolicy")
                .getMaxSizeConfig()
                .setSize(properties.getPostsSizeMax())
                .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.USED_HEAP_PERCENTAGE);

        config.getMapConfigs().put(properties.getPostsName(), ussdCache);

        MapConfig pagedPostsCache = new MapConfig();
        pagedPostsCache
            .setName(properties.getPagedPostsName())
            .setInMemoryFormat(InMemoryFormat.BINARY)
            .setStatisticsEnabled(true)
            .setTimeToLiveSeconds(properties.getPagedPostsCacheTtl())
            .setEvictionPolicy(EvictionPolicy.LFU)
            .setBackupCount(properties.getPagedPostsBackupCount())
            .setNearCacheConfig(nearCacheConfig)
            .getMaxSizeConfig()
            .setSize(properties.getPagedPostsSizeMax())
            .setMaxSizePolicy(MaxSizeConfig.MaxSizePolicy.FREE_NATIVE_MEMORY_SIZE);

        config.getMapConfigs().put(properties.getPagedPostsName(), pagedPostsCache);

        QueueConfig qc = new QueueConfig();
        qc
                .setName("default")
                .setMaxSize(properties.getPostsSizeMax())
                .setBackupCount(1)
                .setAsyncBackupCount(0)
                .setEmptyQueueTtl(-1);

        config.getQueueConfigs().put("default", qc);

        MultiMapConfig mmc = new MultiMapConfig();
        mmc
                .setName("default")
                .setBackupCount(1)
                .setAsyncBackupCount(0)
                .setStatisticsEnabled(true)
                .setBinary(true)
                .setValueCollectionType("SET");
        config.getMultiMapConfigs().put("default", mmc);

        ListConfig lc = new ListConfig();
        lc
                .setName("default")
                .setBackupCount(1)
                .setAsyncBackupCount(0)
                .setStatisticsEnabled(true)
                .setMaxSize(properties.getPostsSizeMax());
        config.getListConfigs().put("default", lc);

        SemaphoreConfig sc = new SemaphoreConfig();
        sc
                .setName("default")
                .setInitialPermits(0)
                .setBackupCount(1)
                .setAsyncBackupCount(0);
        Map<String, SemaphoreConfig> map = new HashMap<>();
        map.put("default", sc);
        config.setSemaphoreConfigs(map);

        RingbufferConfig rbc = new RingbufferConfig("default");
        rbc
                .setCapacity(10000)
                .setTimeToLiveSeconds(0)
                .setBackupCount(1)
                .setAsyncBackupCount(0)
                .setInMemoryFormat(InMemoryFormat.BINARY);
        config.getRingbufferConfigs().put("default", rbc);

        ExecutorConfig ec = new ExecutorConfig();
        ec
                .setName("default")
                .setStatisticsEnabled(true)
                .setPoolSize(16)
                .setQueueCapacity(0);

        config.getExecutorConfigs().put("default", ec);

        PartitionGroupConfig partitionGroupConfig = config.getPartitionGroupConfig();
        partitionGroupConfig
                .setEnabled(true)
                .setGroupType(PartitionGroupConfig.MemberGroupType.HOST_AWARE);

        config.setPartitionGroupConfig(partitionGroupConfig);

        SerializationConfig serializationConfig = new SerializationConfig();
        serializationConfig
                .setPortableVersion(0)
                .setUseNativeByteOrder(false)
                .setByteOrder(ByteOrder.BIG_ENDIAN)
                .setEnableCompression(false)
                .setEnableSharedObject(true)
                .setAllowUnsafe(false)
                .setCheckClassDefErrors(true);

        config.setSerializationConfig(serializationConfig);

        ReliableTopicConfig reliableTopicConfig = new ReliableTopicConfig("default");
        reliableTopicConfig
                .setStatisticsEnabled(true)
                .setReadBatchSize(10)
                .setTopicOverloadPolicy(TopicOverloadPolicy.BLOCK);
        config.getReliableTopicConfigs().put("default", reliableTopicConfig);
        config.setLiteMember(false);

        MemorySize ms = new MemorySize(512, MemoryUnit.MEGABYTES);

        NativeMemoryConfig nativeMemoryConfig = config.getNativeMemoryConfig() == null ? new NativeMemoryConfig() : config.getNativeMemoryConfig();
        nativeMemoryConfig
                .setSize(ms)
                .setMinBlockSize(16)
                .setPageSize(4194304)
                .setMetadataSpacePercentage(Float.valueOf("12.5"));

        config.setNativeMemoryConfig(nativeMemoryConfig);
        config.setServicesConfig(new ServicesConfig().setEnableDefaults(true));

        HotRestartPersistenceConfig hotRestartConfig = new HotRestartPersistenceConfig();
        hotRestartConfig
                .setEnabled(true)
                .setBaseDir(new File(properties.getHotRestartPersistenceBaseDir()))
                .setParallelism(1)
                .setValidationTimeoutSeconds(120)
                .setDataLoadTimeoutSeconds(900);
        config.setHotRestartPersistenceConfig(hotRestartConfig);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, config.toString());
        config.setManagementCenterConfig(managementCenterConfig());
        return config;
    }

    public ManagementCenterConfig managementCenterConfig() {
        ManagementCenterConfig manCenterCfg = new ManagementCenterConfig();
        manCenterCfg.setEnabled(true);
        manCenterCfg.setUrl("http://localhost:8080/mancenter");
        manCenterCfg.setUpdateInterval(5);
        return manCenterCfg;
    }
    @Bean
    public CacheManager cacheManager() {
        return new HazelcastCacheManager(hazelcastinstance());
    }

    @Bean
    public HazelcastInstance hazelcastinstance() {
        return Hazelcast.newHazelcastInstance(hazelCastConfig());
    }

    @Bean
    public KeyValueOperations keyValueTemplate() {
        return new KeyValueTemplate(new HazelcastKeyValueAdapter());
    }

    @Bean
    public HazelcastKeyValueAdapter hazelcastKeyValueAdapter() {

        return new HazelcastKeyValueAdapter();

    }
}
