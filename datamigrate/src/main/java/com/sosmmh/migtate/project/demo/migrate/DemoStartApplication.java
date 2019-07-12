package com.sosmmh.migtate.project.demo.migrate;

import com.sosmmh.migtate.utils.DataSourceUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/07/04 21:44
 */
public class DemoStartApplication {

    public static void main(String[] args) throws SQLException {

        Map<String, String> sourceMap = new HashMap<>();
        sourceMap.put(DataSourceUtil.USER_NAME, "root");
        sourceMap.put(DataSourceUtil.PASSWORD, "root");
        sourceMap.put(DataSourceUtil.URL, "jdbc:mysql://localhost:3306/test1?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");

        Map<String, String> sourceMap2 = new HashMap<>();
        sourceMap2.put(DataSourceUtil.USER_NAME, "root");
        sourceMap2.put(DataSourceUtil.PASSWORD, "root");
        sourceMap2.put(DataSourceUtil.URL, "jdbc:mysql://localhost:3306/test2?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");

        new DemoSourceMigrate(Arrays.asList(sourceMap, sourceMap2), sourceMap);

    }

}
