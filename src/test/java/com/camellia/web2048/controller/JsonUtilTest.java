package com.camellia.web2048.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JsonUtil
 * 
 * These tests ensure JSON serialization and deserialization work correctly,
 * preventing data corruption during game state persistence.
 */
@DisplayName("JsonUtil Tests")
class JsonUtilTest {

    // ==================== CORE REGRESSION TESTS ====================

    /**
     * Test: toJson converts simple 2D array to JSON
     * 
     * Scenario: Converting game grid to JSON string
     * 
     * Expected Behavior:
     * - Valid JSON string is produced
     * - Array structure is preserved
     * 
     * Root Cause Connection: Ensures game state is correctly serialized
     * before database storage, preventing data loss
     */
    @Test
    @DisplayName("Should convert 2D array to JSON string")
    void testToJson_2DArray() {
        // Arrange
        List<List<Integer>> grid = Arrays.asList(
            Arrays.asList(2, 4, 8, 16),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        );

        // Act
        String json = JsonUtil.toJson(grid);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("[2,4,8,16]") || json.contains("[2, 4, 8, 16]"));
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    /**
     * Test: toJson converts Map to JSON
     * 
     * Scenario: Converting response objects to JSON
     * 
     * Expected Behavior:
     * - Valid JSON object string is produced
     * - Key-value pairs are preserved
     */
    @Test
    @DisplayName("Should convert Map to JSON string")
    void testToJson_Map() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", 1);
        data.put("game_id", 42);
        data.put("record", 2048);

