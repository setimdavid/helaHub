package org.app.helaHub.config.properties;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EncryptablePropertySource(value = {"file:${CONFIG_PATH}/persistence-postgres.properties"})
@Getter
public class JpaConnectionProperties {
    @Value("${org.app.helaHub.properties.jdbc.driver.class.name}")
    private String jdbcDriverClassName;
    @Value("${org.app.helaHub.properties.jdbc.url}")
    private String jdbcUrl;
    @Value("${org.app.helaHub.properties.jdbc.user}")
    private String jdbcUser;
    @Value("${org.app.helaHub.properties.jdbc.pass}")
    private String jdbcPass;
    @Value("${org.app.helaHub.properties.dialect}")
    private String dialect;
    @Value("${org.app.helaHub.properties.show.sql}")
    private String showSql;
    @Value("${org.app.helaHub.properties.hbm2ddl.auto}")
    private String hbm2ddlAuto;
    @Value("${org.app.helaHub.properties.pool.connection.customizer.classname}")
    private String poolConnectionCustomizerClassname;
    @Value("${org.app.helaHub.properties.generate.statistics}")
    private String generateStatistics;
    @Value("${org.app.helaHub.properties.spring.component.scan}")
    private String springComponentScan;
    @Value("${org.app.helaHub.properties.spring.enable.jpa.repositories}")
    private String springEnableJpaRepositories;
    @Value("${org.app.helaHub.properties.spring.packages.to.scan}")
    private String springPackagesToScan;
    @Value("${org.app.helaHub.properties.hibernate.id.newgenerator.mappings}")
    private String hibernateIdNewgeneratorMappings;
    @Value("${org.app.helaHub.properties.spring.jpa.hibernate.naming-strategy}")
    private String springJpaHibernateNamingStrategy;
    @Value("${org.app.helaHub.properties.hikari.connection.timeout}")
    private Integer hikariConnectionTimeout;
    @Value("${org.app.helaHub.properties.hikari.idle.timeout}")
    private Integer hikariIdleTimeout;
    @Value("${org.app.helaHub.properties.hikari.max.lifetime}")
    private Integer hikariMaxLifetime;
    @Value("${org.app.helaHub.properties.hikari.validation.timeout}")
    private Integer hikariValidationTimeout;
    @Value("${org.app.helaHub.properties.hikari.maximum.pool.size}")
    private Integer hikariMaximumPoolSize;
    @Value("${org.app.helaHub.properties.hikari.pool.name}")
    private String hikariPoolName;
    @Value("${org.app.helaHub.properties.hikari.leak.detection.threshold}")
    private Integer hikariLeakDetectionThreshold;
    @Value("${org.app.helaHub.properties.cache.prep.stmts}")
    private boolean cachePrepStmts;
    @Value("${org.app.helaHub.properties.prep.stmt.cache.size}")
    private Integer prepStmtCacheSize;
    @Value("${org.app.helaHub.properties.prep.stmt.cache.sqllimit}")
    private Integer prepStmtCacheSqllimit;
    @Value("${org.app.helaHub.properties.hibernate.jdbc.lob.non.contextual.creation}")
    private String jdbcLobNonContextualCreation;

}
