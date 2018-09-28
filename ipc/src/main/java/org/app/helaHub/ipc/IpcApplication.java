package org.app.helaHub.ipc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = {"org.app.helaHub"})
public class IpcApplication {

    public static void main(String[] args) {
        SpringApplication.run(IpcApplication.class, args);
    }
}
