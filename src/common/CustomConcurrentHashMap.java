package common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//TODO(joaoaeafonso): this class has only been implemented, not validated
public class CustomConcurrentHashMap<K, V> {
    private final int BUCKET_COUNT = 16;
    private final List<Bucket<K, V>> buckets;

    public CustomConcurrentHashMap() {
        buckets = new ArrayList<>(BUCKET_COUNT);
        for (int i = 0; i < BUCKET_COUNT; i++) {
            buckets.add(new Bucket<>());
        }
    }

    private int getBucketIndex(K key) {
        return Math.abs(key.hashCode() % BUCKET_COUNT);
    }

    public V put(K key, V value) {
        int bucketIndex = getBucketIndex(key);
        return buckets.get(bucketIndex).put(key, value);
    }

    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        return buckets.get(bucketIndex).get(key);
    }

    public V remove(K key) {
        int bucketIndex = getBucketIndex(key);
        return buckets.get(bucketIndex).remove(key);
    }

    private static class Bucket<K, V> {
        private final List<Entry<K, V>> entries = new ArrayList<>();
        private final Lock lock = new ReentrantLock();

        public V put(K key, V value) {
            lock.lock();
            try {
                for (Entry<K, V> entry : entries) {
                    if (entry.key.equals(key)) {
                        V oldValue = entry.value;
                        entry.value = value;
                        return oldValue;
                    }
                }
                entries.add(new Entry<>(key, value));
                return null;
            } finally {
                lock.unlock();
            }
        }

        public V get(K key) {
            lock.lock();
            try {
                for (Entry<K, V> entry : entries) {
                    if (entry.key.equals(key)) {
                        return entry.value;
                    }
                }
                return null;
            } finally {
                lock.unlock();
            }
        }

        public V remove(K key) {
            lock.lock();
            try {
                for (int i = 0; i < entries.size(); i++) {
                    Entry<K, V> entry = entries.get(i);
                    if (entry.key.equals(key)) {
                        V value = entry.value;
                        entries.remove(i);
                        return value;
                    }
                }
                return null;
            } finally {
                lock.unlock();
            }
        }
    }

    private static class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
