package com.example.hex_game;

import java.util.ArrayList;
import java.util.List;

public class HexAI {
    public int size;
    public int[][] board;
    public HexBoard hexBoard;
    public int[][] pathProgressCache;

    public HexAI(int size, int[][] board, HexBoard hexBoard) {
        this.size = size;
        this.board = board;
        this.hexBoard = hexBoard;
        this.pathProgressCache = new int[size * size][2];
    }

    public int evaluateBoard(int player) {
        int opponent = (player == 1) ? 2 : 1;
        return evaluatePlayer(player) - (int)(0.8 * evaluatePlayer(opponent));
    }

    // Kiểm tra tiến độ đường đi
    public int[] checkPathProgress(int row, int col, int player, boolean[] visited) {
        int index = row * size + col;
        if (visited[index]) return new int[]{row, col};
        visited[index] = true;

        if (pathProgressCache[index][0] != 0 || pathProgressCache[index][1] != 0) {
            return pathProgressCache[index];
        }

        int furthestRow = row;
        int furthestCol = col;

        if (player == 2 && furthestRow >= size - 1) return new int[]{furthestRow, furthestCol};
        if (player == 1 && furthestCol >= size - 1) return new int[]{furthestRow, furthestCol};

        // Ưu tiên MẠNH MẼ cho hướng xuống dưới với Player 2
        int[][] directions;
        if (player == 2) {
            directions = new int[][] {
                    {1, 0},   // Xuống
                    {1, -1},  // Xuống trái
                    {0, 1},   // Phải
                    {0, -1},  // Trái
                    {-1, 1},  // Lên phải
                    {-1, 0}   // Lên
            };
        } else {
            directions = new int[][] {
                    {0, 1},   // Phải
                    {-1, 1},  // Lên phải
                    {1, -1},  // Xuống trái
                    {1, 0},   // Xuống
                    {-1, 0},  // Lên
                    {0, -1}   // Trái
            };
        }

        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size && board[newRow][newCol] == player) {
                int[] progress = checkPathProgress(newRow, newCol, player, visited);
                furthestRow = Math.max(furthestRow, progress[0]);
                furthestCol = Math.max(furthestCol, progress[1]);
                if (player == 2 && furthestRow >= size - 1) break;
                if (player == 1 && furthestCol >= size - 1) break;
            }
        }

        pathProgressCache[index] = new int[]{furthestRow, furthestCol};
        return pathProgressCache[index];
    }

    public int countConsecutiveOnRow(int row, int player) {
        int count = 0, maxCount = 0;
        for (int col = 0; col < size; col++) {
            if (board[row][col] == player) {
                count++;
                maxCount = Math.max(maxCount, count);
            } else count = 0;
        }
        return maxCount;
    }

    public int countConsecutiveOnCol(int col, int player) {
        int count = 0, maxCount = 0;
        for (int row = 0; row < size; row++) {
            if (board[row][col] == player) {
                count++;
                maxCount = Math.max(maxCount, count);
            } else count = 0;
        }
        return maxCount;
    }

    public int evaluatePlayer(int player) {
        int score = 0;
        boolean[] visited = new boolean[size * size];

        // Kiểm tra thắng
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == player && hexBoard.dfs(r, c, player, visited)) {
                    return 100000; // Trả về giá trị rất cao nếu thắng
                }
            }
        }

        // HOÀN TOÀN THAY ĐỔI CÁCH ĐÁNH GIÁ cho Player 2
        if (player == 2) {
            // ===== CHIẾN LƯỢC ĐI TỪ TRÊN XUỐNG DƯỚI =====

            // 1. Tìm quân ở hàng cao nhất và thấp nhất
            int topRow = size;
            int bottomRow = -1;
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board[r][c] == player) {
                        topRow = Math.min(topRow, r);
                        bottomRow = Math.max(bottomRow, r);
                    }
                }
            }

            // 2. Tính toán tiến độ từ trên xuống dưới
            if (bottomRow >= 0) {
                double verticalProgress = (double)bottomRow / (size - 1);
                score += verticalProgress * 2000; // Điểm RẤT CAO cho tiến độ theo chiều dọc

                // Bonus rất lớn khi gần đến đích
                if (bottomRow >= size-2) {
                    score += 3000;
                } else if (bottomRow >= size-3) {
                    score += 1500;
                }
            }

            // 3. Phạt MẠNH MẼ cho quân đi ngang (theo hàng)
            for (int r = 0; r < size; r++) {
                int horizontalRun = countConsecutiveOnRow(r, player);
                if (horizontalRun > 2) {
                    score -= horizontalRun * 500; // Phạt nặng cho chuỗi ngang
                }
            }

            // 4. Thưởng RẤT CAO cho chuỗi dọc
            for (int c = 0; c < size; c++) {
                int verticalRun = countConsecutiveOnCol(c, player);
                if (verticalRun >= 2) {
                    score += verticalRun * 1000; // Thưởng rất cao cho chuỗi dọc
                }
            }

            // 5. Thưởng cao cho quân ở cột giữa
            int centerCol = size / 2;
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board[r][c] == player) {
                        // Thưởng cho quân gần cột giữa
                        score += (size - Math.abs(c - centerCol)) * 50;

                        // Thưởng LỚN nếu quân ở hàng dưới
                        score += r * 150;
                    }
                }
            }

            // 6. Thưởng cho kết nối giữa quân trên cùng và dưới cùng
            if (topRow < size && bottomRow >= 0 && bottomRow - topRow > 0) {
                // Nếu có kết nối từ trên xuống dưới
                for (int c = 0; c < size; c++) {
                    boolean hasConnectionPath = false;
                    boolean[] pathVisited = new boolean[size * size];
                    for (int r = 0; r < size; r++) {
                        if (board[r][c] == player) {
                            int[] progress = checkPathProgress(r, c, player, pathVisited);
                            if (progress[0] - r >= 2) { // Có đường đi ít nhất 3 ô theo chiều dọc
                                hasConnectionPath = true;
                                break;
                            }
                        }
                    }
                    if (hasConnectionPath) {
                        score += 1000; // Thưởng cao cho đường kết nối
                    }
                }
            }
        } else {
            // Đánh giá cho Player 1 (ít thay đổi)

            // Tìm tiến độ theo chiều ngang
            int maxProgress = 0;
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board[r][c] == player) {
                        int[] progress = checkPathProgress(r, c, player, new boolean[size * size]);
                        maxProgress = Math.max(maxProgress, progress[1]);
                    }
                }
            }

            score += maxProgress * 300;

            // Đánh giá đường thẳng liên tục
            for (int row = 0; row < size; row++) {
                int consecutive = countConsecutiveOnRow(row, player);
                if (consecutive >= size / 2) score += consecutive * 100;
                else if (consecutive >= 2) score += consecutive * 40;
            }
        }

        return score;
    }

    public int getHexCellValue(int row, int col, int player) {
        int score = 0;
        int center = size / 2;

        if (player == 2) {
            // Phạt cho các ô ở hàng trên
            if (row < size / 3) {
                score -= 500;
            }

            // Thưởng cao cho các ô hàng dưới
            if (row >= size * 2 / 3) {
                score += 500;
            }

            // Ưu tiên các ô ở cột giữa
            score += (size - Math.abs(col - center)) * 30;
        } else {
            // Logic cho Player 1 (ít thay đổi)
            score += (size - Math.abs(col - center)) * 10;
            score += (size - Math.abs(row - center)) * 10;
        }

        return score;
    }

    public int minmax(int depth, int alpha, int beta, boolean isMaximizing) {
        // Kiểm tra nhanh xem đã có ai thắng chưa
        boolean player1Win = checkPlayerWin(1);
        boolean player2Win = checkPlayerWin(2);

        if (player2Win) return 10000;
        if (player1Win) return -10000;
        if (depth == 0) return evaluateBoard(2);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            // Ưu tiên đi theo chiều dọc
            List<int[]> sortedMoves = new ArrayList<>();
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (board[row][col] == 0) {
                        sortedMoves.add(new int[]{row, col});
                    }
                }
            }

            // Sắp xếp nước đi theo ưu tiên: hàng cao trước (gần dưới)
            sortedMoves.sort((a, b) -> Integer.compare(b[0], a[0]));

            for (int[] move : sortedMoves) {
                int row = move[0], col = move[1];
                board[row][col] = 2;
                int eval = minmax(depth - 1, alpha, beta, false);
                board[row][col] = 0;
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (board[row][col] == 0) {
                        board[row][col] = 1;
                        int eval = minmax(depth - 1, alpha, beta, true);
                        board[row][col] = 0;
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) break;
                    }
                }
            }
            return minEval;
        }
    }

    // Hàm kiểm tra win
    private boolean checkPlayerWin(int player) {
        boolean[] visited = new boolean[size * size];

        if (player == 1) {
            // Kiểm tra từ cột trái sang phải
            for (int r = 0; r < size; r++) {
                if (board[r][0] == player && hexBoard.dfs(r, 0, player, visited)) {
                    return true;
                }
            }
        } else {
            // Kiểm tra từ hàng trên xuống dưới
            for (int c = 0; c < size; c++) {
                if (board[0][c] == player && hexBoard.dfs(0, c, player, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int[] getBestMove() {
        return new HexAIMoveSelector(size, board, hexBoard, this).findBestMove();
    }

    public void aiMove() {
        int[] move = getBestMove();
        if (move[0] != -1) {
            hexBoard.makeMove(move[0], move[1], 2);
            System.out.println("AI moved at: " + move[0] + ", " + move[1]);
        }
    }
}
