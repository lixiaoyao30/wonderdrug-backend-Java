package org.wonder.wonderdrugs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class VaultConfig {

    @Value("${vault.url}")
    private String vaultUrl;

    @Value("${vault.web.url}")
    private String vaultWebUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getVaultWebUrl() {
        return vaultWebUrl;
    }
}
