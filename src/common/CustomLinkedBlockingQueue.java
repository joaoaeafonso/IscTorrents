package common;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//TODO(joaoaeafonso): this class has only been implemented, not validated
public class CustomLinkedBlockingQueue<T> {
    private final LinkedList<T> queue = new LinkedList<>();
    private final int capacity;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public CustomLinkedBlockingQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be greater than 0");
        this.capacity = capacity;
    }

    // Adds an item to the queue, blocking if the queue is full
    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // Wait until space is available
            }
            queue.add(item);
            notEmpty.signal(); // Notify a waiting consumer
        } finally {
            lock.unlock();
        }
    }

    // Retrieves and removes the head of the queue, blocking if the queue is empty
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // Wait until an item is available
            }
            T item = queue.removeFirst();
            notFull.signal(); // Notify a waiting producer
            return item;
        } finally {
            lock.unlock();
        }
    }

    // Returns the current size of the queue
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    // Returns the capacity of the queue
    public int getCapacity() {
        return capacity;
    }
}
