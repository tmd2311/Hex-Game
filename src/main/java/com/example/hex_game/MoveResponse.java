package com.example.hex_game;

public class MoveResponse {
    private boolean success;
    private int winner;


    public MoveResponse(boolean success, int winner) {
        this.success = success;
        this.winner = winner;
    }

    // Getter và Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getWinner() { return winner; }
    public void setWinner(int winner) { this.winner = winner; }

}
