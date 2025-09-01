package com.claude.automator;

import com.claude.automator.automation.SimpleAutomation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.claude.automator", "io.github.jspinak.brobot"})
public class SimpleApplication {

    public static void main(String[] args) {
        System.setProperty("sikuli.Debug", "3");
        System.setProperty("sikuli.Silence", "false");
        System.setProperty("java.awt.headless", "false");
        System.setProperty("apple.awt.UIElement", "false");
        
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApplication.class, args);
        
        SimpleAutomation automation = context.getBean(SimpleAutomation.class);
        automation.run();
        
        context.close();
    }
}