package org.wonder.wonderdrugs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.wonder.wonderdrugs.config.VaultConfig;
import org.wonder.wonderdrugs.exception.VaultApiException;

@Service
public class VaultService {
    private static final Logger logger = LoggerFactory.getLogger(VaultService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final VaultConfig vaultConfig;

    private String sessionId;

    @Autowired
    public VaultService(RestTemplate restTemplate, VaultConfig vaultConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.vaultConfig = vaultConfig;
    }

    public String authenticate(String username, String password) {
        logger.info("Authenticating user: {}", username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    vaultConfig.getVaultUrl() + "/auth", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if ("SUCCESS".equals(root.path("responseStatus").asText())) {
                sessionId = root.path("sessionId").asText();
                logger.info("Authentication successful");
                return sessionId;
            } else {
                logger.warn("Authentication failed: {}", root.path("errors"));
                return null;
            }
        } catch (Exception e) {
            logger.error("Authentication error", e);
            return null;
        }
    }

    public void keepAlive() {
        if (sessionId != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", sessionId);
            headers.set("Accept", "application/json");

            HttpEntity<String> request = new HttpEntity<>(headers);

            try {
                restTemplate.postForEntity(vaultConfig.getVaultUrl() + "/keep-alive", request, String.class);
                logger.debug("Session kept alive");
            } catch (Exception e) {
                logger.error("Error keeping session alive", e);
            }
        }
    }

    public JsonNode executeQuery(String query) {
        if (sessionId == null) {
            logger.error("No active session");
            throw new VaultApiException("No active session");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", sessionId);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 修改这里，只设置 Accept 为 application/json
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("q", query);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            logger.debug("Executing query: {}", query);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    vaultConfig.getVaultUrl() + "/query", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if ("SUCCESS".equals(root.path("responseStatus").asText())) {
                return root.path("data");
            } else {
                logger.error("Query failed: {}", root.path("errors"));
                throw new VaultApiException("Query failed: " + root.path("errors").toString());
            }
        } catch (Exception e) {
            logger.error("Error executing query", e);
            throw new VaultApiException("Error executing query: " + e.getMessage());
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getVaultWebUrl() {
        return vaultConfig.getVaultWebUrl();
    }


    // ... existing code ...

    /**
     * 注销当前会话
     * @return 是否成功注销
     */
    // ... existing code ...

    /**
     * 注销当前会话
     * @return 是否成功注销
     */
    public boolean logout() {
        if (sessionId == null) {
            logger.warn("尝试注销但没有活动会话");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Authorization", sessionId);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    vaultConfig.getVaultUrl() + "/session",
                    HttpMethod.DELETE,  // Vault API使用DELETE方法结束会话
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            if ("SUCCESS".equals(root.path("responseStatus").asText())) {
                logger.info("成功从Vault注销");
                sessionId = null;  // 清除会话ID
                return true;
            } else {
                logger.warn("从Vault注销失败: {}", root.path("errors"));
                return false;
            }
        } catch (Exception e) {
            logger.error("Vault注销过程中出错: {}", e.getMessage(), e);
            return false;
        }
    }


}
