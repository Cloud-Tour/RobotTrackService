package com.zkd.robotTrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Retryable //启用重试机制
@EnableAsync //启用异步机制
public class RobotTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(RobotTrackApplication.class,args);
    }
}
