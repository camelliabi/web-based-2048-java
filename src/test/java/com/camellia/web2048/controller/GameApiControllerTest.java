package com.camellia.web2048.controller;

import com.camellia.web2048.repo.GameRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for GameApiController
 * 
 * These tests guard against regressions in game state management,
 * data persistence, and record calculation logic.
 */
@DisplayName("GameApiController Tests")
class GameApiControllerTest {

    @Mock
    private GameRepo mockRepo;

    private GameApiController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new GameApiController(mockRepo);
    }

    // ==================== CORE REGRESSION TESTS ====================

    /**
     * Test: save_game with valid game_id
     * 
     * Scenario: User saves an active game with a specific game_id
     * 
     * Expected Behavior:
     * - Before fix: N/A (new functionality)
     * - After fix: Game data is correctly saved/updated in database
     * 
     * Root Cause Connection: Ensures game state persistence works correctly,
     * preventing data loss that could lead to navigation issues like Bug #2
     */
    @Test
    @DisplayName("Should save game successfully with valid game_id")
    void testSaveGame_WithValidGameId() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", 42);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(2, 4, 8, 16),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        when(mockRepo.findActiveGame(1)).thenReturn(Optional.of(new HashMap<>()));

        // Act
        Map<String, Object> response = controller.saveGame(requestBody);

        // Assert
        assertEquals("success", response.get("status"));
        verify(mockRepo).updateActiveGame(eq(1), eq(42), anyString());
        verify(mockRepo, never()).insertActiveGame(anyInt(), any(), anyString());
    }

    /**
     * Test: save_game with null game_id
     * 
     * Scenario: User saves a new game without an existing game_id
     * 
     * Expected Behavior:
     * - Game data is saved with null game_id
     * - No NullPointerException is thrown
     * 
     * Root Cause Connection: Tests null handling which was critical in Bug #2
     * where game_id could be null during navigation
     */
    @Test
    @DisplayName("Should save game successfully with null game_id")
    void testSaveGame_WithNullGameId() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", null);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        when(mockRepo.findActiveGame(1)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> response = controller.saveGame(requestBody);

        // Assert
        assertEquals("success", response.get("status"));
        verify(mockRepo).insertActiveGame(eq(1), isNull(), anyString());
    }

    /**
     * Test: save_game defaults user_id to 1
     * 
     * Scenario: Request doesn't include user_id
     * 
     * Expected Behavior:
     * - user_id defaults to 1
     * - Game is saved successfully
     */
    @Test
    @DisplayName("Should default user_id to 1 when not provided")
    void testSaveGame_DefaultUserId() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(2, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        when(mockRepo.findActiveGame(1)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> response = controller.saveGame(requestBody);

        // Assert
        assertEquals("success", response.get("status"));
        verify(mockRepo).insertActiveGame(eq(1), any(), anyString());
    }

    /**
     * Test: load_game returns correct data for existing user
     * 
     * Scenario: User loads their saved game
     * 
     * Expected Behavior:
     * - Game data is returned as JSON object
     * - No error message is present
     */
    @Test
    @DisplayName("Should load game data for existing user")
    void testLoadGame_ExistingUser() throws SQLException {
        // Arrange
        String savedJson = "[[2,4,8,16],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";
        when(mockRepo.loadActiveGameDataJson(1)).thenReturn(Optional.of(savedJson));

        // Act
        Map<String, Object> response = controller.loadGame(1);

        // Assert
        assertTrue(response.containsKey("data_json"));
        assertFalse(response.containsKey("error"));
    }

    /**
     * Test: load_game returns error for non-existent user
     * 
     * Scenario: User has no saved game
     * 
     * Expected Behavior:
     * - Error message is returned
     * - No data_json is present
     */
    @Test
    @DisplayName("Should return error for non-existent user")
    void testLoadGame_NonExistentUser() throws SQLException {
        // Arrange
        when(mockRepo.loadActiveGameDataJson(999)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> response = controller.loadGame(999);

        // Assert
        assertEquals("No saved game found", response.get("error"));
        assertFalse(response.containsKey("data_json"));
    }

    /**
     * Test: load_game_id returns game_id for existing user
     * 
     * Scenario: User has an active game with game_id
     * 
     * Expected Behavior:
     * - game_id is returned
     * - No error message
     * 
     * Root Cause Connection: Critical for Bug #2 fix - ensures game_id
     * is correctly retrieved for navigation
     */
    @Test
    @DisplayName("Should load game_id for existing user")
    void testLoadGameId_ExistingUser() throws SQLException {
        // Arrange
        when(mockRepo.loadActiveGameId(1)).thenReturn(Optional.of(42));

        // Act
        Map<String, Object> response = controller.loadGameId(1);

        // Assert
        assertEquals(42, response.get("game_id"));
        assertFalse(response.containsKey("error"));
    }

    /**
     * Test: load_game_id returns error when no game_id exists
     * 
     * Scenario: User has no active game_id
     * 
     * Expected Behavior:
     * - Error message is returned
     */
    @Test
    @DisplayName("Should return error when no game_id exists")
    void testLoadGameId_NoGameId() throws SQLException {
        // Arrange
        when(mockRepo.loadActiveGameId(1)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> response = controller.loadGameId(1);

        // Assert
        assertEquals("No saved game found", response.get("error"));
        assertFalse(response.containsKey("game_id"));
    }

    /**
     * Test: load_previous_game returns correct data
     * 
     * Scenario: User loads a specific game from history
     * 
     * Expected Behavior:
     * - Game data for specified game_id is returned
     * 
     * Root Cause Connection: Related to Bug #2 - ensures historical games
     * can be loaded correctly when navigating with game_id parameter
     */
    @Test
    @DisplayName("Should load previous game by game_id")
    void testLoadPreviousGame_Success() throws SQLException {
        // Arrange
        String savedJson = "[[1024,512,256,128],[64,32,16,8],[4,2,0,0],[0,0,0,0]]";
        when(mockRepo.loadAllGamesDataJsonById(1, 42)).thenReturn(Optional.of(savedJson));

        // Act
        Map<String, Object> response = controller.loadPreviousGame(1, 42);

        // Assert
        assertTrue(response.containsKey("data_json"));
        assertFalse(response.containsKey("error"));
    }

    // ==================== BOUNDARY AND EDGE-CASE TESTS ====================

    /**
     * Test: computeRecord with empty grid
     * 
     * Scenario: All cells are zero
     * 
     * Expected Behavior:
     * - Record should be 0
     */
    @Test
    @DisplayName("Should compute record as 0 for empty grid")
    void testComputeRecord_EmptyGrid() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        when(mockRepo.getMaxGameId()).thenReturn(0);

        // Act
        controller.saveAllGames(requestBody);

        // Assert
        verify(mockRepo).insertAllGames(eq(1), eq(1), anyString(), eq(0));
    }

    /**
     * Test: computeRecord with single max tile
     * 
     * Scenario: Grid has one 2048 tile and rest are zeros
     * 
     * Expected Behavior:
     * - Record should be 2048
     * 
     * Root Cause Connection: Related to Bug #1 - ensures large tile values
     * are correctly calculated even when they caused overflow issues
     */
    @Test
    @DisplayName("Should compute record as 2048 for grid with 2048 tile")
    void testComputeRecord_SingleMaxTile() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", 1);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(2048, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        // Act
        controller.saveAllGames(requestBody);

        // Assert
        verify(mockRepo).updateAllGames(eq(1), eq(1), anyString(), eq(2048));
    }

    /**
     * Test: computeRecord with multiple high-value tiles
     * 
     * Scenario: Grid has 1024, 512, 256, etc.
     * 
     * Expected Behavior:
     * - Record should be 1024 (the maximum)
     */
    @Test
    @DisplayName("Should compute record as max value from multiple tiles")
    void testComputeRecord_MultipleHighValues() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", 5);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(1024, 512, 256, 128),
            Arrays.asList(64, 32, 16, 8),
            Arrays.asList(4, 2, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        // Act
        controller.saveAllGames(requestBody);

        // Assert
        verify(mockRepo).updateAllGames(eq(1), eq(5), anyString(), eq(1024));
    }

    /**
     * Test: save_allgames creates new game when game_id is null
     * 
     * Scenario: Saving a completed game to history
     * 
     * Expected Behavior:
     * - New game_id is generated (max + 1)
     * - Game is inserted, not updated
     */
    @Test
    @DisplayName("Should create new game in history when game_id is null")
    void testSaveAllGames_NullGameId() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", null);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(2, 4, 8, 16),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        when(mockRepo.getMaxGameId()).thenReturn(10);

        // Act
        controller.saveAllGames(requestBody);

        // Assert
        verify(mockRepo).insertAllGames(eq(1), eq(11), anyString(), eq(16));
        verify(mockRepo, never()).updateAllGames(anyInt(), anyInt(), anyString(), anyInt());
    }

    /**
     * Test: save_allgames updates existing game when game_id is provided
     * 
     * Scenario: Updating an existing game in history
     * 
     * Expected Behavior:
     * - Existing game is updated
     * - No new game is inserted
     */
    @Test
    @DisplayName("Should update existing game in history when game_id is provided")
    void testSaveAllGames_ExistingGameId() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", 5);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(512, 256, 128, 64),
            Arrays.asList(32, 16, 8, 4),
            Arrays.asList(2, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        // Act
        controller.saveAllGames(requestBody);

        // Assert
        verify(mockRepo).updateAllGames(eq(1), eq(5), anyString(), eq(512));
        verify(mockRepo, never()).insertAllGames(anyInt(), anyInt(), anyString(), anyInt());
    }

    /**
     * Test: history endpoint returns game list
     * 
     * Scenario: User views game history
     * 
     * Expected Behavior:
     * - List of games ordered by record (descending)
     */
    @Test
    @DisplayName("Should return game history ordered by record")
    void testHistory_ReturnsOrderedList() throws SQLException {
        // Arrange
        List<Map<String, Object>> mockHistory = Arrays.asList(
            Map.of("user_id", 1, "game_id", 3, "record", 2048),
            Map.of("user_id", 1, "game_id", 2, "record", 1024),
            Map.of("user_id", 1, "game_id", 1, "record", 512)
        );
        when(mockRepo.listHistoryByRecordDesc()).thenReturn(mockHistory);

        // Act
        List<Map<String, Object>> result = controller.history();

        // Assert
        assertEquals(3, result.size());
        assertEquals(2048, result.get(0).get("record"));
        assertEquals(1024, result.get(1).get("record"));
        assertEquals(512, result.get(2).get("record"));
    }

    /**
     * Test: Edge case - very large tile value (4096)
     * 
     * Scenario: Player achieves 4096 tile (beyond normal 2048)
     * 
     * Expected Behavior:
     * - Record is correctly calculated as 4096
     * 
     * Root Cause Connection: Tests boundary of tile values that caused
     * overflow issues in Bug #1
     */
    @Test
    @DisplayName("Should handle very large tile values (4096)")
    void testComputeRecord_VeryLargeTile() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 1);
        requestBody.put("game_id", 1);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(4096, 2048, 1024, 512),
            Arrays.asList(256, 128, 64, 32),
            Arrays.asList(16, 8, 4, 2),
            Arrays.asList(0, 0, 0, 0)
        ));

        // Act
        controller.saveAllGames(requestBody);

        // Assert
        verify(mockRepo).updateAllGames(eq(1), eq(1), anyString(), eq(4096));
    }

    /**
     * Test: Edge case - user_id = 0
     * 
     * Scenario: Invalid user_id edge case
     * 
     * Expected Behavior:
     * - Operation completes without error
     * - user_id 0 is used (though unusual)
     */
    @Test
    @DisplayName("Should handle user_id = 0")
    void testSaveGame_UserIdZero() throws SQLException {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_id", 0);
        requestBody.put("data_json", Arrays.asList(
            Arrays.asList(2, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        ));

        when(mockRepo.findActiveGame(0)).thenReturn(Optional.empty());

        // Act
        Map<String, Object> response = controller.saveGame(requestBody);

        // Assert
        assertEquals("success", response.get("status"));
        verify(mockRepo).insertActiveGame(eq(0), any(), anyString());
    }
}
