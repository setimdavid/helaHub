package org.app.helaHub.store.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.app.helaHub.config.properties.JpaConnectionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.app.helaHub.store.repo"}, entityManagerFactoryRef = "entityManagerFactoryBean")
public class HibernateJpaConfiguration implements TransactionManagementConfigurer {

    private final JpaConnectionProperties jcp;

    public HibernateJpaConfiguration(JpaConnectionProperties jcp) {
        this.jcp = jcp;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    private Properties hibernateAdditionalProperties() {
        Properties results = new Properties();
        results.setProperty("hibernate.dialect", jcp.getDialect());
        results.setProperty("hibernate.hbm2ddl.auto", jcp.getHbm2ddlAuto());
        results.setProperty("hibernate.show_sql", jcp.getShowSql());
        results.setProperty("hibernate.format_sql", jcp.getShowSql());
        results.setProperty("hibernate.use_sql_comments", jcp.getShowSql());
        results.setProperty("hibernate.generate_statistics", jcp.getGenerateStatistics());
        results.setProperty("hibernate.id.new_generator_mappings", jcp.getHibernateIdNewgeneratorMappings());
        results.setProperty("hibernate.jdbc.lob.non_contextual_creation", jcp.getJdbcLobNonContextualCreation());
        return results;
    }

    @Bean(name = "datasource")
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        HikariDataSource hc = null;
        try {
            config.setJdbcUrl(jcp.getJdbcUrl());
            config.setUsername(jcp.getJdbcUser());
            config.setPassword(jcp.getJdbcPass());
            config.setConnectionTimeout(jcp.getHikariConnectionTimeout());
            config.setIdleTimeout(jcp.getHikariIdleTimeout());
            config.setMaxLifetime(jcp.getHikariMaxLifetime());
            config.setValidationTimeout(jcp.getHikariValidationTimeout());
            config.setMaximumPoolSize(jcp.getHikariMaximumPoolSize());
            config.setPoolName(jcp.getHikariPoolName());
            config.setLeakDetectionThreshold(jcp.getHikariLeakDetectionThreshold());
            config.addDataSourceProperty("cachePrepStmts", jcp.isCachePrepStmts());
            config.addDataSourceProperty("prepStmtCacheSize", jcp.getPrepStmtCacheSize());
            config.addDataSourceProperty("prepStmtCacheSqlLimit", jcp.getPrepStmtCacheSqllimit());

            hc = new HikariDataSource(config);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return hc;

    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
        try {
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            vendorAdapter.setGenerateDdl(jcp.getShowSql().equals("true"));
            vendorAdapter.setDatabase(Database.POSTGRESQL);
            vendorAdapter.setShowSql(jcp.getShowSql().equals("true"));
            final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setJpaProperties(hibernateAdditionalProperties());
            em.setPackagesToScan(jcp.getSpringPackagesToScan());
            em.setDataSource(dataSource());
            em.setJpaVendorAdapter(vendorAdapter);
            return em;
        } catch (Exception e) {
            Logger.getLogger(HibernateJpaConfiguration.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return new LocalContainerEntityManagerFactoryBean();
        }
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return null;
    }
}
