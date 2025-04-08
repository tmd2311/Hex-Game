package com.example.hex_game;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController


@RequestMapping("/api")
public class GameController {
    private HexBoard game;
     public GameController() {
         this.game = new HexBoard(11);
     }

     @GetMapping("/board")
    public int[][] getBoard() {
         return game.getBoard();
     }

    @PostMapping("/move")
    public MoveResponse move(@RequestBody MoveRequest moveRequest) {
        try {
            boolean success = game.makeMove(moveRequest.getRow(), moveRequest.getCol(), moveRequest.getPlayer());
            int winner = game.checkWinner();
            if (!success) {
                System.out.println("Nước đi không hợp lệ tại Row: " + moveRequest.getRow() + ", Col: " + moveRequest.getCol());
            }
            return new MoveResponse(success, winner);
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý nước đi: " + e.getMessage());
            return new MoveResponse(false, 0); // Trả về false và không có người thắng trong trường hợp lỗi
        }
    }
     @GetMapping("/reset")
    public void reset() {
         this.game = new HexBoard(11);
     }


    @PostMapping("/swap")
    public ResponseEntity<SwapResponse> swap(@RequestBody SwapRequest swapRequest) {
        // Kiểm tra trạng thái đã swap
        if (game.hasSwapped()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SwapResponse(false, "Swap has already been performed."));
        }
        // Kiểm tra nếu người chơi thứ 2 đã thực hiện nước đi
        if (game.getBoard() != null) {
            for (int i = 0; i < game.getBoard().length; i++) {
                for (int j = 0; j < game.getBoard()[i].length; j++) {
                    if (game.getBoard()[i][j] == 2) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new SwapResponse(false, "Player 2 has already made a move. Swap is not allowed."));
                    }
                }
            }
        }
        // Thực hiện swap nếu tất cả điều kiện đều hợp lệ
        if (swapRequest.isSwap()) {
            game.swap();
            return ResponseEntity.ok(new SwapResponse(true, "Players swapped successfully."));
        }
        return ResponseEntity.ok(new SwapResponse(false, "Swap declined by player."));
    }

    @PostMapping("/aimove")
    public int[] aimove(@RequestBody int[][] board) {
        if (board == null || board.length == 0) {
            return new int[]{-1, -1};  // Trả về lỗi thay vì crash server
        }
        int size = board.length;
        HexBoard hexBoard = new HexBoard(size);
        HexAI ai = new HexAI(size, board, hexBoard);
         return ai.getBestMove();
    }
}
