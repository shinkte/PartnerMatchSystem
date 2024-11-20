package com.shinkte;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 *
 * @author shinkte
 * @from shinkte
 */
@SpringBootApplication
@MapperScan("com.shinkte.mapper")
public class MatchSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchSystemApplication.class, args);
    }

}

