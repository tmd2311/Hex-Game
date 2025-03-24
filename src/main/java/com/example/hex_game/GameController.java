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
         boolean success = game.makeMove(moveRequest.getRow(), moveRequest.getCol(), moveRequest.getPlayer());
         int winner = game.checkWinner();
         return new MoveResponse(success, winner);
     }
     @GetMapping("/reset")
    public void reset() {
         this.game = new HexBoard(11);
     }
    @PostMapping("/swap")
    public ResponseEntity<SwapResponse> swap(@RequestBody SwapRequest swapRequest) {
        if (game.hasSwapped()) { // Kiểm tra trạng thái đã swap (nếu thêm hàm hasSwapped trong HexBoard)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SwapResponse(false, "Swap has already been performed."));
        }
        if (swapRequest.isSwap()) {
            game.swap();
            return ResponseEntity.ok(new SwapResponse(true, "Players swapped successfully."));
        }
        return ResponseEntity.ok(new SwapResponse(false, "Swap declined by player."));
    }
}
