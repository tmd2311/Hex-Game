package com.example.hex_game;

import java.util.*;

public class HexAI {
    public int size;
    public int[][] board;
    public HexBoard hexBoard;
    private final int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
    private Map<String, Integer> transpositionTable;
    private Map<String, Integer> dijkstraCache;
    private Map<String, double[]> raveStats; // Lưu [reward, visits] cho RAVE
    private Random random = new Random();
    private static final int SIMULATION_LIMIT = 500; // Đủ để tính trong 3 giây
    private static final double EXPLORATION_CONSTANT = 0.7; // UCB1 constant
    private static final double RAVE_BETA = 0.3; // Trọng số RAVE

    public HexAI(int size, int[][] board, HexBoard hexBoard) {
        this.size = size;
        this.board = board;
        this.hexBoard = hexBoard;
        this.transpositionTable = new HashMap<>();
        this.dijkstraCache = new HashMap<>();
        this.raveStats = new HashMap<>();
    }

    // Kiểm tra người chơi có thắng không
    private boolean hasPlayerWonSim(int[][] simBoard, int player) {
        boolean[] visited = new boolean[size * size];
        for (int i = 0; i < size; i++) {
            if (player == 1 && simBoard[i][0] == 1 && dfsSim(i, 0, 1, simBoard, visited)) return true;
            if (player == 2 && simBoard[0][i] == 2 && dfsSim(0, i, 2, simBoard, visited)) return true;
        }
        return false;
    }

    private boolean dfsSim(int row, int col, int player, int[][] simBoard, boolean[] visited) {
        int index = row * size + col;
        if (visited[index]) return false;
        visited[index] = true;
        if (player == 1 && col == size - 1) return true; // Đỏ đến cột 10
        if (player == 2 && row == size - 1) return true; // Xanh đến hàng 10

        for (int[] dir : direction) {
            int nr = row + dir[0], nc = col + dir[1];
            if (nr >= 0 && nr < size && nc >= 0 && nc < size && simBoard[nr][nc] == player) {
                if (dfsSim(nr, nc, player, simBoard, visited)) return true;
            }
        }
        return false;
    }

    // Tính đường ngắn nhất bằng Dijkstra
    private int getDijkstraPathCost(int player, int[][] simBoard, List<int[]> pathCells) {
        StringBuilder sb = new StringBuilder();
        sb.append(player).append(":");
        for (int[] row : simBoard) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        String stateKey = sb.toString();
        if (dijkstraCache.containsKey(stateKey)) {
            return dijkstraCache.get(stateKey);
        }

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

        if (player == 2) { // Xanh: trên-dưới
            for (int col = 0; col < size; col++) {
                if (simBoard[0][col] == player || simBoard[0][col] == 0) {
                    dist[0][col] = (simBoard[0][col] == player) ? 0 : 1;
                    pq.offer(new Node(0, col, dist[0][col]));
                }
            }
        } else { // Đỏ: trái-phải
            for (int row = 0; row < size; row++) {
                if (simBoard[row][0] == player || simBoard[row][0] == 0) {
                    dist[row][0] = (simBoard[row][0] == player) ? 0 : 1;
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
                    int cost = (simBoard[nr][nc] == player) ? 0 : (simBoard[nr][nc] == 0 ? 1 : 1000);
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

        dijkstraCache.put(stateKey, minCost);
        return minCost;
    }

    // Nhận diện cầu nối
    private int detectBridges(int player, int[][] simBoard) {
        int bridges = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (simBoard[row][col] != player) continue;
                for (int[] dir : direction) {
                    int nr = row + dir[0], nc = col + dir[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size && simBoard[nr][nc] == 0) {
                        for (int[] dir2 : direction) {
                            int nnr = nr + dir2[0], nnc = nc + dir2[1];
                            if (nnr >= 0 && nnr < size && nnc >= 0 && nnc < size && simBoard[nnr][nnc] == player) {
                                bridges++;
                            }
                        }
                    }
                }
            }
        }
        return bridges;
    }

    // Đánh giá bàn cờ cải tiến
    private int evaluateBoard(int[][] simBoard, int player) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : simBoard) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        String stateKey = sb.toString();
        if (transpositionTable.containsKey(stateKey)) {
            return transpositionTable.get(stateKey);
        }

        if (hasPlayerWonSim(simBoard, player)) return 100000;
        if (hasPlayerWonSim(simBoard, 3 - player)) return -100000;

        List<int[]> playerPath = new ArrayList<>();
        List<int[]> opponentPath = new ArrayList<>();
        int playerDist = getDijkstraPathCost(player, simBoard, playerPath);
        int opponentDist = getDijkstraPathCost(3 - player, simBoard, opponentPath);

