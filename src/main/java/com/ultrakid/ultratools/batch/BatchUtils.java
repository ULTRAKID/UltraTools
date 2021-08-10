package com.ultrakid.ultratools.batch;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.ultrakid.ultratools.batch.operator.BatchOperator;
import com.ultrakid.ultratools.common.DefaultThreadFactory;
import com.ultrakid.ultratools.exception.UltraRuntimeException;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量操作工具类
 *
 * @author ultrakid
 * @version 1.0
 * @date 2020/12/16 19:07
 */
public final class BatchUtils {

    private static final Log LOGGER = LogFactory.get(BatchUtils.class);

    private static final int ALLOWED_MAX_THREADS = 24;

    private static DefaultThreadFactory threadFactory = new DefaultThreadFactory("BatchUtils");

    private static ExecutorService executorService = Executors.newFixedThreadPool(ALLOWED_MAX_THREADS, threadFactory);

    /**
     * 批量操作
     *
     * @param operator  批量操作算符
     * @param batchSize 单批次大小
     * @return 操作成功数
     */
    public static <T> long batchOperate(BatchOperator<T> operator, int batchSize) {
        if (batchSize <= 0) {
            throw new InvalidParameterException("Batch size should be greater than 0, but actual value is " + batchSize);
        }
        T data = operator.oriData();
        int size = operator.calcSize(data);
        if (size <= 0) {
            //如果原始数据为空就不再执行
            return 0L;
        }
        if (size <= batchSize) {
            return operator.operate(data);
        }
        int res = 0;
        int stopIndex = size - batchSize;
        int index = 0;
        for (; index < stopIndex; index += batchSize) {
            T smallData = operator.toSmallBatch(index, batchSize);
            res += operator.operate(smallData);
        }
        int restSize = size - index;
        if (restSize == 0) {
            return res;
        }
        T restData = operator.toSmallBatch(index, restSize);
        res += operator.operate(restData);
        return res;
    }

    /**
     * 使用内置线程池进行并发批量操作
     *
     * @param operator  批量操作算符
     * @param batchSize 单批次大小
     * @param <T>       数据类型
     * @return 操作成功数
     */
    public static <T> long batchParallelOperate(BatchOperator<T> operator, int batchSize)
            throws InterruptedException {
        return batchParallelOperate(operator, batchSize, executorService, true);
    }

    /**
     * 并发批量操作，控制并发量请通过线程池的参数或者batchSize的大小来控制
     *
     * @param operator        批量操作算符
     * @param batchSize       单批次大小
     * @param executorService 操作用的线程池
     * @param ignoreException 线程内有异常时是否抛出,false时抛出
     * @param <T>             数据类型
     * @return 操作成功数
     */
    public static <T> long batchParallelOperate(BatchOperator<T> operator, int batchSize,
                                                ExecutorService executorService, boolean ignoreException)
            throws InterruptedException {
        if (batchSize <= 0) {
            throw new InvalidParameterException("Batch size should be greater than 0, but actual value is " + batchSize);
        }
        T data = operator.oriData();
        int size = operator.calcSize(data);
        if (size <= batchSize) {
            // 数据量较小时直接进行操作
            return operator.operate(data);
        }
        int stopIndex = size - batchSize;
        int index = 0;
        List<StatusJob<T>> jobList = new ArrayList<>(size / batchSize + 1);
        // 提前分批
        for (; index < stopIndex; index += batchSize) {
            T smallData = operator.toSmallBatch(index, batchSize);
            StatusJob<T> statusJob = new StatusJob<>(smallData);
            jobList.add(statusJob);
        }
        int restSize = size - index;
        if (restSize > 0) {
            T restData = operator.toSmallBatch(index, restSize);
            StatusJob<T> statusJob = new StatusJob<>(restData);
            jobList.add(statusJob);
        }
        AtomicInteger res = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(jobList.size());
        jobList.forEach(job ->
                executorService.submit(() -> {
                    try {
                        int successCnt = operator.operate(job.data);
                        res.addAndGet(successCnt);
                    } catch (Exception e) {
                        job.e = e;
                    } finally {
                        job.finished = true;
                        countDownLatch.countDown();
                    }
                }));
        countDownLatch.await();
        jobList.forEach(job -> {
            if (job.e != null) {
                if (ignoreException) {
                    LOGGER.warn("Exception in batch job", job.e);
                } else {
                    throw new UltraRuntimeException(job.e);
                }
            }
        });
        return res.get();
    }

    private static class StatusJob<T> {
        private final T data;
        private boolean finished;
        private Exception e = null;

        public StatusJob(T data) {
            this.data = data;
            this.finished = false;
        }

        public T getData() {
            return data;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public Exception getE() {
            return e;
        }

        public void setE(Exception e) {
            this.e = e;
        }
    }

    private BatchUtils() {

    }
}
