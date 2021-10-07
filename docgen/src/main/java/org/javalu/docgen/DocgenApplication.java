package org.javalu.docgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.javalu.docgen.**"})
public class DocgenApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocgenApplication.class, args);
    }

}
