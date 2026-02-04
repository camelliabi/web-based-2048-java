package com.camellia.web2048.repo;

import com.camellia.web2048.db.Db;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for GameRepo
 * 
 * These tests ensure database operations maintain data integrity
 * and handle edge cases correctly, preventing data loss and corruption.
 */
@DisplayName("GameRepo Tests")
class GameRepoTest {

    @Mock
    private Db mockDb;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private GameRepo repo;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repo = new GameRepo(mockDb);
        when(mockDb.connect()).thenReturn(mockConnection);
    }

    // ==================== CORE REGRESSION TESTS ====================

    /**
     * Test: findActiveGame returns game data when it exists
     * 
     * Scenario: User has an active game saved
     * 
     * Expected Behavior:
     * - Optional contains game data
     * - All fields are correctly populated
     * 
     * Root Cause Connection: Ensures game state can be retrieved,
     * preventing issues like Bug #2 where game_id might be lost
     */
    @Test
    @DisplayName("Should find active game for existing user")
    void testFindActiveGame_ExistingUser() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getObject("game_id")).thenReturn(42);
        when(mockResultSet.getString("data_json")).thenReturn("[[2,4,8,16],[0,0,0,0],[0,0,0,0],[0,0,0,0]]");

        // Act
        Optional<Map<String, Object>> result = repo.findActiveGame(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().get("user_id"));
        assertEquals(42, result.get().get("game_id"));
        assertNotNull(result.get().get("data_json"));
        
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeQuery();
    }

    /**
     * Test: findActiveGame returns empty Optional when no game exists
     * 
     * Scenario: User has no saved game
     * 
     * Expected Behavior:
     * - Optional is empty
     * - No exception is thrown
     */
    @Test
    @DisplayName("Should return empty Optional when no active game exists")
    void testFindActiveGame_NoGame() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Map<String, Object>> result = repo.findActiveGame(999);

        // Assert
        assertFalse(result.isPresent());
        verify(mockPreparedStatement).setInt(1, 999);
    }

    /**
     * Test: insertActiveGame with non-null game_id
     * 
     * Scenario: Creating new active game with specific game_id
     * 
     * Expected Behavior:
     * - Game is inserted with provided game_id
     * - All parameters are correctly set
     */
    @Test
    @DisplayName("Should insert active game with non-null game_id")
    void testInsertActiveGame_WithGameId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[2,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        repo.insertActiveGame(1, 42, dataJson);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 42);
        verify(mockPreparedStatement).setString(3, dataJson);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: insertActiveGame with null game_id
     * 
     * Scenario: Creating new active game without game_id
     * 
     * Expected Behavior:
     * - Game is inserted with NULL game_id
     * - No NullPointerException
     * 
     * Root Cause Connection: Critical for Bug #2 - ensures null game_id
     * is properly handled in database operations
     */
    @Test
    @DisplayName("Should insert active game with null game_id")
    void testInsertActiveGame_NullGameId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[0,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        repo.insertActiveGame(1, null, dataJson);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setNull(2, Types.INTEGER);
        verify(mockPreparedStatement).setString(3, dataJson);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: updateActiveGame with non-null game_id
     * 
     * Scenario: Updating existing game with new data
     * 
     * Expected Behavior:
     * - Game data is updated
     * - game_id is updated
     */
    @Test
    @DisplayName("Should update active game with non-null game_id")
    void testUpdateActiveGame_WithGameId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[1024,512,256,128],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        repo.updateActiveGame(1, 42, dataJson);

        // Assert
        verify(mockPreparedStatement).setString(1, dataJson);
        verify(mockPreparedStatement).setInt(2, 42);
        verify(mockPreparedStatement).setInt(3, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: updateActiveGame with null game_id
     * 
     * Scenario: Updating game but clearing game_id
     * 
     * Expected Behavior:
     * - Game data is updated
     * - game_id is set to NULL
     */
    @Test
    @DisplayName("Should update active game with null game_id")
    void testUpdateActiveGame_NullGameId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[2,4,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        repo.updateActiveGame(1, null, dataJson);

        // Assert
        verify(mockPreparedStatement).setString(1, dataJson);
        verify(mockPreparedStatement).setNull(2, Types.INTEGER);
        verify(mockPreparedStatement).setInt(3, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: loadActiveGameDataJson returns data for existing game
     * 
     * Scenario: Loading game data JSON
     * 
     * Expected Behavior:
     * - Optional contains JSON string
     */
    @Test
    @DisplayName("Should load active game data JSON")
    void testLoadActiveGameDataJson_Success() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("data_json")).thenReturn("[[2,4,8,16],[0,0,0,0],[0,0,0,0],[0,0,0,0]]");

        // Act
        Optional<String> result = repo.loadActiveGameDataJson(1);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("[2,4,8,16]"));
        verify(mockPreparedStatement).setInt(1, 1);
    }

    /**
     * Test: loadActiveGameDataJson returns empty for non-existent game
     * 
     * Scenario: No saved game exists
     * 
     * Expected Behavior:
     * - Optional is empty
     */
    @Test
    @DisplayName("Should return empty Optional when no game data exists")
    void testLoadActiveGameDataJson_NoData() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<String> result = repo.loadActiveGameDataJson(999);

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Test: loadActiveGameId returns game_id when it exists
     * 
     * Scenario: User has active game with game_id
     * 
     * Expected Behavior:
     * - Optional contains game_id
     * 
     * Root Cause Connection: Essential for Bug #2 fix - ensures game_id
     * can be retrieved for navigation purposes
     */
    @Test
    @DisplayName("Should load active game_id")
    void testLoadActiveGameId_Success() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("game_id")).thenReturn(42);
        when(mockResultSet.wasNull()).thenReturn(false);

        // Act
        Optional<Integer> result = repo.loadActiveGameId(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    /**
     * Test: loadActiveGameId returns empty when game_id is NULL
     * 
     * Scenario: Game exists but game_id is NULL
     * 
     * Expected Behavior:
     * - Optional is empty
     * - Correctly handles SQL NULL
     */
    @Test
    @DisplayName("Should return empty Optional when game_id is NULL")
    void testLoadActiveGameId_NullGameId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("game_id")).thenReturn(0);
        when(mockResultSet.wasNull()).thenReturn(true);

        // Act
        Optional<Integer> result = repo.loadActiveGameId(1);

        // Assert
        assertFalse(result.isPresent());
    }

    /**
     * Test: loadAllGamesDataJsonById returns data for specific game
     * 
     * Scenario: Loading a historical game by game_id
     * 
     * Expected Behavior:
     * - Correct game data is returned
     * 
     * Root Cause Connection: Related to Bug #2 - ensures historical games
     * can be loaded when navigating with game_id parameter
     */
    @Test
    @DisplayName("Should load game data by game_id from history")
    void testLoadAllGamesDataJsonById_Success() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("data_json")).thenReturn("[[1024,512,256,128],[64,32,16,8],[4,2,0,0],[0,0,0,0]]");

        // Act
        Optional<String> result = repo.loadAllGamesDataJsonById(1, 42);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("1024"));
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 42);
    }

    /**
     * Test: getMaxGameId returns 0 for empty table
     * 
     * Scenario: No games in allgames table
     * 
     * Expected Behavior:
     * - Returns 0
     * - Handles SQL NULL correctly
     */
    @Test
    @DisplayName("Should return 0 when allgames table is empty")
    void testGetMaxGameId_EmptyTable() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("max_game_id")).thenReturn(0);
        when(mockResultSet.wasNull()).thenReturn(true);

        // Act
        int result = repo.getMaxGameId();

        // Assert
        assertEquals(0, result);
    }

    /**
     * Test: getMaxGameId returns correct maximum
     * 
     * Scenario: Multiple games exist
     * 
     * Expected Behavior:
     * - Returns highest game_id
     */
    @Test
    @DisplayName("Should return maximum game_id from allgames")
    void testGetMaxGameId_WithGames() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("max_game_id")).thenReturn(99);
        when(mockResultSet.wasNull()).thenReturn(false);

        // Act
        int result = repo.getMaxGameId();

        // Assert
        assertEquals(99, result);
    }

    /**
     * Test: insertAllGames inserts new game record
     * 
     * Scenario: Saving completed game to history
     * 
     * Expected Behavior:
     * - All parameters are correctly set
     * - Record value is saved
     */
    @Test
    @DisplayName("Should insert new game into allgames table")
    void testInsertAllGames_Success() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[2048,1024,512,256],[128,64,32,16],[8,4,2,0],[0,0,0,0]]";

        // Act
        repo.insertAllGames(1, 10, dataJson, 2048);

        // Assert
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setInt(2, 10);
        verify(mockPreparedStatement).setString(3, dataJson);
        verify(mockPreparedStatement).setInt(4, 2048);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: updateAllGames updates existing game record
     * 
     * Scenario: Updating game in history
     * 
     * Expected Behavior:
     * - Correct game is updated
     * - Record is updated
     */
    @Test
    @DisplayName("Should update existing game in allgames table")
    void testUpdateAllGames_Success() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[4096,2048,1024,512],[256,128,64,32],[16,8,4,2],[0,0,0,0]]";

        // Act
        repo.updateAllGames(1, 5, dataJson, 4096);

        // Assert
        verify(mockPreparedStatement).setString(1, dataJson);
        verify(mockPreparedStatement).setInt(2, 4096);
        verify(mockPreparedStatement).setInt(3, 1);
        verify(mockPreparedStatement).setInt(4, 5);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: listHistoryByRecordDesc returns ordered list
     * 
     * Scenario: Retrieving game history
     * 
     * Expected Behavior:
     * - Games are ordered by record (descending)
     * - All fields are populated
     */
    @Test
    @DisplayName("Should list game history ordered by record descending")
    void testListHistoryByRecordDesc_Success() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        
        // First game
        when(mockResultSet.getInt("user_id")).thenReturn(1, 1, 1);
        when(mockResultSet.getInt("game_id")).thenReturn(3, 2, 1);
        when(mockResultSet.getInt("record")).thenReturn(2048, 1024, 512);

        // Act
        List<Map<String, Object>> result = repo.listHistoryByRecordDesc();

        // Assert
        assertEquals(3, result.size());
        assertEquals(2048, result.get(0).get("record"));
        assertEquals(1024, result.get(1).get("record"));
        assertEquals(512, result.get(2).get("record"));
    }

    // ==================== BOUNDARY AND EDGE-CASE TESTS ====================

    /**
     * Test: Edge case - very large game_id
     * 
     * Scenario: game_id is Integer.MAX_VALUE
     * 
     * Expected Behavior:
     * - Operation completes successfully
     * - No overflow errors
     */
    @Test
    @DisplayName("Should handle very large game_id values")
    void testInsertActiveGame_LargeGameId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        int largeGameId = Integer.MAX_VALUE;
        String dataJson = "[[2,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        repo.insertActiveGame(1, largeGameId, dataJson);

        // Assert
        verify(mockPreparedStatement).setInt(2, largeGameId);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: Edge case - empty JSON string
     * 
     * Scenario: data_json is empty string
     * 
     * Expected Behavior:
     * - Operation completes
     * - Empty string is stored
     */
    @Test
    @DisplayName("Should handle empty JSON string")
    void testInsertActiveGame_EmptyJson() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String emptyJson = "";

        // Act
        repo.insertActiveGame(1, 1, emptyJson);

        // Assert
        verify(mockPreparedStatement).setString(3, emptyJson);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: Edge case - record value of 0
     * 
     * Scenario: Empty grid results in record = 0
     * 
     * Expected Behavior:
     * - Record 0 is correctly stored
     */
    @Test
    @DisplayName("Should handle record value of 0")
    void testInsertAllGames_ZeroRecord() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[0,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        repo.insertAllGames(1, 1, dataJson, 0);

        // Assert
        verify(mockPreparedStatement).setInt(4, 0);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: Edge case - very large record value
     * 
     * Scenario: Player achieves 8192 tile (or higher)
     * 
     * Expected Behavior:
     * - Large record value is stored correctly
     * 
     * Root Cause Connection: Related to Bug #1 - ensures large values
     * that caused overflow in UI are correctly stored in database
     */
    @Test
    @DisplayName("Should handle very large record values (8192)")
    void testInsertAllGames_VeryLargeRecord() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String dataJson = "[[8192,4096,2048,1024],[512,256,128,64],[32,16,8,4],[2,0,0,0]]";

        // Act
        repo.insertAllGames(1, 1, dataJson, 8192);

        // Assert
        verify(mockPreparedStatement).setInt(4, 8192);
        verify(mockPreparedStatement).executeUpdate();
    }

    /**
     * Test: Connection is properly closed after operation
     * 
     * Scenario: Any database operation
     * 
     * Expected Behavior:
     * - Connection is closed in finally block
     * - Resources are not leaked
     */
    @Test
    @DisplayName("Should close database connections properly")
    void testFindActiveGame_ClosesConnection() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        repo.findActiveGame(1);

        // Assert
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    /**
     * Test: SQL parameters are correctly escaped
     * 
     * Scenario: Data contains special characters
     * 
     * Expected Behavior:
     * - PreparedStatement is used (prevents SQL injection)
     * - Special characters are handled
     */
    @Test
    @DisplayName("Should use PreparedStatement to prevent SQL injection")
    void testInsertActiveGame_SqlInjectionPrevention() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        String maliciousJson = "[[2,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]; DROP TABLE activegame; --";

        // Act
        repo.insertActiveGame(1, 1, maliciousJson);

        // Assert
        // PreparedStatement.setString should be called, which escapes the input
        verify(mockPreparedStatement).setString(3, maliciousJson);
        verify(mockPreparedStatement).executeUpdate();
    }
}
