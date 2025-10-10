// src/main/java/.../SpaForwarding.java
package com.redislabs.demos.redisbank;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@Profile("prod") // <- n’active ce forwarder qu’en prod pour ne pas gêner le dev Vite
public class SpaForwarding implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry r) {
        // Tes routes Vue "history mode" connues (top-level)
        r.addViewController("/login").setViewName("forward:/index.html");
        r.addViewController("/dashboard").setViewName("forward:/index.html");
        // Ajoute ici d’autres routes top-level si tu en crées
        // r.addViewController("/settings").setViewName("forward:/index.html");
  }
}