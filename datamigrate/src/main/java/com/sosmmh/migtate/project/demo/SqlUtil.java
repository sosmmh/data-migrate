package com.sosmmh.migtate.project.demo;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/07/04 15:20
 */
@Slf4j
public class SqlUtil {

    public static final int PAGE_SIZE = 2;

    public static final String SELECT_FROM_SOURCE = "SELECT * FROM user_1 LIMIT ?, " + PAGE_SIZE;

    public static final String INSERT_TO_TARGET = "INSERT INTO user_2(user_name, password) VALUES";

    public static final String SELECT_FROM_SOURCE_2 = "SELECT * FROM user_3 WHERE status = ? LIMIT ?, " + PAGE_SIZE;

    public static final String INSERT_TO_TARGET_2 = "INSERT INTO user_4(user_name, password, status) VALUES";

    /**
     * 以数量维度切割线程
     */
    public static final String SELECT_MAX_ID = "SELECT MAX(id) maxId FROM user_1";

    public static final String SELECT_MIN_ID = "SELECT min(id) minId FROM user_1";

    public static final String SELECT_COUNT = "SELECT count(*) countId FROM user_1";

    public static final String SELECT_BLOCK = "SELECT * FROM user_1 WHERE id >= ? AND id <= ? LIMIT ?, " + PAGE_SIZE;



    public static String buildInsertSql(int size, String sql) {
        StringBuilder insertSql = new StringBuilder(sql);
        for (int i = 0; i < size; i++) {
            insertSql.append("(?, ?, ?),");
        }
        insertSql.setCharAt(insertSql.length() - 1, ';');
        return insertSql.toString();
    }

    public static Object getOneFiled(DruidDataSource dataSource, String sql, String filed) {
        DruidPooledConnection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            Object count = resultSet.getObject(filed);
            resultSet.close();
            conn.close();
            return count;
        } catch (SQLException e) {
            log.error("get one filed fail", e);
        }
        return 0;
    }

}
