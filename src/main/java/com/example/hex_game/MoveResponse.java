package com.example.hex_game;

public class MoveResponse {
    private boolean success;
    private int winner;


    public MoveResponse(boolean success, int winner) {
        this.success = success;
        this.winner = winner;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getWinner() {
        return winner;
    }
}
