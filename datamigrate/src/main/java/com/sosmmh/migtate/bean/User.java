package com.sosmmh.migtate.bean;

import lombok.Data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/07/04 15:23
 */
@Data
public class User {

    private Long id;

    private String userName;

    private String password;

    private int status;

    public static int readData(ResultSet resultSet, List<User> userList) throws SQLException {
        User user = null;
        int size = 0;
        while (resultSet.next()) {
            size++;

            // 从源数据库读取数据可能需要转成成目标数据库的格式
            user = new User();
            user.setUserName(resultSet.getString("user_name"));
            user.setPassword(resultSet.getString("password"));
            user.setStatus(resultSet.getInt("status"));
            userList.add(user);
        }
        return size;
    }

    public static void writeData(PreparedStatement preparedStatement, List<User> userList) throws SQLException {
        int index = 1;
        for (User user : userList) {
            preparedStatement.setObject(index++, user.getUserName());
            preparedStatement.setObject(index++, user.getPassword());
            preparedStatement.setObject(index++, user.getStatus());
        }
    }
}
