package com.ultrakid.ultratools.batch.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对map的批量操作
 *
 * @author ultrakid
 * @version 1.0
 * @date 2020/12/17 10:08
 */
public abstract class MapBatchOperator<K, V> implements BatchOperator<Map<K, V>> {

    private final Map<K, V> originalData;
    private final List<Map.Entry<K, V>> dataList;

    public MapBatchOperator(Map<K, V> originalData) {
        this.originalData = originalData;
        dataList = new ArrayList<>(originalData.entrySet());
    }

    /**
     * 获取原始待操作的数据
     *
     * @return 原始数据
     */
    @Override
    public Map<K, V> oriData() {
        return originalData;
    }

    /**
     * 计算数量
     *
     * @param data 数据
     * @return 数量
     */
    @Override
    public int calcSize(Map<K, V> data) {
        return data.size();
    }

    /**
     * 将大数据分成小数据
     *
     * @param start 起始
     * @param size  数量
     * @return 分解后的小数据
     */
    @Override
    public Map<K, V> toSmallBatch(int start, int size) {
        Map<K, V> smallData = new HashMap<>(size);
        int originalSize = dataList.size();
        int end = Math.min(start + size, originalSize);
        for (int i = start; i < end; i++) {
            Map.Entry<K, V> entry = dataList.get(i);
            smallData.put(entry.getKey(), entry.getValue());
        }
        return smallData;
    }
}
