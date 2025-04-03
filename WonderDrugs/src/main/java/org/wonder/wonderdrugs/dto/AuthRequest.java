package org.wonder.wonderdrugs.dto;

public class AuthRequest {
    private String username;
    private String password;

    // 默认构造函数
    public AuthRequest() {
    }

    // 带参数的构造函数
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
