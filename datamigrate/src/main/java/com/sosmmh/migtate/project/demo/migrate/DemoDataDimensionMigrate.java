package com.sosmmh.migtate.project.demo.migrate;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.sosmmh.migtate.bean.User;
import com.sosmmh.migtate.project.demo.Demo;
import com.sosmmh.migtate.project.demo.SqlUtil;
import com.sosmmh.migtate.utils.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description: 以某个字段的不同值进行切割，比如status有开始进行中结束
 * 每个线程分页读取某个数据源的所有数据
 * @author: lixiahan
 * @create: 2019/07/04 15:50Dimension
 */
@Slf4j
public class DemoDataDimensionMigrate<T> extends Demo<T> {

    public DemoDataDimensionMigrate(Map<String, String> source, Map<String, String> target) throws SQLException {
        super(source, target);
    }

    public DemoDataDimensionMigrate(List<Map<String, String>> sourceList, Map<String, String> target) throws SQLException {
        super(sourceList, target);
    }

    @Override
    public void init() {

        objectQueue = new LinkedBlockingQueue<>();
        objectQueue.add(1);
        objectQueue.add(2);
        objectQueue.add(3);
        super.init();
    }

    @Override
    public void migrate() throws SQLException {

        log.info("Curr Thread = {}", Thread.currentThread().getId());

        Object status = objectQueue.poll();
        if (status == null) {
            return;
        }

        Long page = 0L;
        DruidPooledConnection sourceConnection = sourceDataSource.getConnection();
        DruidPooledConnection targetConnection = targetDataSource.getConnection();

        int threadSourceSize = 0;
        int threadTargetSize = 0;

        List<User> sourceDataList = new ArrayList<>(SqlUtil.PAGE_SIZE);

        try {

            for (;;) {

                // 记录每批次的开始时间
                long start = System.currentTimeMillis();

                int sourceSize = readSourceData(sourceConnection, (List<T>) sourceDataList, status, page);
                log.info("Read Source Data Time = {}", System.currentTimeMillis() - start);

                if (sourceSize != 0) {

                    page += SqlUtil.PAGE_SIZE;
                    threadSourceSize += sourceSize;
                    totalSourceSize.addAndGet(sourceSize);

                    if (CollectionUtil.isEmpty(sourceDataList)) {
                        continue;
                    }

                    int targetSize = writeTargetData(targetConnection, (List<T>) sourceDataList);
                    log.info("write Data Time = {}", System.currentTimeMillis() - start);

                    threadTargetSize += targetSize;
                    totalTargetSize.addAndGet(targetSize);

                    sourceDataList.clear();
                    log.info("read = {}, write = {}, every batch time = {}",
                            sourceSize, targetSize, System.currentTimeMillis() - start);
                }

                if (sourceSize < SqlUtil.PAGE_SIZE) {
                    log.info("该数据类型 = {} ,已空，线程退出，读取条数 = {}, 写入条数 = {}", status, threadSourceSize, threadTargetSize);

                    status = objectQueue.poll();
                    if (status == null) {

                        close(sourceConnection);
                        close(targetConnection);

                        log.info("数据队列已空, currSourceTotal = {}, currTargetTotal = {}, total time = {}",
                                totalSourceSize, totalTargetSize, System.currentTimeMillis() - start);

                        countDownLatch.countDown();
                        if (countDownLatch.getCount() == 0) {
                            close(sourceDataSource);
                            close(targetDataSource);
                            ThreadUtil.shutdown();
                        }
                        return;
                    }

                    page = 0L;
                    threadSourceSize = 0;
                    threadTargetSize = 0;
                }
            }

        } catch (Exception e) {
            log.error("{}", e);
            close(sourceConnection);
            close(targetConnection);
            close(sourceDataSource);
            close(targetDataSource);
            ThreadUtil.shutdown();
        }
    }

    @Override
    public int readSourceData(DruidPooledConnection sourceConnection,
                               List<T> sourceDataList,
                               Object... params) throws SQLException {

        // 读取源数据
        PreparedStatement sourcePS = sourceConnection.prepareStatement(
                SqlUtil.SELECT_FROM_SOURCE_2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        int index = 1;
        for (Object param : params) {
            sourcePS.setObject(index++, param);
        }

        ResultSet sourceResultSet = sourcePS.executeQuery();

        // 封装源数据
        int sourceSize = User.readData(sourceResultSet, (List<User>) sourceDataList);
        sourceResultSet.close();
        sourcePS.close();

        return sourceSize;
    }

    @Override
    public int writeTargetData(DruidPooledConnection targetConnection, List<T> sourceDataList) throws SQLException {

        // 构建批量插入语句
        String batchInsertSql = SqlUtil.buildInsertSql(sourceDataList.size(), SqlUtil.INSERT_TO_TARGET_2);
        PreparedStatement targetPS = targetConnection.prepareStatement(batchInsertSql);

        // 设置目标数据
        User.writeData(targetPS, (List<User>) sourceDataList);

        // 执行插入
        int targetSize = targetPS.executeUpdate();
        targetPS.close();

        return targetSize;
    }

    private void closeConnection(DruidDataSource sourceDataSource, DruidPooledConnection sourceConnection, DruidPooledConnection targetConnection) {
        try {
            close(sourceConnection);
            close(targetConnection);
            close(sourceDataSource);
        } catch (Exception e) {
            log.error("关闭失败{}", e);
        }
    }

}
