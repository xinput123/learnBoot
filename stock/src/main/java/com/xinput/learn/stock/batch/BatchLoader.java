package com.xinput.learn.stock.batch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 通用批处理加载器接口
 *
 * @param <K> 请求的Key类型（如代码）
 * @param <V> 返回的Value类型（如Stock对象）
 */
public interface BatchLoader<K, V> {

    /**
     * 批量加载数据
     *
     * @param keys 需要加载的Key列表
     * @return Key到Value的映射
     */
    Map<K, V> batchLoad(List<K> keys);

    /**
     * 单个加载数据（降级方法，队列满时使用）
     *
     * @param key 需要加载的Key
     * @return 对应的Value
     */
    V singleLoad(K key);

    /**
     * 异步加载单个数据（会被合并到批处理中）
     *
     * @param key 需要加载的Key
     * @return CompletableFuture<V>
     */
    CompletableFuture<V> load(K key);

    /**
     * 同步加载单个数据（会被合并到批处理中）
     *
     * @param key 需要加载的Key
     * @return Value
     */
    V loadSync(K key);
}
