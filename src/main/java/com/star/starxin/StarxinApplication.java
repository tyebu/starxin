package com.star.starxin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.star.starxin.mapper")
@ComponentScan(basePackages= {"com.star.starxin", "org.n3r.idworker"})
public class StarxinApplication {
    @Bean
    public SpringUtil getSpringUtils() {
        return new SpringUtil();
    }
    public static void main(String[] args) {
        SpringApplication.run(StarxinApplication.class, args);
    }

}
