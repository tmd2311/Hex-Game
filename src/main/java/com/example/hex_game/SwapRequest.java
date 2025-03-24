package com.example.hex_game;

public class SwapRequest {
    private boolean swap; // true nếu người chơi muốn hoán đổi

    // Constructor
    public SwapRequest() {}

    // Getter
    public boolean isSwap() {
        return swap;
    }

    // Setter
    public void setSwap(boolean swap) {
        this.swap = swap;
    }
}