package com.ultrakid.ultratools.batch.operator;

import java.util.ArrayList;
import java.util.List;

/**
 * 对list的批量操作
 *
 * @author ultrakid
 * @version 1.0
 * @date 2020/12/17 14:44
 */
public abstract class ListBatchOperator<T> implements BatchOperator<List<T>> {

    private final List<T> originalData;

    public ListBatchOperator(List<T> originalData) {
        this.originalData = originalData;
    }

    /**
     * 获取原始待操作的数据
     *
     * @return 原始数据
     */
    @Override
    public List<T> oriData() {
        return originalData;
    }

    /**
     * 计算数量
     *
     * @param data 数据
     * @return 数量
     */
    @Override
    public int calcSize(List<T> data) {
        return data.size();
    }

    /**
     * 将原始数据分成小数据
     *
     * @param start 起始
     * @param size  数量
     * @return 分解后的小数据
     */
    @Override
    public List<T> toSmallBatch(int start, int size) {
        int originalSize = originalData.size();
        if (start >= originalSize) {
            return new ArrayList<T>();
        }
        int end = Math.min(start + size, originalSize);
        return originalData.subList(start, end);
    }
}
