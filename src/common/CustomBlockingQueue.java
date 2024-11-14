package common;

import java.util.LinkedList;

//TODO(joaoaeafonso): this class has only been implemented, not validated
public class CustomBlockingQueue<T> {

    private final LinkedList<T> m_queue;
    private final int m_capacity;

    public CustomBlockingQueue(int capacity) {
        this.m_queue = new LinkedList<>();
        this.m_capacity = capacity;
    }

    public synchronized void put(T element) throws InterruptedException {
        while(m_queue.size() == m_capacity){
            wait();
        }
        m_queue.addLast(element);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        while(this.m_queue.isEmpty()){
            wait();
        }

        notifyAll();
        return this.m_queue.removeFirst();
    }

}
