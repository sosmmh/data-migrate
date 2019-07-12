package com.sosmmh.migtate.project.demo;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.sosmmh.migtate.bean.User;
import com.sosmmh.migtate.utils.DataSourceUtil;
import com.sosmmh.migtate.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/07/04 15:30
 */
@Slf4j
public abstract class Demo<T> {

    /** 根据数据源数切割线程 */
    protected LinkedBlockingQueue<DruidDataSource> sourceDataSourceQueue;

    /** 根据某个维度切割线程 */
    protected LinkedBlockingQueue<Object> objectQueue;

    protected DruidDataSource sourceDataSource, targetDataSource;

    protected AtomicInteger totalSourceSize, totalTargetSize;

    protected CountDownLatch countDownLatch;

    public Demo(Map<String, String> source, Map<String, String> target) throws SQLException {

        sourceDataSource = DataSourceUtil.getDruidDataSource(source);
        targetDataSource = DataSourceUtil.getDruidDataSource(target);

        totalSourceSize = new AtomicInteger();
        totalTargetSize = new AtomicInteger();

        countDownLatch = new CountDownLatch(ThreadUtil.AVAILABLE_THREAD);

    }

    public Demo(List<Map<String, String>> sourceList, Map<String, String> target) throws SQLException {

        targetDataSource = DataSourceUtil.getDruidDataSource(target);

        totalSourceSize = new AtomicInteger();
        totalTargetSize = new AtomicInteger();

        countDownLatch = new CountDownLatch(ThreadUtil.AVAILABLE_THREAD);

    }

    /**
     * 开启线程
     */
    public void init() {
        ThreadPoolExecutor threadPool = ThreadUtil.getThreadPool();
        for (int i = 0; i < ThreadUtil.AVAILABLE_THREAD; i++) {
            threadPool.execute(() -> {
                try {
                    migrate();
                } catch (SQLException e) {
                    log.error("{}", e);
                }
            });
        }
    }

    /**
     * 开始迁移，具体逻辑由子类实现
     * @throws SQLException
     */
    public abstract void migrate() throws SQLException;

    public abstract int readSourceData(DruidPooledConnection sourceConnection,
                              List<T> sourceDataList,
                              Object... params) throws SQLException;

    public abstract int writeTargetData(DruidPooledConnection targetConnection, List<T> sourceDataList)
            throws SQLException;

    protected void close(DruidPooledConnection connection) throws SQLException {
        connection.close();
    }

    protected void close(DruidDataSource dataSource) {
        dataSource.close();
    }
}
