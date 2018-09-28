package org.app.helaHub.config.properties;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@EncryptablePropertySource(value = {"file:${CONFIG_PATH}/hazelcast-cache.properties"})
public class HazelCastCacheProperties {
    @Value("${org.app.helaHub.properties.hazelcast.default.instance.name}")
    private String defaultInstanceName;
    @Value("${org.app.helaHub.properties.hazelcast.default.name}")
    private String defaultName;
    @Value("${org.app.helaHub.properties.hazelcast.default.ttl}")
    private Integer defaultTtl;
    @Value("${org.app.helaHub.properties.hazelcast.posts.name}")
    private String postsName;
    @Value("${org.app.helaHub.properties.hazelcast.posts.ttl}")
    private Integer postsTtl;
    @Value("${org.app.helaHub.properties.hazelcast.paged.posts.name}")
    private String pagedPostsName;
    @Value("${org.app.helaHub.properties.hazelcast.pagedPosts.cache.ttl}")
    private Integer pagedPostsCacheTtl;
    @Value("${org.app.helaHub.properties.hazelcast.site.statistics.name}")
    private String siteStatisticsName;
    @Value("${org.app.helaHub.properties.hazelcast.site.statistics.ttl}")
    private Integer siteStatisticsTtl;
    @Value("${org.app.helaHub.properties.hazelcast.default.backup.count}")
    private Integer defaultBackupCount;
    @Value("${org.app.helaHub.properties.hazelcast.posts.backup.count}")
    private Integer postsBackupCount;
    @Value("${org.app.helaHub.properties.hazelcast.paged.posts.backup.count}")
    private Integer pagedPostsBackupCount;
    @Value("${org.app.helaHub.properties.hazelcast.site.statistics.backup.count}")
    private Integer siteStatisticsBackupCount;
    @Value("${org.app.helaHub.properties.hazelcast.default.size.max}")
    private Integer defaultSizeMax;
    @Value("${org.app.helaHub.properties.hazelcast.posts.ttl.size.max}")
    private Integer postsTtlSizeMax;
    @Value("${org.app.helaHub.properties.hazelcast.paged.posts.size.max}")
    private Integer pagedPostsSizeMax;
    @Value("${org.app.helaHub.properties.hazelcast.posts.size.max}")
    private Integer postsSizeMax;
    @Value("${org.app.helaHub.properties.hazelcast.site.statistics.size.max}")
    private Integer siteStatisticsSizeMax;
    @Value("${org.app.helaHub.properties.hazelcast.near.cache.max.size}")
    private Integer nearCacheMaxSize;
    @Value("${org.app.helaHub.properties.hazelcast.nearCache.max.idle.seconds}")
    private Integer nearCacheMaxIdleSeconds;
    @Value("${org.app.helaHub.properties.hazelcast.near.cache.ttl}")
    private Integer nearCacheTtl;
    @Value("${org.app.helaHub.properties.hazelcast.logging.type}")
    private String loggingType;
    @Value("${org.app.helaHub.properties.hazelcast.tcp.ip.timeout}")
    private String tcpIpTimeout;
    @Value("${org.app.helaHub.properties.hazelcast.tcp.ip.enabled}")
    private boolean tcpIpEnabled;
    @Value("${org.app.helaHub.properties.hazelcast.phone.home.enabled}")
    private String phoneHomeEnabled;
    @Value("${org.app.helaHub.properties.hazelcast.diagnostics.enabled}")
    private String diagnosticsEnabled;
    @Value("${org.app.helaHub.properties.hazelcast.multicast.enabled}")
    private boolean multicastEnabled;
    @Value("${org.app.helaHub.properties.hazelcast.multicast.trusted.interface.ip}")
    private String multicastTrustedInterfaceIp;
    @Value("${org.app.helaHub.properties.hazelcast.tcpip.members}")
    private String[] tcpipMembers;
    @Value("${org.app.helaHub.properties.hazelcast.tcpip.member.required}")
    private String tcpipMemberRequired;
    @Value("${org.app.helaHub.properties.hazelcast.interfaces.allowed.enabled}")
    private boolean interfacesAllowedEnabled;
    @Value("${org.app.helaHub.properties.hazelcast.interfaces.allowed.interface}")
    private String interfacesAllowedInterface;
    @Value("${org.app.helaHub.properties.hazelcast.interfaces.disallowed.enabled}")
    private String interfacesDisallowedEnabled;
    @Value("${org.app.helaHub.properties.hazelcast.interfaces.disallowed.interface}")
    private String interfacesDisallowedInterface;
    @Value("${org.app.helaHub.properties.hazelcast.hot.restart.persistence.base.dir}")
    private String hotRestartPersistenceBaseDir;
}
