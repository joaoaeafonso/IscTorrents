package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//TODO(joaoaeafonso): this class has only been implemented, not validated
public class CustomCopyOnWriteArrayList <E> implements Iterable<E> {
    private volatile List<E> internalList;

    public CustomCopyOnWriteArrayList() {
        internalList = new ArrayList<>();
    }

    public CustomCopyOnWriteArrayList(List<E> list) {
        internalList = new ArrayList<>(list);
    }

    public synchronized void add(E element) {
        List<E> newList = new ArrayList<>(internalList);
        newList.add(element);
        internalList = newList;
    }

    public synchronized void addAll(List<E> elements) {
        List<E> newList = new ArrayList<>(internalList);
        newList.addAll(elements);
        internalList = newList;
    }

    public synchronized void remove(E element) {
        List<E> newList = new ArrayList<>(internalList);
        newList.remove(element);
        internalList = newList;
    }

    public E get(int index) {
        return internalList.get(index);
    }

    public int size() {
        return internalList.size();
    }

    public List<E> getSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(internalList));
    }

    @Override
    public Iterator<E> iterator() {
        return getSnapshot().iterator();
    }
}
