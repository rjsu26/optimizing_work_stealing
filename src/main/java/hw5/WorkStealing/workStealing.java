package hw5.WorkStealing;

import hw5.DEQue.concurrentDeque;
import hw5.utils.ThreadId;
import java.util.concurrent.*;
import hw5.PrimeNumbers;


public class workStealing<T extends RecursiveAction> extends Thread{
    // static RecursiveAction stop = new RecursiveAction();
    private final Thread[] threadPool;
    // Create a deque for each thread
    private concurrentDeque<T>[] taskQueue;
    private int numThreads;

    public workStealing(int numThreads) {
        this.numThreads = numThreads;
        
        taskQueue = new concurrentDeque[numThreads];
        for (int i = 0; i < numThreads; i++)
            taskQueue[i] = new concurrentDeque<T>();
        
        this.threadPool = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threadPool[i] = new Thread(() -> {
                // Get the current thread
                Thread currentThread = Thread.currentThread();
                // Keep executing tasks until the thread pool is shut down
                while (!currentThread.isInterrupted()) {
                    // System.out.println("thread: "+ currentThread.getId()+" running..");
                    // Dequeue a task from the task queue
                    T task = take();
                    while (task != null) {
                        // Execute the task
                        System.out.println("thread: "+ currentThread.getId() + " "+task.toString());
                        ((PrimeNumbers)task).run();
                        task = take();
                    }
                }
            });
            threadPool[i].start();
        }
    }

    public void submit(T task) {
        // Add the task to the deque of the current thread
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        taskQueue[threadId].pushBottom(task);
    }

    public T take() {
        // Try to take a task from the current thread's deque
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        T task = taskQueue[threadId].popBottom();
        if (task != null) {
            return task;
        }

        // If the current thread's deque is empty, try to steal a task from another
        // thread's deque
        for (int i = 0; i < numThreads; i++) {
            if (i != threadId && taskQueue[i].isEmpty() == false){
                task = taskQueue[i].popTop();
                if (task != null) 
                    return task;  
            }
        }

        // If no tasks are available, return null
        return null;
    }

    // Shutdown the thread pool
    public synchronized void shutdown() {

        boolean empty=false;
        // check if all taskQueues are empty or not. 
        while(!empty){
            for(Thread t : threadPool){
                int threadId = (int)t.getId() % numThreads;
                concurrentDeque<T> tq = taskQueue[threadId];
                empty = tq.isEmpty();
                System.out.println("Thread ["+threadId+"] : empty : "+empty);
                if(empty==false)
                    break;
            }
            try{
                Thread.sleep(100);
            }
            catch (Exception e){
                System.err.println("Thread sleep failed");
            }
        }
        // Interrupt all threads in the thread pool
        for (Thread t : threadPool) {
            try{

                // System.out.println(t.isInterrupted());
                t.interrupt();
                // System.out.println(t.getId());
                // System.out.println(t.isAlive());
                // System.out.println(t.isInterrupted());
            }
             catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }
}
