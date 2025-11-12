package com.booklending.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.booklending.mapper")
public class MyBatisConfig {
}