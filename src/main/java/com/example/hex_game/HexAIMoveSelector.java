package com.example.hex_game;

import java.util.*;

public class HexAIMoveSelector {
    private int size;
    private int[][] board;
    private HexBoard hexBoard;
    private HexAI hexAI;

    // Thêm biến thành viên để lưu nước đi gần đây
    private int[] lastMove = null;

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
            lastMove = new int[]{0, centerCol}; // Lưu nước đi đầu tiên
            return lastMove; // Đi ở hàng đầu, cột giữa
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
                lastMove = new int[]{newRow, firstCol};
                return lastMove;
            }

            // Nếu không thể đi thẳng xuống, thử đi chéo xuống
            if (newRow < size && firstCol - 1 >= 0 && board[newRow][firstCol - 1] == 0) {
                lastMove = new int[]{newRow, firstCol - 1};
                return lastMove;
            }
            if (newRow < size && firstCol + 1 < size && board[newRow][firstCol + 1] == 0) {
                lastMove = new int[]{newRow, firstCol + 1};
                return lastMove;
            }
        }

        // === XÂY DỰNG ĐƯỜNG ĐI CỦA MÌNH (TĂNG ƯU TIÊN) ===

        // 1. Tự thắng nếu có thể
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0 && hexBoard.willWinIfMove(r, c, 2)) {
                    System.out.println("AI thắng tại: " + r + "," + c);
                    lastMove = new int[]{r, c};
                    return lastMove;
                }
            }
        }

        // 2. Nối đường đi để tiến về phía dưới
        int[] move = extendPlayer2Progress();
        if (move != null) {
            System.out.println("Mở rộng đường đi từ trên xuống tại: " + move[0] + "," + move[1]);
            lastMove = move;
            return move;
        }

        // 3. Mở rộng chuỗi của mình theo chiều dọc
        move = extendOwnVerticalChains();
        if (move != null) {
            System.out.println("Mở rộng chuỗi dọc tại: " + move[0] + "," + move[1]);
            lastMove = move;
            return move;
        }

        // === PHÒNG THỦ (GIẢM ƯU TIÊN) ===

        // 4. Chặn thắng ngay
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0 && hexBoard.willWinIfMove(r, c, 1)) {
                    System.out.println("Chặn thắng ngay tại: " + r + "," + c);
                    lastMove = new int[]{r, c};
                    return lastMove;
                }
            }
        }

        // 5. Chặn chuỗi hàng/cột dài
        move = checkAndBlockChains();
        if (move != null) {
            lastMove = move;
            return move;
        }

        // 6. Chặn tiến độ đường đi của đối thủ
        move = blockPlayer1Progress();
        if (move != null) {
            lastMove = move;
            return move;
        }

        // 7. Chặn ô có nhiều quân địch xung quanh
        move = blockDangerousNeighborhood();
        if (move != null) {
            lastMove = move;
            return move;
        }

        // 8. Ưu tiên nối quân mình gần nhau để mở rộng đường đi (ưu tiên hướng dọc)
        move = connectToOwnStones();
        if (move != null) {
            lastMove = move;
            return move;
        }

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
        lastMove = bestMove;
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

        // Các biến để lưu nước đi tốt nhất
        int bestRow = -1;
        int bestCol = -1;
        int bestConnectionValue = -1;

        // Tìm tất cả các cụm quân hiện có
        List<Set<Integer>> existingGroups = findExistingGroups(2);

        // Xét tất cả các ô trống
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0) { // Ô trống
                    // Kiểm tra các ô kề
                    Set<Integer> connectedGroups = new HashSet<>();
                    int nearbyStones = 0;

                    for (int[] d : dirs) {
                        int nr = r + d[0], nc = c + d[1];
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 2) {
                            nearbyStones++;

                            // Xác định ô này thuộc nhóm nào
                            for (int i = 0; i < existingGroups.size(); i++) {
                                if (existingGroups.get(i).contains(nr * size + nc)) {
                                    connectedGroups.add(i);
                                    break;
                                }
                            }
                        }
                    }

                    // Tính điểm giá trị kết nối
                    int connectionValue = 0;

                    // 1. Số lượng nhóm kết nối (càng nhiều càng tốt)
                    connectionValue += connectedGroups.size() * 100;

                    // 2. Số lượng quân kề (càng nhiều càng tốt)
                    connectionValue += nearbyStones * 10;

                    // 3. Ưu tiên hàng thấp (càng xuống dưới càng tốt)
                    connectionValue += r * 5;

                    // 4. Ưu tiên các ô ở giữa theo chiều ngang
                    int centerCol = size / 2;
                    connectionValue += (size - Math.abs(c - centerCol)) * 2;

                    // Cập nhật nước đi tốt nhất
                    if (connectionValue > bestConnectionValue) {
                        bestConnectionValue = connectionValue;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }

        if (bestRow != -1) {
            return new int[]{bestRow, bestCol};
        }
        return null;
    }

    // Hàm tìm các cụm quân hiện có (mỗi cụm là một tập hợp các vị trí kết nối với nhau)
    private List<Set<Integer>> findExistingGroups(int player) {
        List<Set<Integer>> groups = new ArrayList<>();
        boolean[] visited = new boolean[size * size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == player && !visited[r * size + c]) {
                    Set<Integer> group = new HashSet<>();
                    collectConnectedStones(r, c, player, visited, group);
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    // Hàm DFS để thu thập tất cả các quân kết nối
    private void collectConnectedStones(int r, int c, int player, boolean[] visited, Set<Integer> group) {
        int index = r * size + c;
        if (visited[index] || board[r][c] != player) return;

        visited[index] = true;
        group.add(index);

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, -1}, {-1, 1}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                collectConnectedStones(nr, nc, player, visited, group);
            }
        }
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
        // Kiểm tra xem có thể tiếp tục đường đi hiện tại không
        if (lastMove != null) {
            int lastRow = lastMove[0];
            int lastCol = lastMove[1];

            // Kiểm tra xem có thể đi xuống dưới từ nước đi gần đây không
            if (lastRow + 1 < size && board[lastRow + 1][lastCol] == 0) {
                return new int[]{lastRow + 1, lastCol};
            }

            // Nếu bị chặn, thử đi ngang 1 bước
            if (lastRow + 1 < size && board[lastRow + 1][lastCol] != 0) {
                int[] dirs = {-1, 1}; // Trái và phải

                // Ưu tiên hướng về phía trung tâm
                int center = size / 2;
                if (lastCol < center) {
                    dirs = new int[]{1, -1}; // Ưu tiên phải
                } else {
                    dirs = new int[]{-1, 1}; // Ưu tiên trái
                }

                for (int dir : dirs) {
                    int newCol = lastCol + dir;
                    if (newCol >= 0 && newCol < size && board[lastRow][newCol] == 0) {
                        // Kiểm tra xem có thể đi xuống từ vị trí ngang này không
                        if (lastRow + 1 < size && board[lastRow + 1][newCol] == 0) {
                            return new int[]{lastRow, newCol}; // Đi ngang 1 bước
                        } else {
                            // Ít nhất đi ngang được, lưu lại để xem xét sau
                            int[] sideMove = {lastRow, newCol};

                            // Tìm kiếm nước đi tốt hơn trước khi trả về
                            if (!hasDownwardPath()) {
                                return sideMove;
                            }
                        }
                    }
                }
            }
        }

        // Nếu không thể tiếp tục từ nước đi gần đây, tìm kiếm tất cả các vị trí bị chặn
        List<int[]> blockedPositions = new ArrayList<>();

        // Xác định vị trí có tiến độ nhưng bị chặn
        for (int c = 0; c < size; c++) {
            int maxRow = -1;

            // Tìm quân xa nhất trong cột này
            for (int r = 0; r < size; r++) {
                if (board[r][c] == 2) {
                    maxRow = r;
                }
            }

            // Kiểm tra xem ô tiếp theo có bị chặn không
            if (maxRow != -1 && maxRow + 1 < size && board[maxRow + 1][c] == 1) {
                blockedPositions.add(new int[]{maxRow, c});
            }
        }

        // Đánh giá tất cả các đường vòng có thể - CHỈ ĐI NGANG 1 BƯỚC
        for (int[] blocked : blockedPositions) {
            int r = blocked[0];
            int c = blocked[1];

            // Kiểm tra đường vòng CHỈ 1 bước sang ngang
            int[] dirs = {-1, 1}; // Trái và phải

            // Ưu tiên hướng về phía trung tâm bàn cờ
            int center = size / 2;
            if (c < center) {
                dirs = new int[]{1, -1}; // Ưu tiên phải
            } else {
                dirs = new int[]{-1, 1}; // Ưu tiên trái
            }

            for (int dir : dirs) {
                int newCol = c + dir; // CHỈ đi ngang 1 bước

                // Xác thực vị trí
                if (newCol >= 0 && newCol < size && board[r][newCol] == 0) {
                    // Kiểm tra ngay xem có thể đi xuống được sau khi đi ngang không
                    if (r + 1 < size && board[r + 1][newCol] == 0) {
                        // Có thể đi ngang 1 bước và đi xuống ngay => đây là lựa chọn tốt nhất
                        return new int[]{r, newCol};
                    } else {
                        // Có thể đi ngang nhưng không thể đi xuống ngay
                        // Để đây là phương án dự phòng
                        int[] sideMove = {r, newCol};

                        // Tìm kiếm nước đi tốt hơn trước khi trả về nước đi này
                        boolean foundBetterMove = false;
                        for (int[] otherBlocked : blockedPositions) {
                            if (otherBlocked != blocked) { // Kiểm tra các vị trí bị chặn khác
                                int otherR = otherBlocked[0];
                                int otherC = otherBlocked[1];

                                for (int otherDir : dirs) {
                                    int otherNewCol = otherC + otherDir;
                                    if (otherNewCol >= 0 && otherNewCol < size && board[otherR][otherNewCol] == 0) {
                                        if (otherR + 1 < size && board[otherR + 1][otherNewCol] == 0) {
                                            // Tìm thấy nước đi tốt hơn ở vị trí khác
                                            foundBetterMove = true;
                                            break;
                                        }
                                    }
                                }
                                if (foundBetterMove) break;
                            }
                        }

                        if (!foundBetterMove) {
                            return sideMove; // Trả về nước đi ngang này nếu không có nước đi tốt hơn
                        }
                    }
                }
            }
        }

        // Không tìm thấy đường vòng ngang 1 bước, sử dụng chiến lược mặc định
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

    private boolean hasDownwardPath() {
        for (int c = 0; c < size; c++) {
            int maxRow = -1;

            // Tìm quân xa nhất trong cột này
            for (int r = 0; r < size; r++) {
                if (board[r][c] == 2) {
                    maxRow = r;
                }
            }

            // Kiểm tra xem ô tiếp theo có thể đi xuống không
            if (maxRow != -1 && maxRow + 1 < size && board[maxRow + 1][c] == 0) {
                return true; // Có đường đi xuống tốt hơn
            }
        }

        return false; // Không có đường đi xuống nào tốt hơn
    }


    // Hàm tính điểm cho đường vòng
    private int calculateDetourScore(int row, int col) {
        int score = 0;

        // Kiểm tra có thể đi xuống bao nhiêu bước
        int downCount = 0;
        for (int r = row + 1; r < size; r++) {
            if (board[r][col] == 0) {
                downCount++;
            } else if (board[r][col] == 1) {
                // Trừ điểm nếu gặp quân đối thủ (đường đi sẽ bị chặn sớm)
                score -= 5;
                break;
            } else {
                // Gặp quân mình (tốt vì có thể kết nối)
                score += 3;
                break;
            }
        }
        score += downCount * 10; // Đánh giá cao khả năng đi xuống

        // Trọng số cho vị trí gần trung tâm
        int centerCol = size / 2;
        score += (size - Math.abs(col - centerCol)) * 3;

        // Kiểm tra xem nước đi này có thể kết nối hai nhóm quân riêng biệt không
        int connectedGroups = 0;
        Set<Integer> groups = new HashSet<>();
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, -1}, {-1, 1}};
        for (int[] dir : dirs) {
            int nr = row + dir[0];
            int nc = col + dir[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 2) {
                // Tìm nhóm của quân này
                boolean[] visited = new boolean[size * size];
                Set<Integer> connectedPositions = new HashSet<>();
                collectConnectedStones(nr, nc, 2, visited, connectedPositions);

                // Thêm nhóm mới nếu chưa có
                if (!connectedPositions.isEmpty()) {
                    boolean isNewGroup = true;
                    for (Integer groupId : groups) {
                        if (connectedPositions.contains(groupId)) {
                            isNewGroup = false;
                            break;
                        }
                    }
                    if (isNewGroup) {
                        connectedGroups++;
                        groups.addAll(connectedPositions);
                    }
                }

                // Cộng điểm cho mỗi quân kề
                score += 5;
            }
        }

        // Thưởng lớn nếu kết nối được nhiều nhóm
        score += connectedGroups * 15;

        // Kiểm tra xem nước đi này có tạo đường thẳng xuống đáy bàn cờ không
        boolean hasPathToBottom = false;
        boolean[] visited = new boolean[size * size];
        visited[row * size + col] = true; // Giả sử ô hiện tại đã được đánh

        // Thử DFS từ ô này xuống dưới
        if (canReachBottom(row, col, visited)) {
            hasPathToBottom = true;
            score += 50; // Thưởng lớn nếu tạo được đường thẳng xuống đáy
        }

        return score;
    }

    // Kiểm tra xem từ vị trí (row, col) có thể đi xuống đáy bàn cờ không
    private boolean canReachBottom(int row, int col, boolean[] visited) {
        // Đã đến đáy bàn cờ
        if (row == size - 1) return true;

        int[][] dirs = {{1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}};
        for (int[] dir : dirs) {
            int nr = row + dir[0];
            int nc = col + dir[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size &&
                    !visited[nr * size + nc] && board[nr][nc] != 1) {
                visited[nr * size + nc] = true;
                if (canReachBottom(nr, nc, visited)) {
                    return true;
                }
            }
        }
        return false;
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
