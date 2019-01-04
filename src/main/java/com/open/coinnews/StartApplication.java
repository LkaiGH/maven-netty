package com.open.coinnews;

import org.n3r.idworker.Sid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan(basePackages = "com.open.coinnews.app.dao")
public class StartApplication{

    private static final Logger logger = LoggerFactory.getLogger(StartApplication.class);

    public static void main(String [] args) {
        logger.info("start ");
        SpringApplication.run(StartApplication.class, args);
    }

    @Bean
    public SpringUtil getSpingUtil() {
        return new SpringUtil();
    }

    @Bean
    public Sid sid() {
        return new Sid();
    }
}
