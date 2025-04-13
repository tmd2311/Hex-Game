package com.example.hex_game;

public class HexAI {
    private int size;
    private int[][] board;
    private HexBoard hexBoard;
    private int[][] pathProgressCache; // Cache để lưu kết quả của checkPathProgress

    public HexAI(int size, int[][] board, HexBoard hexBoard) {
        this.size = size;
        this.board = board;
        this.hexBoard = hexBoard;
        this.pathProgressCache = new int[size * size][2]; // Cache: [furthestRow, furthestCol]
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

        // Kiểm tra cache
        if (pathProgressCache[index][0] != 0 || pathProgressCache[index][1] != 0) {
            return pathProgressCache[index];
        }

        int furthestRow = row; // Đối với Player 2: tìm hàng xa nhất bên dưới
        int furthestCol = col; // Đối với Player 1: tìm cột xa nhất bên phải

        // Điều kiện dừng sớm
        if (player == 2 && furthestRow >= size - 1) {
            return new int[]{furthestRow, furthestCol};
        }
        if (player == 1 && furthestCol >= size - 1) {
            return new int[]{furthestRow, furthestCol};
        }

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && board[newRow][newCol] == player) {
                int[] progress = checkPathProgress(newRow, newCol, player, visited);
                furthestRow = Math.max(furthestRow, progress[0]);
                furthestCol = Math.max(furthestCol, progress[1]);

                // Điều kiện dừng sớm
                if (player == 2 && furthestRow >= size - 1) break;
                if (player == 1 && furthestCol >= size - 1) break;
            }
        }

        // Lưu vào cache
        pathProgressCache[index] = new int[]{furthestRow, furthestCol};
        return pathProgressCache[index];
    }

    // Kiểm tra chuỗi quân liên tiếp trên cùng một hàng
    private int countConsecutiveOnRow(int row, int player) {
        int count = 0;
        int maxCount = 0;
        for (int col = 0; col < size; col++) {
            if (board[row][col] == player) {
                count++;
                maxCount = Math.max(maxCount, count);
            } else {
                count = 0; // Reset nếu gặp ô không phải của player
            }
        }
        return maxCount;
    }

    // Kiểm tra chuỗi quân liên tiếp trên cùng một cột
    private int countConsecutiveOnCol(int col, int player) {
        int count = 0;
        int maxCount = 0;
        for (int row = 0; row < size; row++) {
            if (board[row][col] == player) {
                count++;
                maxCount = Math.max(maxCount, count);
            } else {
                count = 0; // Reset nếu gặp ô không phải của player
            }
        }
        return maxCount;
    }

    // Đánh giá điểm cho từng người chơi
    private int evaluatePlayer(int player) {
        int score = 0;
        boolean[] visited = new boolean[size * size];

        // Thưởng nếu gần thắng
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == player) {
                    if (player == 2 && hexBoard.dfs(r, c, 2, visited)) {
                        score += 1000;
                    } else if (player == 1 && hexBoard.dfs(r, c, 1, visited)) {
                        score += 1000;
                    }
                }
            }
        }

        // Đánh giá tiến độ đường đi (tìm đường dài nhất đến biên đích)
        int maxProgress = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == player) {
                    int[] progress = checkPathProgress(r, c, player, new boolean[size * size]);
                    if (player == 2) {
                        maxProgress = Math.max(maxProgress, progress[0]); // Player 2 quan tâm đến hàng (trên → dưới)
                    } else {
                        maxProgress = Math.max(maxProgress, progress[1]); // Player 1 quan tâm đến cột (trái → phải)
                    }
                }
            }
        }

        // Thưởng điểm nếu đường đi gần biên đích
        if (player == 2) {
            score += maxProgress * 150; // Player 2: càng gần hàng cuối càng nguy hiểm
            if (maxProgress >= size - 2) score += 500; // Gần thắng (thiếu 1-2 ô)

            // Thưởng thêm nếu có chuỗi quân liên tiếp trên cùng một cột
            for (int col = 0; col < size; col++) {
                int consecutive = countConsecutiveOnCol(col, player);
                if (consecutive >= size - 2) score += 600; // Chuỗi dài gần thắng
                else if (consecutive >= size - 3) score += 300; // Chuỗi khá dài
            }
        } else {
            score += maxProgress * 150; // Player 1: càng gần cột cuối càng nguy hiểm
            if (maxProgress >= size - 2) score += 500;

            // Thưởng thêm nếu có chuỗi quân liên tiếp trên cùng một hàng
            for (int row = 0; row < size; row++) {
                int consecutive = countConsecutiveOnRow(row, player);
                if (consecutive >= size - 2) score += 600; // Chuỗi dài gần thắng
                else if (consecutive >= size - 3) score += 300; // Chuỗi khá dài
            }
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

        // Ưu tiên gần biên chiến thắng, nhưng giảm điểm nếu đối thủ có chuỗi dài
        int opponent = (player == 1) ? 2 : 1;
        boolean opponentHasLongChain = false;
        if (opponent == 1) {
            for (int r = 0; r < size; r++) {
                if (countConsecutiveOnRow(r, opponent) >= 2) { // Giảm ngưỡng xuống 2
                    opponentHasLongChain = true;
                    break;
                }
            }
        } else {
            for (int c = 0; c < size; c++) {
                if (countConsecutiveOnCol(c, opponent) >= 2) { // Giảm ngưỡng xuống 2
                    opponentHasLongChain = true;
                    break;
                }
            }
        }

        if (!opponentHasLongChain) {
            if (player == 2 && (row == 0 || row == size - 1)) score += 50; // Player 2: trên → dưới
            if (player == 1 && (col == 0 || col == size - 1)) score += 50; // Player 1: trái → phải
        }

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
        if (depth == 0) return evaluateBoard(2); // AI là Player 2

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

        // Reset cache trước khi tìm nước đi mới
        pathProgressCache = new int[size * size][2];

        // Ưu tiên chặn nếu đối thủ (Player 1) sắp thắng
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    if (hexBoard.willWinIfMove(row, col, 1)) {
                        System.out.println("Chặn nước thắng ngay lập tức của Player 1 tại: " + row + "," + col);
                        return new int[]{row, col}; // Chặn thắng ngay lập tức
                    }
                }
            }
        }

        // Kiểm tra chuỗi quân liên tiếp trên cùng một hàng để chặn đường của Player 1 (trái → phải)
        for (int row = 0; row < size; row++) {
            int consecutive = countConsecutiveOnRow(row, 1); // Đếm chuỗi của Player 1 trên hàng
            if (consecutive >= 2) { // Giảm ngưỡng xuống 2 để chặn sớm
                // Tìm ô đầu và ô cuối của chuỗi
                int firstCol = -1, lastCol = -1;
                for (int col = 0; col < size; col++) {
                    if (board[row][col] == 1) {
                        if (firstCol == -1) firstCol = col;
                        lastCol = col;
                    }
                }
                // Kiểm tra các ô lân cận để chặn
                if (firstCol != -1 && lastCol != -1) {
                    // Tính khoảng cách đến biên chiến thắng của Player 1 (cột 0 và cột 4)
                    int distToLeftBorder = firstCol; // Khoảng cách từ ô đầu đến cột 0
                    int distToRightBorder = (size - 1) - lastCol; // Khoảng cách từ ô cuối đến cột 4

                    // Ưu tiên chặn đầu gần biên chiến thắng hơn
                    if (distToRightBorder <= distToLeftBorder) { // Cuối chuỗi gần cột 4 hơn
                        if (lastCol < size - 1 && board[row][lastCol + 1] == 0) {
                            System.out.println("Chặn cuối chuỗi (gần biên cột 4) của Player 1 tại: " + row + "," + (lastCol + 1));
                            return new int[]{row, lastCol + 1};
                        } else if (firstCol > 0 && board[row][firstCol - 1] == 0) {
                            System.out.println("Chặn đầu chuỗi của Player 1 tại: " + row + "," + (firstCol - 1));
                            return new int[]{row, firstCol - 1};
                        }
                    } else { // Đầu chuỗi gần cột 0 hơn
                        if (firstCol > 0 && board[row][firstCol - 1] == 0) {
                            System.out.println("Chặn đầu chuỗi (gần biên cột 0) của Player 1 tại: " + row + "," + (firstCol - 1));
                            return new int[]{row, firstCol - 1};
                        } else if (lastCol < size - 1 && board[row][lastCol + 1] == 0) {
                            System.out.println("Chặn cuối chuỗi của Player 1 tại: " + row + "," + (lastCol + 1));
                            return new int[]{row, lastCol + 1};
                        }
                    }
                }
            }
        }

        // Kiểm tra chuỗi quân liên tiếp trên cùng một cột để chặn đường của Player 1 (nếu cần)
        for (int col = 0; col < size; col++) {
            int consecutive = countConsecutiveOnCol(col, 1); // Đếm chuỗi của Player 1 trên cột
            if (consecutive >= 2) { // Giảm ngưỡng xuống 2
                // Tìm ô đầu và ô cuối của chuỗi
                int firstRow = -1, lastRow = -1;
                for (int row = 0; row < size; row++) {
                    if (board[row][col] == 1) {
                        if (firstRow == -1) firstRow = row;
                        lastRow = row;
                    }
                }
                // Tính khoảng cách đến biên chiến thắng của Player 2 (hàng 0 và hàng 4)
                int distToTopBorder = firstRow; // Khoảng cách từ ô đầu đến hàng 0
                int distToBottomBorder = (size - 1) - lastRow; // Khoảng cách từ ô cuối đến hàng 4

                // Ưu tiên chặn đầu gần biên chiến thắng hơn
                if (distToBottomBorder <= distToTopBorder) { // Cuối chuỗi gần hàng 4 hơn
                    if (lastRow < size - 1 && board[lastRow + 1][col] == 0) {
                        System.out.println("Chặn cuối chuỗi trên cột (gần biên hàng 4) của Player 1 tại: " + (lastRow + 1) + "," + col);
                        return new int[]{lastRow + 1, col};
                    } else if (firstRow > 0 && board[firstRow - 1][col] == 0) {
                        System.out.println("Chặn đầu chuỗi trên cột của Player 1 tại: " + (firstRow - 1) + "," + col);
                        return new int[]{firstRow - 1, col};
                    }
                } else { // Đầu chuỗi gần hàng 0 hơn
                    if (firstRow > 0 && board[firstRow - 1][col] == 0) {
                        System.out.println("Chặn đầu chuỗi trên cột (gần biên hàng 0) của Player 1 tại: " + (firstRow - 1) + "," + col);
                        return new int[]{firstRow - 1, col};
                    } else if (lastRow < size - 1 && board[lastRow + 1][col] == 0) {
                        System.out.println("Chặn cuối chuỗi trên cột của Player 1 tại: " + (lastRow + 1) + "," + col);
                        return new int[]{lastRow + 1, col};
                    }
                }
            }
        }

        // Tính trước các đường gần thắng của Player 1
        int maxProgressPlayer1 = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 1) {
                    int[] pathProgress = checkPathProgress(r, c, 1, new boolean[size * size]);
                    maxProgressPlayer1 = Math.max(maxProgressPlayer1, pathProgress[1]); // Player 1 quan tâm đến cột
                }
            }
        }

        // Ưu tiên chặn các ô nằm trên đường gần thắng của Player 1
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0 && maxProgressPlayer1 >= size - 3) { // Giảm ngưỡng để nhạy hơn
                    // Giả lập Player 1 đi ở ô này
                    board[row][col] = 1;
                    int progress = 0;
                    for (int r = 0; r < size; r++) {
                        for (int c = 0; c < size; c++) {
                            if (board[r][c] == 1) {
                                int[] pathProgress = checkPathProgress(r, c, 1, new boolean[size * size]);
                                progress = Math.max(progress, pathProgress[1]); // Player 1 quan tâm đến cột
                            }
                        }
                    }
                    board[row][col] = 0;

                    // Nếu ô này giúp Player 1 gần thắng (chỉ thiếu 1 ô)
                    if (progress >= size - 1) {
                        System.out.println("Chặn ô trên đường gần thắng của Player 1 tại: " + row + "," + col);
                        return new int[]{row, col};
                    }
                }
            }
        }

        // Tính trước các đường gần thắng của AI (Player 2)
        int maxProgressPlayer2 = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 2) {
                    int[] pathProgress = checkPathProgress(r, c, 2, new boolean[size * size]);
                    maxProgressPlayer2 = Math.max(maxProgressPlayer2, pathProgress[0]); // Player 2 quan tâm đến hàng
                }
            }
        }

        // Ưu tiên đi vào các ô nằm trên đường gần thắng của Player 2 (AI tự bảo vệ)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0 && maxProgressPlayer2 >= size - 3) { // Giảm ngưỡng để nhạy hơn
                    // Giả lập Player 2 đi ở ô này
                    board[row][col] = 2;
                    int progress = 0;
                    for (int r = 0; r < size; r++) {
                        for (int c = 0; c < size; c++) {
                            if (board[r][c] == 2) {
                                int[] pathProgress = checkPathProgress(r, c, 2, new boolean[size * size]);
                                progress = Math.max(progress, pathProgress[0]); // Player 2 quan tâm đến hàng
                            }
                        }
                    }
                    board[row][col] = 0;

                    // Nếu ô này giúp Player 2 gần thắng (chỉ thiếu 1 ô)
                    if (progress >= size - 1) {
                        System.out.println("Tự bảo vệ: Đi vào ô trên đường gần thắng của Player 2 tại: " + row + "," + col);
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
                    if (adjacentCount >= 2) { // Nếu có từ 2 ô của Player 1 xung quanh
                        System.out.println("Chặn ô nguy hiểm của Player 1 tại: " + row + "," + col);
                        return new int[]{row, col};
                    }
                }
            }
        }

        // Ưu tiên chặn nếu đối thủ (Player 1) có bước tiến tốt
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    // Giả lập Player 1 đi
                    board[row][col] = 1;
                    int score = evaluateBoard(1); // Tính điểm nếu Player 1 đi ở đây
                    board[row][col] = 0;

                    // Nếu điểm tăng mạnh → đối thủ có bước đi tốt → chặn!
                    if (score > 300) { // Giảm ngưỡng để nhạy hơn
                        System.out.println("Chặn trước nước nguy hiểm của Player 1 tại: " + row + "," + col);
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
                    int moveScore = minmax(3, Integer.MIN_VALUE, Integer.MAX_VALUE, false); // Độ sâu 3
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