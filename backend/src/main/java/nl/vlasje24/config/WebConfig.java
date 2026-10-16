package nl.vlasje24.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@Configuration
public class WebConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
