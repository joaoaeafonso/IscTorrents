package common;

//TODO(joaoaeafonso): this class has only been implemented, not validated
public class CustomCountdownLatch {

    private int count;

    public CustomCountdownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        this.count = count;
    }

    public synchronized void await() throws InterruptedException {
        while (count > 0) {
            wait();
        }
    }

    public synchronized boolean await(long timeoutMillis) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeoutMillis;
        while (count > 0) {
            long waitTime = endTime - System.currentTimeMillis();
            if (waitTime <= 0) {
                return count == 0;
            }
            wait(waitTime);
        }
        return true;
    }

    public synchronized void countDown() {
        if (count > 0) {
            count--;
            if (count == 0) {
                notifyAll();
            }
        }
    }

    public synchronized int getCount() {
        return count;
    }

}
