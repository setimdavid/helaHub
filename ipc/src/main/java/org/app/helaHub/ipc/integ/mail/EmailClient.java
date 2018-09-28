package org.app.helaHub.ipc.integ.mail;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.*;
import lombok.extern.java.Log;
import org.muhia.app.leto.store.model.EmailRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

@Log
@Component
public class EmailClient {
    @Qualifier("getJavaMailSender")
    @Autowired
    private JavaMailSender emailSender;
    @Qualifier("getFreeMarkerConfiguration")
    @Autowired
    private Configuration freemarkerConfig;

    public String emailSender(EmailRegistry emailRegistry) {
        String response = "Failed";
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            String mailMessage = "";

            if (emailRegistry.getUseTemplate() == 1){
                Template t = freemarkerConfig.getTemplate(emailRegistry.getTemplateName());
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(emailRegistry.getMessagePayload(), new TypeReference<Map<String, String>>() {});
                mailMessage = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
            }else{
                mailMessage = emailRegistry.getEmailMessage();
            }
            if (emailRegistry.getIsAttachment() == 1){
                String filePath = emailRegistry.getAttachment();
                FileSystemResource file = new FileSystemResource(filePath);
                helper.addAttachment(file.getFilename(), file);
            }

            helper.setTo(emailRegistry.getEmailDestination());
            helper.setText(mailMessage, true);
            helper.setSubject(emailRegistry.getEmailSubject());
           // helper.setFrom(mail.getFrom());

            emailSender.send(message);
            response = "Success";
        }catch(Exception ex){
            log.log(Level.INFO,ex.getMessage());
        }
        return response;
    }

}
