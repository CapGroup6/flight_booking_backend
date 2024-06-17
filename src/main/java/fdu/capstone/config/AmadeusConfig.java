package fdu.capstone.config;

import fdu.capstone.system.module.service.impl.AmadeusService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmadeusConfig {

    @Bean
    public AmadeusService amadeusService(@Value("${amadeus.apiKey}") String apiKey,
                                         @Value("${amadeus.apiSecret}") String apiSecret) {
        return new AmadeusService(apiKey, apiSecret);
    }
}
