package org.infinitesolutions.tinyurl.config;

import org.infinitesolutions.tinyurl.common.util.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowFlakeIdGeneratorConfig {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(1, 1);
    }
}
