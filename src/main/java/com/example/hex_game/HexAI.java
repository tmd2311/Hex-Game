package com.example.hex_game;

public class HexAI {
    private int size;
    private int[][] board;
    private HexBoard hexBoard;

    public HexAI(int size, int[][] board, HexBoard hexBoard) {
        this.size = size;
        this.board = board;
        this.hexBoard = hexBoard;
    }

    // Tổng hợp đánh giá AI và đối thủ
    private int evaluateBoard(int player) {
        int opponent = (player == 1) ? 2 : 1;
        int playerScore = evaluatePlayer(player);
        int opponentScore = evaluatePlayer(opponent);
        return playerScore - (int)(0.8 * opponentScore); // Giảm điểm nếu đối thủ mạnh
    }

    // Kiểm tra tiến độ đường đi, trả về [furthestRow, furthestCol]
    private int[] checkPathProgress(int row, int col, int player, boolean[] visited) {
        int index = row * size + col;
        if (visited[index]) return new int[]{row, col};
        visited[index] = true;

        int furthestRow = row; // Đối với Player 2: tìm hàng xa nhất bên dưới
        int furthestCol = col; // Đối với Player 1: tìm cột xa nhất bên phải

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && board[newRow][newCol] == player) {
                int[] progress = checkPathProgress(newRow, newCol, player, visited);
                furthestRow = Math.max(furthestRow, progress[0]);
                furthestCol = Math.max(furthestCol, progress[1]);
            }
        }
        return new int[]{furthestRow, furthestCol};
    }

    // Đánh giá điểm cho từng người chơi
    private int evaluatePlayer(int player) {
        int score = 0;
        boolean[] visited = new boolean[size * size];

        // Thưởng nếu gần thắng
        for (int i = 0; i < size; i++) {
            if (player == 1 && board[i][0] == 1 && hexBoard.dfs(i, 0, 1, visited)) {
                score += 1000;
            } else if (player == 2 && board[0][i] == 2 && hexBoard.dfs(0, i, 2, visited)) {
                score += 1000;
            }
        }

        // Đánh giá tiến độ đường đi (tìm đường dài nhất đến biên đích)
        int maxProgress = 0;
        for (int i = 0; i < size; i++) {
            if (player == 1 && board[i][0] == 1) {
                int[] progress = checkPathProgress(i, 0, player, new boolean[size * size]);
                maxProgress = Math.max(maxProgress, progress[1]); // Player 1 quan tâm đến cột (progress[1])
            } else if (player == 2 && board[0][i] == 2) {
                int[] progress = checkPathProgress(0, i, player, new boolean[size * size]);
                maxProgress = Math.max(maxProgress, progress[0]); // Player 2 quan tâm đến hàng (progress[0])
            }
        }
        // Thưởng điểm nếu đường đi gần biên đích
        if (player == 1) {
            score += maxProgress * 150; // Player 1: càng gần cột cuối càng nguy hiểm
            if (maxProgress >= size - 2) score += 500; // Gần thắng (thiếu 1-2 ô)
        } else {
            score += maxProgress * 150; // Player 2: càng gần hàng cuối càng nguy hiểm
            if (maxProgress >= size - 2) score += 500;
        }

        // Cộng điểm từng ô
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == player) {
                    score += getHexCellValue(r, c, player);
                }
            }
        }

        return score;
    }

    // Heuristic cho 1 ô: ưu tiên trung tâm, gần nhau, gần biên chiến thắng
    private int getHexCellValue(int row, int col, int player) {
        int score = 0;

        // Ưu tiên trung tâm
        int centerScore = (size - Math.abs(row - size/2)) + (size - Math.abs(col - size/2));
        score += centerScore * 10;

        // Ưu tiên gần biên chiến thắng
        if (player == 1 && (col == 0 || col == size - 1)) score += 50;
        if (player == 2 && (row == 0 || row == size - 1)) score += 50;

        // Ưu tiên gần ô cùng màu
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
        int connectedBonus = 0;
        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                if (board[newRow][newCol] == player) {
                    connectedBonus += 20;
                }
            }
        }

        score += connectedBonus;
        return score;
    }

    // Thuật toán Minimax có alpha-beta pruning
    private int minmax(int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0) return evaluateBoard(2); // AI là player 2

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (board[row][col] == 0) {
                        board[row][col] = 2; // AI move
                        int eval = minmax(depth - 1, alpha, beta, false);
                        board[row][col] = 0;
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) return maxEval;
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (board[row][col] == 0) {
                        board[row][col] = 1; // Human move
                        int eval = minmax(depth - 1, alpha, beta, true);
                        board[row][col] = 0;
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) return minEval;
                    }
                }
            }
            return minEval;
        }
    }

    // Tìm nước đi tốt nhất cho AI
    public int[] getBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        // Ưu tiên chặn nếu đối thủ sắp thắng
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    if (hexBoard.willWinIfMove(row, col, 1)) {
                        System.out.println("Chặn nước thắng ngay lập tức tại: " + row + "," + col);
                        return new int[]{row, col}; // Chặn thắng ngay lập tức
                    }
                }
            }
        }

        // Ưu tiên chặn các ô nằm trên đường gần thắng của Player 1
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    // Giả lập Player 1 đi ở ô này
                    board[row][col] = 1;
                    int progress = 0;
                    for (int i = 0; i < size; i++) {
                        if (board[i][0] == 1) {
                            int[] pathProgress = checkPathProgress(i, 0, 1, new boolean[size * size]);
                            progress = Math.max(progress, pathProgress[1]); // Player 1 quan tâm đến cột
                        }
                    }
                    board[row][col] = 0;

                    // Nếu ô này giúp Player 1 gần thắng (chỉ thiếu 1 ô)
                    if (progress >= size - 1) {
                        System.out.println("Chặn ô trên đường gần thắng tại: " + row + "," + col);
                        return new int[]{row, col};
                    }
                }
            }
        }

        // Ưu tiên chặn nếu ô trống có nhiều ô của Player 1 xung quanh
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    int adjacentCount = 0;
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
                    for (int[] dir : directions) {
                        int newRow = row + dir[0], newCol = col + dir[1];
                        if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && board[newRow][newCol] == 1) {
                            adjacentCount++;
                        }
                    }
                    if (adjacentCount >= 3) { // Nếu có từ 3 ô của Player 1 xung quanh
                        System.out.println("Chặn ô nguy hiểm tại: " + row + "," + col);
                        return new int[]{row, col};
                    }
                }
            }
        }

        // Ưu tiên chặn nếu đối thủ có bước tiến tốt
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    // Giả lập người chơi 1 đi
                    board[row][col] = 1;
                    int score = evaluateBoard(1); // Tính điểm nếu player 1 đi ở đây
                    board[row][col] = 0;

                    // Nếu điểm tăng mạnh → đối thủ có bước đi tốt → chặn!
                    if (score > 500) {
                        System.out.println("Chặn trước nước nguy hiểm tại: " + row + "," + col);
                        return new int[]{row, col};
                    }
                }
            }
        }

        // Nếu không cần chặn, dùng Minimax để tìm nước đi tốt nhất
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    board[row][col] = 2; // AI thử nước đi
                    int moveScore = minmax(4, Integer.MIN_VALUE, Integer.MAX_VALUE, false); // Độ sâu 4
                    board[row][col] = 0;

                    if (moveScore > bestScore) {
                        bestScore = moveScore;
                        bestMove = new int[]{row, col};
                    }
                }
            }
        }

        return bestMove;
    }

    // Hàm gọi AI chơi
    public void aiMove() {
        int[] move = getBestMove();
        if (move[0] != -1) {
            hexBoard.makeMove(move[0], move[1], 2);
            System.out.println("AI moved at: " + move[0] + ", " + move[1]);
        }
    }
}