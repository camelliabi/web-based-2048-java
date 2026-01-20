package com.camellia.web2048.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class Db {
    private final String url;

    public Db(@Value("${db.path}") String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}