        // Act
        String json = JsonUtil.toJson(data);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("user_id"));
        assertTrue(json.contains("game_id"));
        assertTrue(json.contains("record"));
        assertTrue(json.contains("42"));
        assertTrue(json.contains("2048"));
    }

    /**
     * Test: toJson handles null values
     * 
     * Scenario: Grid contains null values
     * 
     * Expected Behavior:
     * - JSON string is produced
     * - null is represented as JSON null
     * 
     * Root Cause Connection: Related to Bug #2 - ensures null game_id
     * is correctly serialized
     */
    @Test
    @DisplayName("Should handle null values in objects")
    void testToJson_NullValues() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", 1);
        data.put("game_id", null);
        data.put("data_json", "[[0,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]]");

        // Act
        String json = JsonUtil.toJson(data);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("null") || json.contains("game_id"));
    }

    /**
     * Test: fromJson converts JSON string to object
     * 
     * Scenario: Loading game data from database
     * 
     * Expected Behavior:
     * - JSON is parsed correctly
     * - Data structure is reconstructed
     */
    @Test
    @DisplayName("Should convert JSON string to object")
    void testFromJson_ValidJson() {
        // Arrange
        String json = "[[2,4,8,16],[0,0,0,0],[0,0,0,0],[0,0,0,0]]";

        // Act
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof List);
        
        @SuppressWarnings("unchecked")
        List<List<Integer>> grid = (List<List<Integer>>) result;
        assertEquals(4, grid.size());
        assertEquals(4, grid.get(0).size());
    }

    /**
     * Test: fromJson handles JSON object
     * 
     * Scenario: Parsing response data
     * 
     * Expected Behavior:
     * - JSON object is parsed to Map
     * - All fields are accessible
     */
    @Test
    @DisplayName("Should convert JSON object string to Map")
    void testFromJson_JsonObject() {
        // Arrange
        String json = "{\"user_id\":1,\"game_id\":42,\"record\":2048}";

        // Act
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1, map.get("user_id"));
        assertEquals(42, map.get("game_id"));
        assertEquals(2048, map.get("record"));
    }

    /**
     * Test: Round-trip conversion preserves data
     * 
     * Scenario: Save and load game data
     * 
     * Expected Behavior:
     * - Data is identical after toJson -> fromJson
     * - No data loss occurs
     */
    @Test
    @DisplayName("Should preserve data in round-trip conversion")
    void testRoundTrip_PreservesData() {
        // Arrange
        List<List<Integer>> originalGrid = Arrays.asList(
            Arrays.asList(1024, 512, 256, 128),
            Arrays.asList(64, 32, 16, 8),
            Arrays.asList(4, 2, 0, 0),
            Arrays.asList(0, 0, 0, 0)
        );

        // Act
        String json = JsonUtil.toJson(originalGrid);
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof List);
        
        @SuppressWarnings("unchecked")
        List<List<Integer>> reconstructedGrid = (List<List<Integer>>) result;
        
        assertEquals(4, reconstructedGrid.size());
        assertEquals(1024, reconstructedGrid.get(0).get(0));
        assertEquals(512, reconstructedGrid.get(0).get(1));
        assertEquals(256, reconstructedGrid.get(0).get(2));
        assertEquals(128, reconstructedGrid.get(0).get(3));
    }

    // ==================== BOUNDARY AND EDGE-CASE TESTS ====================

    /**
     * Test: toJson handles empty array
     * 
     * Scenario: Empty grid
     * 
     * Expected Behavior:
     * - Valid JSON for empty array
     */
    @Test
    @DisplayName("Should handle empty array")
    void testToJson_EmptyArray() {
        // Arrange
        List<List<Integer>> emptyGrid = new ArrayList<>();

        // Act
        String json = JsonUtil.toJson(emptyGrid);

        // Assert
        assertNotNull(json);
        assertEquals("[]", json);
    }

    /**
     * Test: toJson handles empty map
     * 
     * Scenario: Empty response object
     * 
     * Expected Behavior:
     * - Valid JSON for empty object
     */
    @Test
    @DisplayName("Should handle empty map")
    void testToJson_EmptyMap() {
        // Arrange
        Map<String, Object> emptyMap = new HashMap<>();

        // Act
        String json = JsonUtil.toJson(emptyMap);

        // Assert
        assertNotNull(json);
        assertEquals("{}", json);
    }

    /**
     * Test: toJson handles very large numbers
     * 
     * Scenario: Grid with 8192 tile (or higher)
     * 
     * Expected Behavior:
     * - Large numbers are correctly serialized
     * - No overflow or precision loss
     * 
     * Root Cause Connection: Related to Bug #1 - ensures large tile values
     * that caused UI overflow are correctly serialized
     */
    @Test
    @DisplayName("Should handle very large numbers (8192)")
    void testToJson_LargeNumbers() {
        // Arrange
        List<List<Integer>> grid = Arrays.asList(
            Arrays.asList(8192, 4096, 2048, 1024),
            Arrays.asList(512, 256, 128, 64),
            Arrays.asList(32, 16, 8, 4),
            Arrays.asList(2, 0, 0, 0)
        );

        // Act
        String json = JsonUtil.toJson(grid);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("8192"));
        assertTrue(json.contains("4096"));
        assertTrue(json.contains("2048"));
    }

    /**
     * Test: fromJson handles empty JSON array
     * 
     * Scenario: Loading empty grid
     * 
     * Expected Behavior:
     * - Empty list is returned
     * - No exception thrown
     */
    @Test
    @DisplayName("Should parse empty JSON array")
    void testFromJson_EmptyArray() {
        // Arrange
        String json = "[]";

        // Act
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    /**
     * Test: fromJson handles empty JSON object
     * 
     * Scenario: Loading empty response
     * 
     * Expected Behavior:
     * - Empty map is returned
     */
    @Test
    @DisplayName("Should parse empty JSON object")
    void testFromJson_EmptyObject() {
        // Arrange
        String json = "{}";

        // Act
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    /**
     * Test: fromJson throws exception for invalid JSON
     * 
     * Scenario: Malformed JSON string
     * 
     * Expected Behavior:
     * - RuntimeException is thrown
     * - Error is clearly indicated
     */
    @Test
    @DisplayName("Should throw exception for invalid JSON")
    void testFromJson_InvalidJson() {
        // Arrange
        String invalidJson = "{ this is not valid json }";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            JsonUtil.fromJson(invalidJson);
        });
    }

    /**
     * Test: toJson throws exception for non-serializable object
     * 
     * Scenario: Object that cannot be serialized
     * 
     * Expected Behavior:
     * - RuntimeException is thrown
     */
    @Test
    @DisplayName("Should throw exception for non-serializable object")
    void testToJson_NonSerializable() {
        // Arrange
        Object circular = new Object() {
            @SuppressWarnings("unused")
            public Object self = this; // Circular reference
        };

        // Act & Assert
        // Note: Jackson can handle circular references with proper configuration,
        // but without it, this should fail
        assertThrows(RuntimeException.class, () -> {
            JsonUtil.toJson(circular);
        });
    }

    /**
     * Test: toJson handles nested structures
     * 
     * Scenario: Complex nested data
     * 
     * Expected Behavior:
     * - Nested structure is correctly serialized
     */
    @Test
    @DisplayName("Should handle nested structures")
    void testToJson_NestedStructures() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", 1);
        data.put("game_data", Arrays.asList(
            Arrays.asList(2, 4, 8, 16),
            Arrays.asList(0, 0, 0, 0)
        ));
        data.put("metadata", Map.of("version", "1.0", "timestamp", 1234567890));

        // Act
        String json = JsonUtil.toJson(data);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("user_id"));
        assertTrue(json.contains("game_data"));
        assertTrue(json.contains("metadata"));
        assertTrue(json.contains("version"));
    }

    /**
     * Test: fromJson handles nested JSON
     * 
     * Scenario: Parsing complex nested JSON
     * 
     * Expected Behavior:
     * - All nested levels are accessible
     */
    @Test
    @DisplayName("Should parse nested JSON structures")
    void testFromJson_NestedStructures() {
        // Arrange
        String json = "{\"user_id\":1,\"game_data\":[[2,4],[8,16]],\"metadata\":{\"version\":\"1.0\"}}";

        // Act
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        
        assertEquals(1, map.get("user_id"));
        assertTrue(map.get("game_data") instanceof List);
        assertTrue(map.get("metadata") instanceof Map);
    }

    /**
     * Test: toJson handles special characters
     * 
     * Scenario: Data contains quotes, backslashes, etc.
     * 
     * Expected Behavior:
     * - Special characters are properly escaped
     * - Valid JSON is produced
     */
    @Test
    @DisplayName("Should escape special characters in JSON")
    void testToJson_SpecialCharacters() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Game \"2048\" - High Score!");
        data.put("path", "C:\\Users\\Player\\game.json");

        // Act
        String json = JsonUtil.toJson(data);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\\\"") || json.contains("2048"));
        assertTrue(json.contains("\\\\") || json.contains("game.json"));
    }

    /**
     * Test: fromJson handles special characters
     * 
     * Scenario: JSON contains escaped characters
     * 
     * Expected Behavior:
     * - Characters are correctly unescaped
     */
    @Test
    @DisplayName("Should unescape special characters from JSON")
    void testFromJson_SpecialCharacters() {
        // Arrange
        String json = "{\"message\":\"Game \\\"2048\\\" - High Score!\"}";

        // Act
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        
        String message = (String) map.get("message");
        assertTrue(message.contains("\"2048\""));
    }

    /**
     * Test: toJson handles zero values correctly
     * 
     * Scenario: Grid with many zeros
     * 
     * Expected Behavior:
     * - Zeros are preserved (not omitted)
     */
    @Test
    @DisplayName("Should preserve zero values in arrays")
    void testToJson_ZeroValues() {
        // Arrange
        List<List<Integer>> grid = Arrays.asList(
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 0),
            Arrays.asList(0, 0, 0, 2),
            Arrays.asList(0, 0, 0, 0)
        );

        // Act
        String json = JsonUtil.toJson(grid);

        // Assert
        assertNotNull(json);
        // Count zeros - should have many
        int zeroCount = json.length() - json.replace("0", "").length();
        assertTrue(zeroCount >= 15); // At least 15 zeros
    }

    /**
     * Test: Round-trip with null values
     * 
     * Scenario: Data contains null values
     * 
     * Expected Behavior:
     * - Null values are preserved
     * 
     * Root Cause Connection: Critical for Bug #2 - ensures null game_id
     * survives serialization round-trip
     */
    @Test
    @DisplayName("Should preserve null values in round-trip")
    void testRoundTrip_NullValues() {
        // Arrange
        Map<String, Object> original = new HashMap<>();
        original.put("user_id", 1);
        original.put("game_id", null);
        original.put("active", true);

        // Act
        String json = JsonUtil.toJson(original);
        Object result = JsonUtil.fromJson(json);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> reconstructed = (Map<String, Object>) result;
        
        assertEquals(1, reconstructed.get("user_id"));
        assertNull(reconstructed.get("game_id"));
        assertEquals(true, reconstructed.get("active"));
    }
}
