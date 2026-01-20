package com.camellia.web2048.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final ObjectMapper M = new ObjectMapper();

    public static String toJson(Object obj) {
        try { return M.writeValueAsString(obj); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static Object fromJson(String json) {
        try { return M.readValue(json, new TypeReference<Object>(){}); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}

