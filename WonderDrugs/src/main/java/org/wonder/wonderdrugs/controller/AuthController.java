package org.wonder.wonderdrugs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wonder.wonderdrugs.dto.AuthRequest;
import org.wonder.wonderdrugs.dto.AuthResponse;
import org.wonder.wonderdrugs.service.VaultService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final VaultService vaultService;

    @Autowired
    public AuthController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<AuthResponse> loginWithForm(
            @RequestParam String username,
            @RequestParam String password) {

        logger.info("Processing form login request for user: {}", username);
        return processLogin(username, password);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> loginWithJson(@RequestBody AuthRequest request) {
        logger.info("Processing JSON login request for user: {}", request.getUsername());
        return processLogin(request.getUsername(), request.getPassword());
    }

    private ResponseEntity<AuthResponse> processLogin(String username, String password) {
        String sessionId = vaultService.authenticate(username, password);

        AuthResponse response = new AuthResponse();
        if (sessionId != null) {
            response.setStatus("success");
            response.setSessionId(sessionId);
            return ResponseEntity.ok(response);
        } else {
            response.setStatus("error");
            response.setMessage("Authentication failed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/keep-alive")
    public ResponseEntity<AuthResponse> keepAlive() {
        vaultService.keepAlive();

        AuthResponse response = new AuthResponse();
        response.setStatus("success");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        logger.info("处理注销请求");

        boolean success = vaultService.logout();

        AuthResponse response = new AuthResponse();
        if (success) {
            response.setStatus("success");
            response.setMessage("成功注销");
            return ResponseEntity.ok(response);
        } else {
            response.setStatus("error");
            response.setMessage("注销失败");
            return ResponseEntity.badRequest().body(response);
        }
    }




// ... existing code ...

}
