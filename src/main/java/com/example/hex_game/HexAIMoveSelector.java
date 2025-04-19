package com.example.hex_game;

import java.util.ArrayList;
import java.util.List;

public class HexAIMoveSelector {
    private int size;
    private int[][] board;
    private HexBoard hexBoard;
    private HexAI hexAI;

    public HexAIMoveSelector(int size, int[][] board, HexBoard hexBoard, HexAI hexAI) {
        this.size = size;
        this.board = board;
        this.hexBoard = hexBoard;
        this.hexAI = hexAI;
    }

    public int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE; // Thêm biến này
        int[] bestMove = {-1, -1}; // Thêm biến này

        // Đánh giá xem có bao nhiêu quân của AI (Player 2) trên bàn
        int blueCount = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 2) blueCount++;
            }
        }

        // Nếu là nước đi đầu tiên hoặc thứ hai, chọn vị trí cụ thể gần trung tâm
        if (blueCount == 0) {
            int centerCol = size / 2;
            return new int[]{0, centerCol}; // Đi ở hàng đầu, cột giữa
        }

        if (blueCount == 1) {
            // Tìm nước đi đầu tiên
            int firstRow = -1, firstCol = -1;
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board[r][c] == 2) {
                        firstRow = r;
                        firstCol = c;
                        break;
                    }
                }
                if (firstRow != -1) break;
            }

            // Nước thứ hai nên đi xuống dưới nước đầu tiên
            int newRow = firstRow + 1;
            if (newRow < size && board[newRow][firstCol] == 0) {
                return new int[]{newRow, firstCol};
            }

            // Nếu không thể đi thẳng xuống, thử đi chéo xuống
            if (newRow < size && firstCol - 1 >= 0 && board[newRow][firstCol - 1] == 0) {
                return new int[]{newRow, firstCol - 1};
            }
            if (newRow < size && firstCol + 1 < size && board[newRow][firstCol + 1] == 0) {
                return new int[]{newRow, firstCol + 1};
            }
        }

        // === XÂY DỰNG ĐƯỜNG ĐI CỦA MÌNH (TĂNG ƯU TIÊN) ===

        // 1. Tự thắng nếu có thể
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0 && hexBoard.willWinIfMove(r, c, 2)) {
                    System.out.println("AI thắng tại: " + r + "," + c);
                    return new int[]{r, c};
                }
            }
        }

        // 2. Nối đường đi để tiến về phía dưới
        int[] move = extendPlayer2Progress();
        if (move != null) {
            System.out.println("Mở rộng đường đi từ trên xuống tại: " + move[0] + "," + move[1]);
            return move;
        }

        // 3. Mở rộng chuỗi của mình theo chiều dọc
        move = extendOwnVerticalChains();
        if (move != null) {
            System.out.println("Mở rộng chuỗi dọc tại: " + move[0] + "," + move[1]);
            return move;
        }

        // === PHÒNG THỦ (GIẢM ƯU TIÊN) ===

        // 4. Chặn thắng ngay
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0 && hexBoard.willWinIfMove(r, c, 1)) {
                    System.out.println("Chặn thắng ngay tại: " + r + "," + c);
                    return new int[]{r, c};
                }
            }
        }

        // 5. Chặn chuỗi hàng/cột dài
        move = checkAndBlockChains();
        if (move != null) return move;

        // 6. Chặn tiến độ đường đi của đối thủ
        move = blockPlayer1Progress();
        if (move != null) return move;

        // 7. Chặn ô có nhiều quân địch xung quanh
        move = blockDangerousNeighborhood();
        if (move != null) return move;

        // 8. Ưu tiên nối quân mình gần nhau để mở rộng đường đi (ưu tiên hướng dọc)
        move = connectToOwnStones();
        if (move != null) return move;

        // === DÙNG MINIMAX ===

        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        // Ưu tiên gần giữa + ưu tiên hướng xuống dưới (Player 2 đi từ trên xuống dưới)
        int center = size / 2;
        emptyCells.sort((a, b) -> {
            // Ưu tiên hàng thấp hơn (xuống dưới) và gần trung tâm theo cột
            int da = a[0] * 10 + Math.abs(a[1] - center); // Ưu tiên hàng thấp (a[0] lớn)
            int db = b[0] * 10 + Math.abs(b[1] - center);
            return Integer.compare(db, da); // So sánh ngược để ưu tiên giá trị lớn
        });

        for (int[] cell : emptyCells) {
            int r = cell[0], c = cell[1];
            board[r][c] = 2;
            int score = hexAI.minmax(2, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            board[r][c] = 0;

            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{r, c};
            }
        }

        System.out.println("AI chọn nước bằng Minimax tại: " + bestMove[0] + "," + bestMove[1]);
        return bestMove;
    }

    // Tìm và mở rộng chuỗi dọc (theo chiều đi từ trên xuống)
    private int[] extendOwnVerticalChains() {
        int center = size / 2;

        // Kiểm tra các chuỗi dọc
        for (int c = 0; c < size; c++) {
            int start = -1, len = 0;
            for (int r = 0; r <= size; r++) {
                if (r < size && board[r][c] == 2) {
                    if (start == -1) start = r;
                    len++;
                } else {
                    if (len >= 2) { // Có ít nhất 2 quân liền nhau theo chiều dọc
                        int top = start - 1;
                        int bottom = start + len;

                        boolean topEmpty = top >= 0 && board[top][c] == 0;
                        boolean bottomEmpty = bottom < size && board[bottom][c] == 0;

                        if (bottomEmpty) { // Ưu tiên mở rộng xuống dưới
                            return new int[]{bottom, c};
                        } else if (topEmpty) {
                            return new int[]{top, c};
                        }
                    }
                    start = -1;
                    len = 0;
                }
            }
        }
        return null;
    }

    private int[] checkAndBlockChains() {
        // Trung tâm bàn cờ để tính khoảng cách nếu cần
        int center = size / 2;

        // Kiểm tra hàng (ngang)
        for (int r = 0; r < size; r++) {
            int start = -1, len = 0;
            for (int c = 0; c <= size; c++) {
                if (c < size && board[r][c] == 1) {
                    if (start == -1) start = c;
                    len++;
                } else {
                    if (len >= 3) {
                        int left = start - 1;
                        int right = start + len;

                        boolean leftEmpty = left >= 0 && board[r][left] == 0;
                        boolean rightEmpty = right < size && board[r][right] == 0;

                        if (leftEmpty && rightEmpty) {
                            // Chỉ chặn 1 đầu — chọn đầu nào gần trung tâm hơn
                            if (Math.abs(left - center) <= Math.abs(right - center))
                                return new int[]{r, left};
                            else
                                return new int[]{r, right};
                        } else if (leftEmpty) {
                            return new int[]{r, left};
                        } else if (rightEmpty) {
                            return new int[]{r, right};
                        }
                    }
                    start = -1;
                    len = 0;
                }
            }
        }

        // Kiểm tra cột (dọc)
        for (int c = 0; c < size; c++) {
            int start = -1, len = 0;
            for (int r = 0; r <= size; r++) {
                if (r < size && board[r][c] == 1) {
                    if (start == -1) start = r;
                    len++;
                } else {
                    if (len >= 3) {
                        int top = start - 1;
                        int bottom = start + len;

                        boolean topEmpty = top >= 0 && board[top][c] == 0;
                        boolean bottomEmpty = bottom < size && board[bottom][c] == 0;

                        if (topEmpty && bottomEmpty) {
                            if (Math.abs(top - center) <= Math.abs(bottom - center))
                                return new int[]{top, c};
                            else
                                return new int[]{bottom, c};
                        } else if (topEmpty) {
                            return new int[]{top, c};
                        } else if (bottomEmpty) {
                            return new int[]{bottom, c};
                        }
                    }
                    start = -1;
                    len = 0;
                }
            }
        }

        return null;
    }

    private int[] connectToOwnStones() {
        int[][] dirs = {{1, 0}, {-1, 1}, {1, -1}, {0, -1}, {0, 1}, {-1, 0}}; // Ưu tiên hướng xuống (1,0) và chéo
        int bestRow = -1;
        int bestCol = -1;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 2) { // Quân của AI
                    for (int[] d : dirs) {
                        int nr = r + d[0], nc = c + d[1];
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 0) {
                            // Ưu tiên các ô có hàng cao hơn (đi xuống)
                            if (bestRow == -1 || nr > bestRow) {
                                bestRow = nr;
                                bestCol = nc;
                            }
                        }
                    }
                }
            }
        }

        if (bestRow != -1) {
            return new int[]{bestRow, bestCol};
        }
        return null;
    }

    private int[] blockPlayer1Progress() {
        int maxProgress = 0;
        int[] bestBlockingMove = null;

        // Tìm tiến độ lớn nhất của đối thủ
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 1) {
                    int[] prog = hexAI.checkPathProgress(r, c, 1, new boolean[size * size]);
                    if (prog[1] > maxProgress) {
                        maxProgress = prog[1];
                    }
                }
            }
        }

        // Chỉ chặn khi tiến độ đối thủ đáng kể
        if (maxProgress >= size / 2) {
            int bestProgressReduction = 0;

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (board[r][c] == 0) {
                        board[r][c] = 2; // Thử đặt quân mình ở đây

                        // Tính lại tiến độ đối thủ sau khi đặt quân
                        int newMaxProgress = 0;
                        for (int i = 0; i < size; i++) {
                            for (int j = 0; j < size; j++) {
                                if (board[i][j] == 1) {
                                    int[] p = hexAI.checkPathProgress(i, j, 1, new boolean[size * size]);
                                    newMaxProgress = Math.max(newMaxProgress, p[1]);
                                }
                            }
                        }

                        // Tính lượng giảm tiến độ
                        int progressReduction = maxProgress - newMaxProgress;

                        // Nếu đặt quân ở đây giúp giảm tiến độ đối thủ tốt hơn
                        if (progressReduction > bestProgressReduction) {
                            bestProgressReduction = progressReduction;
                            bestBlockingMove = new int[]{r, c};
                        }

                        board[r][c] = 0; // Trả lại bàn cờ
                    }
                }
            }
        }

        if (bestBlockingMove != null) {
            System.out.println("Chặn tiến độ đối thủ tại: " + bestBlockingMove[0] + "," + bestBlockingMove[1]);
        }
        return bestBlockingMove;
    }

    private int[] extendPlayer2Progress() {
        int maxProgress = 0;
        int[] bestProgressMove = null;

        // Tìm tiến độ lớn nhất cho quân mình
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 2) {
                    int[] p = hexAI.checkPathProgress(r, c, 2, new boolean[size * size]);
                    if (p[0] > maxProgress) {
                        maxProgress = p[0];
                    }
                }
            }
        }

        // Thử các nước đi để tăng tiến độ
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0) {
                    board[r][c] = 2;

                    // Tính lại tiến độ sau khi đặt quân
                    int newMaxProgress = 0;
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            if (board[i][j] == 2) {
                                int[] p = hexAI.checkPathProgress(i, j, 2, new boolean[size * size]);
                                newMaxProgress = Math.max(newMaxProgress, p[0]);
                            }
                        }
                    }

                    // Nếu tiến độ tăng đáng kể
                    if (newMaxProgress > maxProgress + size/4) {
                        maxProgress = newMaxProgress;
                        bestProgressMove = new int[]{r, c};
                    }

                    board[r][c] = 0;
                }
            }
        }

        return bestProgressMove;
    }

    private int[] blockDangerousNeighborhood() {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0) {
                    int count = 0;
                    for (int[] d : dirs) {
                        int nr = r + d[0], nc = c + d[1];
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 1) {
                            count++;
                        }
                    }
                    if (count >= 3) return new int[]{r, c};
                }
            }
        }
        return null;
    }
}
