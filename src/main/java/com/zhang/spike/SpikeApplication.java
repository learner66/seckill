package com.zhang.spike;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.zhang.spike.dao")
public class SpikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpikeApplication.class, args);
    }
}
