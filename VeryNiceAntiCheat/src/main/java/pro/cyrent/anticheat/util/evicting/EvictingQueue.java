package pro.cyrent.anticheat.util.evicting;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class EvictingQueue<T> implements Deque<T> {
    private final Deque<T> deque;
    private final int maxSize;

    public EvictingQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be greater than 0");
        }
        this.deque = new ArrayDeque<>(maxSize);
        this.maxSize = maxSize;
    }

    public EvictingQueue(Collection<? extends T> c, int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be greater than 0");
        }
        this.deque = new ArrayDeque<>(Math.max(maxSize, c.size()));
        this.maxSize = maxSize;
        addAll(c);
    }

    public synchronized int getMaxSize() {
        return maxSize;
    }

    public synchronized boolean isFull() {
        return deque.size() >= maxSize;
    }

    @Override
    public synchronized boolean add(T element) {
        if (isFull()) {
            deque.pollFirst();  // Evict the oldest element
        }
        return deque.add(element);
    }

    @Override
    public synchronized void addFirst(T element) {
        if (isFull()) {
            deque.pollLast();  // Evict the oldest element
        }
        deque.addFirst(element);
    }

    @Override
    public synchronized void addLast(T element) {
        if (isFull()) {
            deque.pollFirst();  // Evict the oldest element
        }
        deque.addLast(element);
    }

    @Override
    public synchronized boolean offerFirst(T element) {
        if (isFull()) {
            deque.pollLast();  // Evict the oldest element
        }
        return deque.offerFirst(element);
    }

    @Override
    public synchronized boolean offerLast(T element) {
        if (isFull()) {
            deque.pollFirst();  // Evict the oldest element
        }
        return deque.offerLast(element);
    }

    @Override
    public synchronized boolean offer(T element) {
        return add(element);
    }

    @Override
    public synchronized T removeFirst() {
        return deque.removeFirst();
    }

    @Override
    public synchronized T removeLast() {
        return deque.removeLast();
    }

    @Override
    public synchronized T pollFirst() {
        return deque.pollFirst();
    }

    @Override
    public synchronized T pollLast() {
        return deque.pollLast();
    }

    @Override
    public synchronized T getFirst() {
        return deque.getFirst();
    }

    @Override
    public synchronized T getLast() {
        return deque.getLast();
    }

    @Override
    public synchronized T peekFirst() {
        return deque.peekFirst();
    }

    @Override
    public synchronized T peekLast() {
        return deque.peekLast();
    }

    @Override
    public synchronized int size() {
        return deque.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return deque.contains(o);
    }

    @Override
    public synchronized Iterator<T> iterator() {
        return deque.iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return deque.toArray();
    }

    @Override
    public synchronized <T1> T1[] toArray(T1[] a) {
        return deque.toArray(a);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return deque.remove(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return deque.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends T> c) {
        boolean modified = false;
        for (T element : c) {
            modified |= add(element);
        }
        return modified;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return deque.removeAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return deque.retainAll(c);
    }

    @Override
    public synchronized void clear() {
        deque.clear();
    }

    @Override
    public synchronized T remove() {
        return deque.remove();
    }

    @Override
    public synchronized T poll() {
        return deque.poll();
    }

    @Override
    public synchronized T element() {
        return deque.element();
    }

    @Override
    public synchronized T peek() {
        return deque.peek();
    }

    @Override
    public synchronized void push(T element) {
        addFirst(element);
    }

    @Override
    public synchronized T pop() {
        return removeFirst();
    }

    @Override
    public synchronized Iterator<T> descendingIterator() {
        return deque.descendingIterator();
    }

    @Override
    public synchronized boolean removeFirstOccurrence(Object o) {
        return deque.removeFirstOccurrence(o);
    }

    @Override
    public synchronized boolean removeLastOccurrence(Object o) {
        return deque.removeLastOccurrence(o);
    }
}