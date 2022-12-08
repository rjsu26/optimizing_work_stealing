package hw5.WorkStealing;

import hw5.DEQue.concurrentDeque;
import hw5.utils.ThreadId;

import java.util.Random;
import java.util.concurrent.*;
import hw5.PrimeNumbers;

public class workStealing extends Thread {
    // static Runnable stop = new Runnable();
    private final Thread[] threadPool;
    // Create a deque for each thread
    private concurrentDeque[] taskQueue;
    private int numThreads;
    private static String stealing_algo; // firstAvailable, bestOfTwoRandom, bestofAll, smartStealing
    private ThreadLocal<Integer> last_used_thread = ThreadLocal.withInitial(() -> -1); // used by smartStealing algo to
                                                                                       // save threadID having deepest
                                                                                       // taskQueue for repeated
                                                                                       // stealing.

    public workStealing(int numThreads, String steal_algo) {
        this.numThreads = numThreads;
        stealing_algo = steal_algo;

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
                        ((PrimeNumbers) task).run();
                        task = take();
                    }
                }
            });
            threadPool[i].start();
        }
    }

    public workStealing(int numThreads) {
        this(numThreads, "firstAvailable");
    }

    public void submit(Runnable task) {
        // Add the task to the deque of the current thread
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        taskQueue[threadId].pushBottom(task);

        // Select a thread from random and push to it.
        // int randomThread = ThreadLocalRandom.current().nextInt(0, numThreads);
        // System.out.println("Pushing "+task.toString() + " to thread : "+
        // randomThread);
        // taskQueue[randomThread].pushBottom(task);
    }

    public Runnable take() {
        // Try to take a task from the current thread's deque
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        Runnable task = taskQueue[threadId].popBottom();
        if (task != null) {
            return task;
        }

        // If the current thread's deque is empty, try to steal a task from another
        // thread's deque. If steal fails, we will obtain null.
        return steal();

    }

    public Runnable steal() {
        switch (stealing_algo) {
            case "firstAvailable":
                return steal_firstAvailable();
            case "bestOfTwoRandom":
                return steal_bestOfTwoRandom();
            case "bestofAll":
                return steal_bestofAll();
            case "smartStealing":
                return steal_smartStealing();

            default:
                System.err.println(stealing_algo + " not found");
                return null;
        }
    }

    private Runnable steal_firstAvailable() {
        // iteratively checking all threads for work
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        Runnable task;
        for (int i = 0; i < numThreads; i++) {
            if (i != threadId && taskQueue[i].isEmpty() == false) {
                task = taskQueue[i].popTop();
                if (task != null)
                    return task;
            }
        }
        return null;
    }

    private Runnable steal_bestOfTwoRandom() {
        int myThreadId = (int) Thread.currentThread().getId() % numThreads;

        // get two other random threads
        int randomThread1 = ThreadLocalRandom.current().nextInt(0, numThreads);
        int randomThread2 = ThreadLocalRandom.current().nextInt(0, numThreads);

        if (randomThread1 == myThreadId)
            randomThread1 = (randomThread1 + 1) % numThreads;

        if (randomThread2 == myThreadId)
            randomThread2 = (randomThread1 + numThreads - 1) % numThreads;

        Runnable task = null;

        // fetch a task from busier of the two queues
        // while(task==null){
        if (taskQueue[randomThread1].size() > taskQueue[randomThread2].size())
            task = taskQueue[randomThread1].popTop();

        else
            task = taskQueue[randomThread2].popTop();

        // }
        return task;
    }

    private Runnable steal_bestofAll() {
        // iteratively checking all threads for queue depth. At the end, the thread
        // having deepesth taskQueue will get a task stolen
        int threadId = (int) Thread.currentThread().getId() % numThreads;
        Runnable task = null;
        int best_size = 0;
        for (int i = 0; i < numThreads; i++) {
            if (i != threadId && taskQueue[i].size() > best_size) {
                task = taskQueue[i].popTop();
                best_size = taskQueue[i].size();
            }
        }
        return task;
    }

    private Runnable steal_smartStealing() {
        if (last_used_thread.get() == -1) {
            // find threadID with deepest taskQueue
            int threadId = (int) Thread.currentThread().getId() % numThreads;
            int deepestThread = -1, best_size = 0;

            for (int i = 0; i < numThreads; i++) {
                if (i != threadId && taskQueue[i].size() > best_size) {
                    deepestThread = i;
                    best_size = taskQueue[i].size();
                }
            }
            last_used_thread.set(deepestThread);
        }
        if (last_used_thread.get() != -1) {
            // get the deepest queue and repeatedly
            if (taskQueue[last_used_thread.get()].isEmpty() == true) {
                // the taskQueue became empty (when smartStealing is called again and above if
                // condition is not executed, it might happen that the taskQueue became empty)
                // ==> reset this value to -1
                last_used_thread.set(-1);
            } else {
                return taskQueue[last_used_thread.get()].popTop();
            }
        }
        return null;
    }

    public void awaitTermination() {
        boolean empty = false;
        // check if all taskQueues are empty or not.
        do {
            for (Thread t : threadPool) {
                int threadId = (int) t.getId() % numThreads;
                concurrentDeque tq = taskQueue[threadId];
                empty = tq.isEmpty();
                if (empty == false) {
                    break; // found a thread with active tasks. no need to check other threads. just sleep
                }
            }
            try {
                Thread.sleep(1);
            } catch (Exception e) {

            }
        } while (!empty);

    }

    // Shutdown the thread pool
    public synchronized void shutdown() {

        awaitTermination();
        // all tasks must be complete by now

        // Interrupt all threads in the thread pool
        for (Thread t : threadPool) {
            try {

                t.interrupt();
                int threadId = (int) t.getId() % numThreads;
                while (t.isAlive()) {
                    Thread.sleep(1);
                }
                // System.out.println("Thread ["+threadId +"] : isAlive? "+t.isAlive());
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }
}
