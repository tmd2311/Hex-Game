package com.example.hex_game;

public class SwapResponse {
    private boolean success; // Trạng thái hoán đổi
    private String message;  // Thông báo

    // Constructor
    public SwapResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getter
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}