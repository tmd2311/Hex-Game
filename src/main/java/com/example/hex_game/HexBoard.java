package com.example.hex_game;

public class HexBoard {
    private  int[][] board;
    private int size;
    private boolean swapped = false; // Kiểm tra xem đã hoán đổi chưa
    private int currentPlayer = 1; // Người chơi hiện tại (1 hoặc 2)

    public HexBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    public boolean makeMove(int row, int col, int player) {
        // Kiểm tra tọa độ hợp lệ
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false; // Tọa độ không hợp lệ
        }

        // Kiểm tra ô đã được chọn hay chưa
        if (board[row][col] != 0) {
            return false; // Ô đã được chọn
        }

        // Cập nhật trạng thái ô
        board[row][col] = player;

        // Vô hiệu hóa Swap nếu người chơi thứ 2 thực hiện nước đi
        if (player == 2 && !swapped) {
            swapped = true;
            System.out.println("Player 2 has made a move. Swap is now disabled.");
        }

        // Kiểm tra người chiến thắng
        int winner = checkWinner();
        if (winner != 0) {
            System.out.println("Player " + winner + " has won the game!");
        }

        return true; // Nước đi thành công
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

    public void swap() {
        if (swapped) {
            System.out.println("Swap has already been performed.");
            return;
        }
        // Kiểm tra xem người chơi thứ 2 đã đi chưa
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 2) { // Nếu người chơi thứ 2 đã đi
                    System.out.println("Player 2 has already made a move. Swap is not allowed.");
                    return;
                }
            }
        }
        // Thực hiện swap
        swapped = true;
        currentPlayer = 2;
        System.out.println("Players swapped successfully! Player 2 becomes Player 1.");
    }

    public boolean hasSwapped() {
        return swapped;
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
