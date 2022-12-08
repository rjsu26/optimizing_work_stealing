package hw5.WorkStealing;

import hw5.DEQue.concurrentDeque;
import hw5.utils.ThreadId;
import java.util.concurrent.*;
import hw5.PrimeNumbers;


public class workStealing extends Thread{
    // static Runnable stop = new Runnable();
    private final Thread[] threadPool;
    // Create a deque for each thread
    private concurrentDeque[] taskQueue;
    private int numThreads;

    public workStealing(int numThreads) {
        this.numThreads = numThreads;
        
        taskQueue = new concurrentDeque[numThreads];
        for (int i = 0; i < numThreads; i++)
            taskQueue[i] = new concurrentDeque();
        
        this.threadPool = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threadPool[i] = new Thread(() -> {
                // Get the current thread
                Thread currentThread = Thread.currentThread();
                // Keep executing tasks until the thread pool is shut down
                while (!currentThread.isInterrupted()) {
                    // System.out.println("thread: "+ currentThread.getId()+" running..");
                    // Dequeue a task from the task queue
                    Runnable task = take();
                    while (task != null) {
                        // Execute the task
                        // System.out.println("thread: "+ currentThread.getId() + " "+task.toString());
                        ((PrimeNumbers)task).run();
                        task = take();
                    }
                }
            });
            threadPool[i].start();
        }
    }

    public void submit(Runnable task) {
        // Add the task to the deque of the current thread
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        taskQueue[threadId].pushBottom(task);
    }

    public Runnable take() {
        // Try to take a task from the current thread's deque
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        Runnable task = taskQueue[threadId].popBottom();
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

    public void awaitTermination()  {
        boolean empty = false;
        // check if all taskQueues are empty or not. 
        do{
            for(Thread t : threadPool){
                int threadId = (int)t.getId() % numThreads;
                concurrentDeque tq = taskQueue[threadId];
                empty = tq.isEmpty();
                if(empty==false){
                    break; // found a thread with active tasks. no need to check other threads. just sleep
                }
            }
            try{
                Thread.sleep(1);
            }
            catch (Exception e){
                
            }
        } while(!empty);
        
    }

    // Shutdown the thread pool
    public synchronized void shutdown() {

        awaitTermination(); 
        // all tasks must be complete by now

        // Interrupt all threads in the thread pool
        for (Thread t : threadPool) {
            try{
                
                t.interrupt();
                int threadId = (int)t.getId() % numThreads;
                while (t.isAlive()){
                    Thread.sleep(1);
                }
                // System.out.println("Thread ["+threadId +"] : isAlive? "+t.isAlive());
            }
             catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }
}
