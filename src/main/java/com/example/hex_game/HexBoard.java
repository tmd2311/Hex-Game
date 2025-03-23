package com.example.hex_game;

public class HexBoard {
    private  int[][] board;
    private int size;

    public HexBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    public boolean makeMove(int row, int col, int player) {
        if(row >= 0 && row < size && col >= 0 && col < size && board[row][col] == 0)
        {
            board[row][col] = player;
            return true;
        }
        return false;
    }

    public int[][] getBoard() {
        return board;
    }
    public int checkWinner() {
        for(int i = 0; i < size; i++) {
            boolean[] visited1 = new boolean[size * size];
            boolean[] visited2 = new boolean[size * size];
            if(board[i][0] == 1 && dfs(i, 0, 1, visited1)) return 1;
            if(board[0][i] == 2 && dfs(0, i, 2, visited2)) return 2;
        }
        return 0;
    }



    private boolean dfs(int row, int col, int player, boolean[] visited) {
        int index = row*size + col;
        if(visited[index]) return false;
        visited[index] = true;

        if(player == 1 && col == size - 1) return true;
        if(player == 2 && row == size - 1) return true;

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
        for(int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];
            if(newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && board[newRow][newCol] == player
                    && dfs(newRow, newCol, player, visited)) { return true; }
        }
        return false;
    }
}
