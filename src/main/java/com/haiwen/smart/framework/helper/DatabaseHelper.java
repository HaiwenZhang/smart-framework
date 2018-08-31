package com.haiwen.smart.framework.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final String driver = ConfigHelper.getJdbcDriver();
    private static final String url = ConfigHelper.getJdbcUrl();
    private static final String username = ConfigHelper.getJdbcUsername();
    private static final String password = ConfigHelper.getJdbcPassword();


    private static final ThreadLocal<Connection> connContainer = new ThreadLocal<>();

    public static Connection getConnection() {
        Connection connection = connContainer.get();
        try {
            if(connection != null) {
                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connContainer.set(connection);
        }
        return connection;
    }

    public static void beginTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                logger.error("begin transaction failure ", e);
                throw new RuntimeException(e);
            } finally {
                connContainer.set(conn);
            }
        }
    }

    public static void commitTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                logger.error("commit transaction failure ", e);
                throw new RuntimeException(e);
            } finally {
                connContainer.remove();
            }
        }
    }

    public static void rollbackTransaction() {
        Connection conn = getConnection();

        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                logger.error("commit transaction failure ", e);
                throw new RuntimeException(e);
            } finally {
                connContainer.remove();
            }
        }
    }

}
