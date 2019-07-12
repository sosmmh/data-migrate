package com.sosmmh.migtate.utils;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.SQLException;
import java.util.Map;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/06/30 16:27
 */
public class DataSourceUtil {

    public static final String USER_NAME = "USER_NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String URL = "URL";

    public static DruidDataSource getDruidDataSource(Map<String, String> map) throws SQLException {
        return getDruidDataSource(map.get(USER_NAME), map.get(PASSWORD), map.get(URL));
    }

    public static DruidDataSource getDruidDataSource(String username, String pwd, String url) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(username);
        dataSource.setPassword(pwd);
        dataSource.setUrl(url);
        dataSource.setMaxActive(20);
        dataSource.init();
        return dataSource;
    }
}
