package com.camellia.web2048.repo;

import com.camellia.web2048.db.Db;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class GameRepo {
    private final Db db;

    public GameRepo(Db db) {
        this.db = db;
    }

    // activegame: SELECT * FROM activegame WHERE user_id = ?
    public Optional<Map<String, Object>> findActiveGame(int userId) throws SQLException {
        String sql = "SELECT user_id, game_id, data_json FROM activegame WHERE user_id = ?";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Map<String, Object> row = new HashMap<>();
                row.put("user_id", rs.getInt("user_id"));
                row.put("game_id", rs.getObject("game_id"));
                row.put("data_json", rs.getString("data_json"));
                return Optional.of(row);
            }
        }
    }

    // activegame: UPDATE activegame SET data_json=?, game_id=? WHERE user_id=?
    public void updateActiveGame(int userId, Integer gameId, String dataJson) throws SQLException {
        String sql = "UPDATE activegame SET data_json = ?, game_id = ? WHERE user_id = ?";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dataJson);
            if (gameId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, gameId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    // activegame: INSERT INTO activegame (user_id, game_id, data_json) VALUES (?, ?, ?)
    public void insertActiveGame(int userId, Integer gameId, String dataJson) throws SQLException {
        String sql = "INSERT INTO activegame (user_id, game_id, data_json) VALUES (?, ?, ?)";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (gameId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, gameId);
            ps.setString(3, dataJson);
            ps.executeUpdate();
        }
    }

    // load_game.php: SELECT data_json FROM activegame WHERE user_id = ?
    public Optional<String> loadActiveGameDataJson(int userId) throws SQLException {
        String sql = "SELECT data_json FROM activegame WHERE user_id = ?";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.ofNullable(rs.getString("data_json"));
            }
        }
    }

    // load_game_id.php: SELECT game_id FROM activegame WHERE user_id = ?
    public Optional<Integer> loadActiveGameId(int userId) throws SQLException {
        String sql = "SELECT game_id FROM activegame WHERE user_id = ?";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                int gid = rs.getInt("game_id");
                if (rs.wasNull()) return Optional.empty();
                return Optional.of(gid);
            }
        }
    }

    // load_previous_game.php: SELECT * FROM allgames WHERE user_id=? AND game_id=?
    public Optional<String> loadAllGamesDataJsonById(int userId, int gameId) throws SQLException {
        String sql = "SELECT data_json FROM allgames WHERE user_id = ? AND game_id = ?";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.ofNullable(rs.getString("data_json"));
            }
        }
    }

    // save_allgames.php: SELECT MAX(game_id) AS max_game_id FROM allgames
    public int getMaxGameId() throws SQLException {
        String sql = "SELECT MAX(game_id) AS max_game_id FROM allgames";
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) return 0;
            int max = rs.getInt("max_game_id");
            if (rs.wasNull()) return 0;
            return max;
        }
    }

    // save_allgames.php: UPDATE allgames SET data_json=?, record=? WHERE user_id=1 AND game_id=?
    public void updateAllGames(int userId, int gameId, String dataJson, int record) throws SQLException {
        String sql = "UPDATE allgames SET data_json = ?, record = ? WHERE user_id = ? AND game_id = ?";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dataJson);
            ps.setInt(2, record);
            ps.setInt(3, userId);
            ps.setInt(4, gameId);
            ps.executeUpdate();
        }
    }

    // save_allgames.php: INSERT INTO allgames (user_id, game_id, data_json, record) VALUES (1, ?, ?, ?)
    public void insertAllGames(int userId, int gameId, String dataJson, int record) throws SQLException {
        String sql = "INSERT INTO allgames (user_id, game_id, data_json, record) VALUES (?, ?, ?, ?)";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, gameId);
            ps.setString(3, dataJson);
            ps.setInt(4, record);
            ps.executeUpdate();
        }
    }

    // 给 history 用：SELECT * FROM allgames ORDER BY record DESC
    public List<Map<String, Object>> listHistoryByRecordDesc() throws SQLException {
        String sql = "SELECT user_id, game_id, record FROM allgames ORDER BY record DESC";
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("user_id", rs.getInt("user_id"));
                row.put("game_id", rs.getInt("game_id"));
                row.put("record", rs.getInt("record"));
                list.add(row);
            }
            return list;
        }
    }
}

