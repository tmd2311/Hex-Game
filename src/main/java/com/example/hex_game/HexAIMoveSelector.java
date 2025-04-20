package com.example.hex_game;

import java.util.*;

public class HexAIMoveSelector {
    private int size;
    private int[][] board;
    private HexBoard hexBoard;
    private HexAI hexAI;
    private int[] lastMove = null;
    private final int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};

    public HexAIMoveSelector(int size, int[][] board, HexBoard hexBoard, HexAI hexAI) {
        this.size = size;
        this.board = board;
        this.hexBoard = hexBoard;
        this.hexAI = hexAI;
    }

    public int[] findBestMove() {
        // Đếm số quân của Xanh (Player 2)
        int blueCount = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 2) blueCount++;
            }
        }

        // Nước đi đầu tiên hoặc thứ hai: gần trung tâm
        if (blueCount == 0) {
            int centerCol = size / 2;
            lastMove = new int[]{0, centerCol};
            return lastMove;
        }
        if (blueCount == 1) {
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
            int newRow = firstRow + 1;
            if (newRow < size && board[newRow][firstCol] == 0) {
                lastMove = new int[]{newRow, firstCol};
                return lastMove;
            }
            if (newRow < size && firstCol - 1 >= 0 && board[newRow][firstCol - 1] == 0) {
                lastMove = new int[]{newRow, firstCol - 1};
                return lastMove;
            }
            if (newRow < size && firstCol + 1 < size && board[newRow][firstCol + 1] == 0) {
                lastMove = new int[]{newRow, firstCol + 1};
                return lastMove;
            }
        }

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

        // 2. Chặn Đỏ thắng ngay
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0 && hexBoard.willWinIfMove(r, c, 1)) {
                    System.out.println("Chặn thắng ngay tại: " + r + "," + c);
                    lastMove = new int[]{r, c};
                    return lastMove;
                }
            }
        }

        // 3. Chặn đường ngắn nhất của Đỏ
        int[] blockMove = blockPlayer1Path();
        if (blockMove != null) {
            System.out.println("Chặn đường Đỏ tại: " + blockMove[0] + "," + blockMove[1]);
            lastMove = blockMove;
            return blockMove;
        }

        // 4. Mở rộng đường trên-dưới của Xanh
        int[] extendMove = extendPlayer2Path();
        if (extendMove != null) {
            System.out.println("Mở rộng đường Xanh tại: " + extendMove[0] + "," + extendMove[1]);
            lastMove = extendMove;
            return extendMove;
        }

        // 5. Tạo cầu nối cho Xanh
        int[] bridgeMove = createBridge();
        if (bridgeMove != null) {
            System.out.println("Tạo cầu nối tại: " + bridgeMove[0] + "," + bridgeMove[1]);
            lastMove = bridgeMove;
            return bridgeMove;
        }

        // 6. Dùng MCTS làm phương án dự phòng
        int[] mctsMove = hexAI.getBestMoveMCTS();
        if (mctsMove[0] != -1) {
            System.out.println("MCTS chọn tại: " + mctsMove[0] + "," + mctsMove[1]);
            lastMove = mctsMove;
            return mctsMove;
        }

        // Nếu không tìm thấy nước đi
        System.out.println("Không tìm thấy nước đi hợp lệ!");
        return new int[]{-1, -1};
    }

    // Tính đường ngắn nhất bằng Dijkstra
    private int getDijkstraPathCost(int player, List<int[]> pathCells) {
        int[][] dist = new int[size][size];
        int[][] prevRow = new int[size][size];
        int[][] prevCol = new int[size][size];
        boolean[][] visited = new boolean[size][size];
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        for (int i = 0; i < size; i++) {
            Arrays.fill(dist[i], Integer.MAX_VALUE);
            Arrays.fill(prevRow[i], -1);
            Arrays.fill(prevCol[i], -1);
        }

        if (player == 2) {
            for (int col = 0; col < size; col++) {
                if (board[0][col] == player || board[0][col] == 0) {
                    dist[0][col] = (board[0][col] == player) ? 0 : 1;
                    pq.offer(new Node(0, col, dist[0][col]));
                }
            }
        } else {
            for (int row = 0; row < size; row++) {
                if (board[row][0] == player || board[row][0] == 0) {
                    dist[row][0] = (board[row][0] == player) ? 0 : 1;
                    pq.offer(new Node(row, 0, dist[row][0]));
                }
            }
        }

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int r = current.row, c = current.col;
            if (visited[r][c]) continue;
            visited[r][c] = true;

            for (int[] dir : direction) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr >= 0 && nr < size && nc >= 0 && nc < size && !visited[nr][nc]) {
                    int cost = (board[nr][nc] == player) ? 0 : (board[nr][nc] == 0 ? 1 : 1000);
                    if (dist[r][c] + cost < dist[nr][nc]) {
                        dist[nr][nc] = dist[r][c] + cost;
                        prevRow[nr][nc] = r;
                        prevCol[nr][nc] = c;
                        pq.offer(new Node(nr, nc, dist[nr][nc]));
                    }
                }
            }
        }

        int minCost = Integer.MAX_VALUE;
        int endRow = -1, endCol = -1;
        if (player == 1) {
            for (int row = 0; row < size; row++) {
                if (dist[row][size - 1] < minCost) {
                    minCost = dist[row][size - 1];
                    endRow = row;
                    endCol = size - 1;
                }
            }
        } else {
            for (int col = 0; col < size; col++) {
                if (dist[size - 1][col] < minCost) {
                    minCost = dist[size - 1][col];
                    endRow = size - 1;
                    endCol = col;
                }
            }
        }

        if (pathCells != null && endRow != -1) {
            pathCells.clear();
            int r = endRow, c = endCol;
            while (r != -1 && c != -1 && dist[r][c] != Integer.MAX_VALUE) {
                pathCells.add(new int[]{r, c});
                int pr = prevRow[r][c], pc = prevCol[r][c];
                r = pr;
                c = pc;
            }
        }

        return minCost;
    }

    // Chặn đường ngắn nhất của Đỏ
    private int[] blockPlayer1Path() {
        List<int[]> opponentPath = new ArrayList<>();
        int opponentDist = getDijkstraPathCost(1, opponentPath);
        if (opponentDist < size / 1.5) { // Nếu Đỏ sắp thắng
            for (int[] cell : opponentPath) {
                if (board[cell[0]][cell[1]] == 0) {
                    return new int[]{cell[0], cell[1]};
                }
            }
            for (int[] cell : opponentPath) {
                for (int[] dir : direction) {
                    int nr = cell[0] + dir[0], nc = cell[1] + dir[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 0) {
                        return new int[]{nr, nc};
                    }
                }
            }
        }
        return null;
    }

    // Mở rộng đường trên-dưới của Xanh
    private int[] extendPlayer2Path() {
        List<int[]> playerPath = new ArrayList<>();
        getDijkstraPathCost(2, playerPath);
        for (int[] cell : playerPath) {
            if (board[cell[0]][cell[1]] == 0) {
                return new int[]{cell[0], cell[1]};
            }
        }
        for (int[] cell : playerPath) {
            for (int[] dir : direction) {
                int nr = cell[0] + dir[0], nc = cell[1] + dir[1];
                if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 0) {
                    return new int[]{nr, nc};
                }
            }
        }
        return null;
    }

    // Tạo cầu nối cho Xanh
    private int[] createBridge() {
        int bestRow = -1, bestCol = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] != 0) continue;
                int score = 0;
                int connections = 0;
                for (int[] dir : direction) {
                    int nr = row + dir[0], nc = col + dir[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size && board[nr][nc] == 2) {
                        connections++;
                    }
                }
                if (connections >= 2) {
                    score += connections * 100;
                    score += row * 50; // Ưu tiên hàng thấp (xuống dưới)
                    int centerCol = size / 2;
                    score += (size - Math.abs(col - centerCol)) * 20;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        if (bestRow != -1) {
            return new int[]{bestRow, bestCol};
        }
        return null;
    }

    private static class Node {
        int row, col, cost;
        Node(int row, int col, int cost) {
            this.row = row;
            this.col = col;
            this.cost = cost;
        }
    }
}