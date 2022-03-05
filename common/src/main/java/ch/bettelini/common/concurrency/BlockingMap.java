package ch.bettelini.common.concurrency;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class BlockingMap<K, V> {

    private Map<K, BlockingQueue<V>> map = new ConcurrentHashMap<>();

    public void createQueue(K key) {
        map.put(key, new ArrayBlockingQueue<V>(1));
    }

    public BlockingQueue<V> getQueue(K key) {
        return map.get(key);
    }

    public void put(K key, V value) {
        getQueue(key).add(value);
    }

    public V get(K key) throws InterruptedException {
        return getQueue(key).take();
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

}