package com.camellia.web2048.controller;

import com.camellia.web2048.repo.GameRepo;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class GameApiController {
    private final GameRepo repo;

    public GameApiController(GameRepo repo) {
        this.repo = repo;
    }

    // === 等价 save_game.php ===
    @PostMapping("/save_game")
    public Map<String, Object> saveGame(@RequestBody Map<String, Object> body) throws SQLException {
        int userId = ((Number) body.getOrDefault("user_id", 1)).intValue();
        Object gameIdObj = body.get("game_id"); // 可能是 null
        Integer gameId = (gameIdObj == null) ? null : ((Number) gameIdObj).intValue();

        Object dataJsonObj = body.get("data_json"); // 这里是 grid 的二维数组
        String dataJson = JsonUtil.toJson(dataJsonObj);

        // only one active game per user: insert or update
        if (repo.findActiveGame(userId).isPresent()) {
            repo.updateActiveGame(userId, gameId, dataJson);
        } else {
            repo.insertActiveGame(userId, gameId, dataJson);
        }

        return Map.of("status", "success");
    }

    // === 等价 load_game.php ===
    @GetMapping("/load_game")
    public Map<String, Object> loadGame(@RequestParam("user_id") int userId) throws SQLException {
        return repo.loadActiveGameDataJson(userId)
                .map(s -> Map.<String, Object>of("data_json", JsonUtil.fromJson(s)))
                .orElseGet(() -> Map.of("error", "No saved game found"));
    }

    // === 等价 load_game_id.php ===
    @GetMapping("/load_game_id")
    public Map<String, Object> loadGameId(@RequestParam("user_id") int userId) throws SQLException {
        return repo.loadActiveGameId(userId)
                .<Map<String, Object>>map(gid -> Map.of("game_id", gid))
                .orElseGet(() -> Map.of("error", "No saved game found"));
    }

    // === 等价 load_previous_game.php ===
    @GetMapping("/load_previous_game")
    public Map<String, Object> loadPreviousGame(@RequestParam("user_id") int userId,
                                                @RequestParam("game_id") int gameId) throws SQLException {
        return repo.loadAllGamesDataJsonById(userId, gameId)
                .map(s -> Map.<String, Object>of("data_json", JsonUtil.fromJson(s)))
                .orElseGet(() -> Map.of("error", "No saved game found"));
    }

    // === 等价 save_allgames.php ===
    @PostMapping("/save_allgames")
    public Map<String, Object> saveAllGames(@RequestBody Map<String, Object> body) throws SQLException {
        int userId = ((Number) body.getOrDefault("user_id", 1)).intValue();
        Object gameIdObj = body.get("game_id");
        Integer gameId = (gameIdObj == null) ? null : ((Number) gameIdObj).intValue();

        Object dataJsonObj = body.get("data_json");
        String dataJson = JsonUtil.toJson(dataJsonObj);

        // record = biggest tile
        int record = computeRecord(dataJsonObj);

        if (gameId != null) {
            repo.updateAllGames(userId, gameId, dataJson, record);
        } else {
            int nextId = repo.getMaxGameId() + 1;
            repo.insertAllGames(userId, nextId, dataJson, record);
            // 这里可以选择：把 activegame 的 game_id 也更新为 nextId（更合理）
            // repo.updateActiveGame(userId, nextId, dataJson);
        }

        return Map.of("status", "success");
    }

    // 给 history.html 用（替代 history.php 的查询）
    @GetMapping("/history")
    public List<Map<String, Object>> history() throws SQLException {
        return repo.listHistoryByRecordDesc();
    }

    @SuppressWarnings("unchecked")
    private int computeRecord(Object gridObj) {
        int record = 0;
        if (gridObj instanceof List<?> rows) {
            for (Object rowObj : rows) {
                if (rowObj instanceof List<?> cols) {
                    for (Object cell : cols) {
                        int v = (cell instanceof Number n) ? n.intValue() : Integer.parseInt(String.valueOf(cell));
                        if (v > record) record = v;
                    }
                }
            }
        }
        return record;
    }
}

