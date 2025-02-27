package com.example.hex_game;

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
}
