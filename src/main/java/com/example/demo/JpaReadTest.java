package com.example.demo;

import com.example.demo.repository.LogEntryRepoIF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@SpringBootApplication
@Slf4j
@EntityScan("com.example.demo.model")
public class JpaReadTest {
    @Autowired
    LogEntryRepoIF repo;

    public static void main(String[] args) throws IOException {

        SpringApplication app = new SpringApplication(JpaReadTest.class);

        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);

    }

    @Bean
    public CommandLineRunner runJacksonJpaProcessor() throws IOException {
        return (args) -> {
            System.out.println("***");
            System.out.println("****");
            System.out.println("*****");
            System.out.println("******");
            System.out.println("*******");
            System.out.println("Response from findAll "+repo.findAll());
            Iterator i =repo.findAll().iterator();
            while(i.hasNext()){
                System.out.println(i.next());
            }
        };
    }


}
