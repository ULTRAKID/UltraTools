package com.ultrakid.ultratools.data;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 随机工具类
 *
 * @author ultrakid
 * @version 1.0
 * @date 2020/09/17 20:01
 */
public final class RandomUtils {

    private RandomUtils() {

    }

    /**
     * 随机生成指定类型随机数量的数组
     * size范围 [minNum, maxNum)
     *
     * @param minNum    生成的数组长度大于等于该值
     * @param maxNum    生成的数组长度小于该值
     * @param generator 随机生成器
     * @param <T>       生成的数据类型
     * @return 生成结果
     */
    public static <T> List<T> generateList(int minNum, int maxNum, RandomGenerator<T> generator) {
        int size = 0;
        if (minNum > maxNum) {
            size = RandomUtil.randomInt(maxNum, minNum);
        } else if (minNum < maxNum - 1) {
            size = RandomUtil.randomInt(minNum, maxNum);
        } else {
            size = minNum;
        }
        return generateList(size, generator);
    }

    /**
     * 随机生成指定类型指定数量的数组
     *
     * @param num       需要生成的数组长度
     * @param generator 随机生成器
     * @param <T>       生成的数据类型
     * @return 生成结果
     */
    public static <T> List<T> generateList(int num, RandomGenerator<T> generator) {
        List<T> resList = new ArrayList<>();
        if (num <= 0) {
            return resList;
        }
        for (int i = 0; i < num; i++) {
            resList.add(generator.generate());
        }
        return resList;
    }

    /**
     * 从候选数组中随机挑选一个
     *
     * @param candidates 候选数据数组
     * @return 随机挑选结果
     */
    public static <T> T randomPick(T[] candidates) {
        if (candidates == null || candidates.length == 0) {
            return null;
        }
        int index = RandomUtil.randomInt(candidates.length);
        return candidates[index];
    }

    /**
     * 从候选数组中随机挑选一个
     *
     * @param candidates 候选数据数组
     * @return 随机挑选结果
     */
    public static <T> T randomPick(List<T> candidates) {
        if (CollectionUtil.isEmpty(candidates)) {
            return null;
        }
        int index = RandomUtil.randomInt(candidates.size());
        return candidates.get(index);
    }

    /**
     * 随机对象生成器
     *
     * @author ultrakid
     * @version 1.0
     * @date 2020/09/17 20:03
     */
    public interface RandomGenerator<T> {

        /**
         * 生成函数
         *
         * @return 生成结果
         */
        T generate();

    }
}
