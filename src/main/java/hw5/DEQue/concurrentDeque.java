package hw5.DEQue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;


public class concurrentDeque<T> {
    T[] tasks;
    volatile int bottom;
    AtomicStampedReference<Integer> top;

    public concurrentDeque(int capacity) {
        tasks = (T[])new Object[capacity];
        top = new AtomicStampedReference<Integer>(0, 0);
        bottom = 0;
    }
    public concurrentDeque() {
        this(10);
    }
    
    public void pushBottom(T r){
        tasks[bottom] = r;
        bottom++;
    }
    
    // called by thieves to determine whether to try to steal
    public boolean isEmpty() {
        return (top.getReference() < bottom);
    }
    
    public T popTop() {
        int[] stamp = new int[1];
        int oldTop = top.get(stamp);
        int newTop = oldTop + 1;
        int oldStamp = stamp[0];
        if (bottom <= oldTop)
          return null;
          T r = tasks[oldTop];
        
        if (top.compareAndSet(oldTop, newTop, oldStamp, oldStamp))
            return r;
        else
            return null;
    }
        
    public T popBottom() {
        if (bottom == 0)
            return null;
        int newBottom = --bottom;
        T r = tasks[newBottom];
        int[] stamp = new int[1];
        int oldTop = top.get(stamp);
        int newTop = 0;
        int oldStamp = stamp[0];
        int newStamp = oldStamp + 1;
        
        if (newBottom > oldTop)
            return r;
        if (newBottom == oldTop) {
            bottom = 0;
            if (top.compareAndSet(oldTop, newTop, oldStamp, newStamp))
                return r;
        }
        top.set(newTop, newStamp);
        return null;
    }
}