package com.ultrakid.ultratools.batch;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.ultrakid.ultratools.batch.operator.BatchOperator;
import com.ultrakid.ultratools.batch.operator.ListBatchOperator;
import com.ultrakid.ultratools.common.DefaultThreadFactory;
import com.ultrakid.ultratools.data.RandomUtils;
import com.ultrakid.ultratools.exception.UltraRuntimeException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * 批量操作单元测试
 *
 * @author ultrakid
 * @version 1.0
 * @date 2021/12/29 20:53
 */
class BatchUtilsTest {
    private static final Log LOGGER = LogFactory.get(BatchUtilsTest.class);

    private int batchSize = 100;
    private ThreadFactory threadFactory = new DefaultThreadFactory("BatchUtilsTest");
    private ExecutorService executorService = Executors.newFixedThreadPool(4, threadFactory);

    /**
     * 单线程批量操作单元测试
     */
    @Test
    void batchOperate() {
        List<Integer> numList = RandomUtils.generateList(1000, RandomUtil::randomNumber);
        StringBuilder expectedRes = new StringBuilder();
        for (Integer num : numList) {
            expectedRes.append(num);
        }
        StringBuilder operatedRes = new StringBuilder();
        BatchOperator<List<Integer>> batchOperator = new ListBatchOperator<Integer>(numList) {
            @Override
            public int operate(List<Integer> data) {
                for (Integer num : data) {
                    operatedRes.append(num);
                }
                return data.size();
            }
        };
        long res = BatchUtils.batchOperate(batchOperator, 100);
        assertEquals(numList.size(), res);
        LOGGER.info("Result: {}", operatedRes);
        assertEquals(expectedRes.toString(), operatedRes.toString());
    }

    /**
     * 批量任务中正常运行情况的多线程批量操作单元测试
     *
     * @throws InterruptedException 线程被打断时的异常
     */
    @Test
    void batchParallelOperate() throws InterruptedException {
        List<String> dataList = new ArrayList<>(1000);
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            dataList.add(String.valueOf(i));
            sum += i;
        }
        AtomicInteger sequence = new AtomicInteger(0);
        BatchOperator<List<String>> operator = new ListBatchOperator<String>(dataList) {
            @Override
            public int operate(List<String> data) {
                int res = data.stream().mapToInt(Integer::parseInt).sum();
                LOGGER.info("NO.{} finished the job, first data: {}, data size: {}, res: {}",
                        sequence.getAndIncrement(), data.get(0), data.size(), res);
                return res;
            }
        };
        long res = BatchUtils.batchParallelOperate(operator, batchSize, executorService, true);
        LOGGER.info("BatchParallelOperate res: {}", res);
        assertEquals(sum, res);
    }

    /**
     * 批量任务中存在运行异常的多线程批量操作单元测试
     *
     * @throws InterruptedException 线程被打断时的异常
     */
    @Test
    void batchParallelOperateWithException() throws InterruptedException {
        List<String> dataList = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            dataList.add(String.valueOf(i));
        }
        AtomicInteger sequence = new AtomicInteger(0);
        BatchOperator<List<String>> operator = new ListBatchOperator<String>(dataList) {
            @Override
            public int operate(List<String> data) {
                int res = data.stream().mapToInt(Integer::parseInt).sum();
                int seq = sequence.getAndIncrement();
                if (seq == 5) {
                    LOGGER.info("NO.{} ready to divide zero, first data: {}, data size: {}, res: {}",
                            seq, data.get(0), data.size(), res);
                    return res / 0;
                }
                LOGGER.info("NO.{} finished the job, first data: {}, data size: {}, res: {}",
                        seq, data.get(0), data.size(), res);
                return data.size();
            }
        };

        long res = BatchUtils.batchParallelOperate(operator, batchSize, executorService, true);
        LOGGER.info("BatchParallelOperate ignore exception res: {}", res);
        assertEquals(dataList.size() - batchSize, res);
        sequence.set(0);
        assertThrows(UltraRuntimeException.class,
                () -> BatchUtils.batchParallelOperate(operator, batchSize, executorService, false));
    }
}