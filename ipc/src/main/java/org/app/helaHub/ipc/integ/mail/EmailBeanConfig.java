package org.app.helaHub.ipc.integ.mail;

import org.jasypt.encryption.StringEncryptor;
import org.muhia.app.leto.store.model.EmailProperties;
import org.muhia.app.leto.store.repo.EmailPropertiesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.Properties;

@Configuration
public class EmailBeanConfig {

    private final EmailPropertiesRepository emailPropertiesRepository;
    private final EmailProperties emailProperties;
    private final StringEncryptor jasyptStringEncryptor;
    public EmailBeanConfig(EmailPropertiesRepository emailPropertiesRepository, StringEncryptor jasyptStringEncryptor){
        this.emailPropertiesRepository = emailPropertiesRepository;
        this.jasyptStringEncryptor = jasyptStringEncryptor;

        this.emailProperties = emailPropertiesRepository.findEmailPropertiesById(1);
    }

    @Bean
    public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration() {
        FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
        bean.setTemplateLoaderPath("/templates/mailer/");
        return bean;
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());

        mailSender.setUsername(jasyptStringEncryptor.decrypt(emailProperties.getUsername()));
        mailSender.setPassword(jasyptStringEncryptor.decrypt(emailProperties.getPassword()));

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailProperties.getTransportProtocol());
        props.put("mail.smtp.auth", emailProperties.getSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailProperties.getSmtpStarttlsEnable());
        props.put("mail.debug", emailProperties.getIsDebug());

        return mailSender;
    }


}
