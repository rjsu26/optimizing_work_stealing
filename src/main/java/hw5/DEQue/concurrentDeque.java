package hw5.DEQue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/* Unbounded concurrent deque implementation from the textbook */

public class concurrentDeque {
    private final static int LOG_CAPACITY = 4;
    private volatile CircularArray tasks;
    volatile int bottom;
    AtomicReference<Integer> top;

    public concurrentDeque(int logCapacity) {
        tasks = new CircularArray(logCapacity);
        top = new AtomicReference<Integer>(0);
        bottom = 0;
    }

    public concurrentDeque(){
        this(10);
    }

    public int size(){
        int oldTop = top.get();
        int oldBottom = bottom;
        int size = oldBottom - oldTop;
        return size;
    }

    public boolean isEmpty() {
        int localTop = top.get();
        int localBottom = bottom;
        return (localBottom <= localTop);
    }

    public void pushBottom(Runnable r) {
        int oldBottom = bottom;
        int oldTop = top.get();
        CircularArray currentTasks = tasks;
        int size = oldBottom - oldTop;
        if (size >= currentTasks.capacity() - 1) {
            currentTasks = currentTasks.resize(oldBottom, oldTop);
            tasks = currentTasks;
        }
        currentTasks.put(oldBottom, r);
        bottom = oldBottom + 1;
    }

    public Runnable popTop() {
        int oldTop = top.get();
        int newTop = oldTop + 1;
        int oldBottom = bottom;
        CircularArray currentTasks = tasks;
        int size = oldBottom - oldTop;
        if (size <= 0)
            return null;
        Runnable r = currentTasks.get(oldTop);
        if (top.compareAndSet(oldTop, newTop))
            return r;
        return null;
    }

    public Runnable popBottom() {
        int newBottom = --bottom;
        int oldTop = top.get();
        int newTop = oldTop + 1;
        int size = newBottom - oldTop;
        if (size < 0) {
            bottom = oldTop;
            return null;
        }
        Runnable r = tasks.get(newBottom);
        if (size > 0)
            return r;
        if (!top.compareAndSet(oldTop, newTop))
            r = null;
        bottom = newTop;
        return r;
    }
}

class CircularArray {
    private int logCapacity;
    private Runnable[] currentTasks;

    CircularArray(int logCapacity) {
        this.logCapacity = logCapacity;
        currentTasks = new Runnable[1 << logCapacity];
    }

    int capacity() {
        return 1 << logCapacity;
    }

    Runnable get(int i) {
        return currentTasks[i % capacity()];
    }

    void put(int i, Runnable task) {
        currentTasks[(capacity()+i) % capacity()] = task;
    }

    CircularArray resize(int bottom, int top) {
        CircularArray newTasks = new CircularArray(logCapacity + 1);
        for (int i = top; i < bottom; i++) {
            newTasks.put(i, get(i));
        }
        return newTasks;
    }
}