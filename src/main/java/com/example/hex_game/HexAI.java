package com.example.hex_game;

public class HexAI {
    private int size;
    private int[][] board;
    private HexBoard hexBoard;

    public HexAI(int size, int[][] board) {
        this.size = size;
        this.board = board;
    }
    private int evaluateBoard(int player) {
        int score = 0;
        boolean[] visited = new boolean[size *size];
        for (int i = 0; i < size; i++) {
            if(player == 1 && board[i][0] == 1 && hexBoard.dfs(i, 0, 1, visited) ) {
                score +=1000;
            }
            if (player == 2 && board[0][i]== 1 && hexBoard.dfs(0, i, 2, visited)){
                score +=1000;
            }
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board[r][c] == 2){
                        score += getHexCellValue(r, c, 2);
                    }else if (board[r][c] == 1){
                        score += getHexCellValue(r, c, 1);
                    }
                }
            }
        }
        return score;
    }
    private int getHexCellValue(int row, int col, int player) {
        int center = size / 2;
        int distanceToCenter = Math.abs(row - center) + Math.abs(col - center);
        return (size - distanceToCenter) *10;
    }
    private int minmax(int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0) return  evaluateBoard(2);
        if (isMaximizing){
            int maxEval = Integer.MIN_VALUE;
            for(int row = 0; row < size; row++){
                for(int col = 0; col < size; col++){
                    if(board[row][col] == 0){
                        board[row][col] = 2;
                        int eval = minmax(depth - 1, alpha, beta, false);
                        board[row][col] = 0;
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if(beta <= alpha){
                            return maxEval;
                        }
                    }
                }
            }
            return maxEval;
        }
        else {
            int minEval = Integer.MAX_VALUE;
            for(int row = 0; row < size; row++){
                for(int col = 0; col < size; col++){
                    if(board[row][col] == 0){
                        board[row][col] = 1;
                        int eval = minmax(depth - 1, alpha, beta, true);
                        board[row][col] = 0;
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if(beta <= alpha){
                            return minEval;
                        }
                    }
                }
            }
            return minEval;
        }
    }
    public int[] getBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if(board[row][col] == 0){
                    board[row][col] = 2;
                    int  score = minmax(3, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                    board[row][col] = 0;
                    if(score > bestScore){
                        bestScore = score;
                        bestMove[0] = row;
                        bestMove[1] = col;

                    }
                }
            }
        }
        return bestMove;
    }
    public void aiMove(){
        int[] move = getBestMove();
        if(move[0] != -1 ){
            hexBoard.makeMove(move[0], move[1], 2);
            System.out.println("AI moved at: " + move[0] + ", " + move[1]);
        }
    }


}