        int score = -playerDist * 400 + opponentDist * 500;
        score += (opponentDist - playerDist) * 200;

        for (int[] cell : opponentPath) {
            if (simBoard[cell[0]][cell[1]] == player) score += 700;
            if (simBoard[cell[0]][cell[1]] == 3 - player) score -= 300;
        }
        for (int[] cell : playerPath) {
            if (simBoard[cell[0]][cell[1]] == player) score += 200;
        }

        score += detectBridges(player, simBoard) * 150;
        score -= detectBridges(3 - player, simBoard) * 100;

        if (player == 2) {
            for (int col = 0; col < size; col++) {
                if (simBoard[0][col] == 2 || simBoard[size - 1][col] == 2) score += 200;
                if (simBoard[0][col] == 1 || simBoard[size - 1][col] == 1) score -= 200;
            }
        } else {
            for (int row = 0; row < size; row++) {
                if (simBoard[row][0] == 1 || simBoard[row][size - 1] == 1) score += 200;
                if (simBoard[row][0] == 2 || simBoard[row][size - 1] == 2) score -= 200;
            }
        }

        transpositionTable.put(stateKey, score);
        return score;
    }

    // Lấy các nước thí sinh tiềm năng
    private List<int[]> getCandidateMoves(int[][] currentBoard) {
        Set<String> seen = new HashSet<>();
        List<int[]> candidates = new ArrayList<>();
        List<int[]> playerPath = new ArrayList<>();
        List<int[]> opponentPath = new ArrayList<>();
        int opponentDist = getDijkstraPathCost(1, currentBoard, opponentPath);
        getDijkstraPathCost(2, currentBoard, playerPath);

        int emptyCells = 0;
        for (int[] row : currentBoard) {
            for (int cell : row) {
                if (cell == 0) emptyCells++;
            }
        }
        if (emptyCells > size * size * 0.9) {
            int center = size / 2;
            if (currentBoard[center][center] == 0) {
                candidates.add(new int[]{center, center});
                return candidates;
            }
            for (int[] dir : direction) {
                int nr = center + dir[0], nc = center + dir[1];
                if (nr >= 0 && nr < size && nc >= 0 && nc < size && currentBoard[nr][nc] == 0) {
                    candidates.add(new int[]{nr, nc});
                    return candidates;
                }
            }
        }

        if (opponentDist < size / 1.5) {
            for (int[] cell : opponentPath) {
                int row = cell[0], col = cell[1];
                if (currentBoard[row][col] == 0) {
                    String key = row + "," + col;
                    if (seen.add(key)) candidates.add(new int[]{row, col});
                }
                for (int[] dir : direction) {
                    int nr = row + dir[0], nc = col + dir[1];
                    if (nr >= 0 && nr < size && nc >= 0 && nc < size && currentBoard[nr][nc] == 0) {
                        String key = nr + "," + nc;
                        if (seen.add(key)) candidates.add(new int[]{nr, nc});
                        if (candidates.size() >= 3) break;
                    }
                }
                if (candidates.size() >= 3) break;
            }
        }

        for (int[] cell : playerPath) {
            int row = cell[0], col = cell[1];
            if (currentBoard[row][col] == 0) {
                String key = row + "," + col;
                if (seen.add(key)) candidates.add(new int[]{row, col});
            }
            for (int[] dir : direction) {
                int nr = row + dir[0], nc = col + dir[1];
                if (nr >= 0 && nr < size && nc >= 0 && nc < size && currentBoard[nr][nc] == 0) {
                    String key = nr + "," + nc;
                    if (seen.add(key)) candidates.add(new int[]{nr, nc});
                    if (candidates.size() >= 6) break;
                }
            }
            if (candidates.size() >= 6) break;
        }

        if (candidates.isEmpty()) {
            for (int col = 0; col < size; col++) {
                if (currentBoard[0][col] == 0) {
                    String key = "0," + col;
                    if (seen.add(key)) {
                        candidates.add(new int[]{0, col});
                        if (candidates.size() >= 2) break;
                    }
                }
                if (currentBoard[size - 1][col] == 0) {
                    String key = (size - 1) + "," + col;
                    if (seen.add(key)) {
                        candidates.add(new int[]{size - 1, col});
                        if (candidates.size() >= 2) break;
                    }
                }
            }
        }

        return candidates;
    }

    // MCTS Node
    private class MCTSNode {
        int[] move;
        int player;
        int visits;
        double totalReward;
        int raveVisits;
        double raveReward;
        List<MCTSNode> children;
        MCTSNode parent;

        MCTSNode(int[] move, int player, MCTSNode parent) {
            this.move = move;
            this.player = player;
            this.visits = 0;
            this.totalReward = 0;
            this.raveVisits = 0;
            this.raveReward = 0;
            this.children = new ArrayList<>();
            this.parent = parent;
        }

        double getUCB1WithRAVE() {
            if (visits == 0) return Double.MAX_VALUE;
            double ucb1 = totalReward / visits;
            double rave = raveVisits > 0 ? raveReward / raveVisits : 0;
            double beta = RAVE_BETA * raveVisits / (visits + raveVisits + 1e-5);
            double combined = (1 - beta) * ucb1 + beta * rave;
            double exploration = EXPLORATION_CONSTANT * Math.sqrt(Math.log(parent.visits + 1) / (visits + 1));
            return combined + exploration;
        }
    }

    public int[] getBestMoveMCTS() {
        MCTSNode root = new MCTSNode(null, 2, null);
        int simulations = 0;

        while (simulations < SIMULATION_LIMIT) {
            MCTSNode node = select(root);
            double reward = simulate(node);
            backpropagate(node, reward);
            simulations++;
        }

        MCTSNode bestChild = root.children.stream()
            .max(Comparator.comparingInt(n -> n.visits))
            .orElse(null);

        return bestChild != null ? bestChild.move : new int[]{-1, -1};
    }

    private MCTSNode select(MCTSNode node) {
        while (!node.children.isEmpty()) {
            node = node.children.stream()
                .max(Comparator.comparingDouble(MCTSNode::getUCB1WithRAVE))
                .orElse(node.children.get(0));
        }

        if (node.visits > 0) {
            expand(node);
            node = node.children.isEmpty() ? node : node.children.get(0);
        }

        return node;
    }

    private void expand(MCTSNode node) {
        if (hasPlayerWonSim(board, 1) || hasPlayerWonSim(board, 2)) return;

        List<int[]> moves = getCandidateMoves(board);
        int nextPlayer = (node.player == 1) ? 2 : 1;

        for (int[] move : moves) {
            MCTSNode child = new MCTSNode(move, nextPlayer, node);
            node.children.add(child);
        }
    }

    private double simulate(MCTSNode node) {
        int[][] simBoard = new int[size][size];
        for (int i = 0; i < size; i++) {
            simBoard[i] = board[i].clone();
        }

        int currentPlayer = node.player;
        List<int[]> movesPlayed = new ArrayList<>();
        if (node.move != null) {
            simBoard[node.move[0]][node.move[1]] = currentPlayer;
            movesPlayed.add(node.move);
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }

        while (!hasPlayerWonSim(simBoard, 1) && !hasPlayerWonSim(simBoard, 2)) {
            List<int[]> moves = getCandidateMoves(simBoard);
            if (moves.isEmpty()) break;

            int[] bestMove = null;
            int bestScore = Integer.MIN_VALUE;
            for (int[] move : moves) {
                int[][] tempBoard = new int[size][size];
                for (int i = 0; i < size; i++) {
                    tempBoard[i] = simBoard[i].clone();
                }
                tempBoard[move[0]][move[1]] = currentPlayer;
                int score = evaluateBoard(tempBoard, currentPlayer);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }

            if (bestMove != null) {
                simBoard[bestMove[0]][bestMove[1]] = currentPlayer;
                movesPlayed.add(bestMove);
            } else {
                int[] move = moves.get(random.nextInt(moves.size()));
                simBoard[move[0]][move[1]] = currentPlayer;
                movesPlayed.add(move);
            }
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }

        double reward = 0;
        if (hasPlayerWonSim(simBoard, 2)) reward = 1.0;
        else if (hasPlayerWonSim(simBoard, 1)) reward = -1.0;

        for (int[] move : movesPlayed) {
            String moveKey = move[0] + "," + move[1];
            double[] stats = raveStats.getOrDefault(moveKey, new double[]{0.0, 0.0});
            stats[0] += reward;
            stats[1] += 1;
            raveStats.put(moveKey, stats);
        }

        return reward;
    }

    private void backpropagate(MCTSNode node, double reward) {
        while (node != null) {
            node.visits++;
            node.totalReward += (node.player == 2) ? reward : -reward;
            if (node.move != null) {
                String moveKey = node.move[0] + "," + node.move[1];
                double[] stats = raveStats.getOrDefault(moveKey, new double[]{0.0, 0.0});
                node.raveReward = stats[0];
                node.raveVisits = (int) stats[1];
            }
            node = node.parent;
        }
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

    private static class Node {
        int row, col, cost;
        Node(int row, int col, int cost) {
            this.row = row;
            this.col = col;
            this.cost = cost;
        }
    }
}