package com.ultrakid.ultratools.batch.operator;

/**
 * 批量操作，超过设定批次大小则分批进行
 *
 * @author ultrakid
 * @version 1.0
 * @date 2020/12/16 18:46
 */
public interface BatchOperator<T> {

    /**
     * 获取原始待操作的数据
     *
     * @return 原始数据
     */
    T oriData();

    /**
     * 计算数量
     *
     * @param data 数据
     * @return 数量
     */
    int calcSize(T data);

    /**
     * 将原始数据分成小数据
     *
     * @param start 起始
     * @param size  数量
     * @return 分解后的小数据
     */
    T toSmallBatch(int start, int size);

    /**
     * 对一份数据的操作
     *
     * @return 操作成功数
     */
    int operate(T data);
}
