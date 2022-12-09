package hw5;

import java.util.*;
import java.util.concurrent.*;

public class PrimeNumberBaseline implements Runnable {
    private final int start;
    private final int end;
    private int count;

    public PrimeNumberBaseline(int start, int end) {
        this.start = start;
        this.end = end;
        this.count = 0;
    }

    public void run() {
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) {
                count++;
            }
        }
    }

    public int getCount() {
        return count;
    }

    private boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int range = 1000;
        final int ITERATIONS = 100;
        final int WARMUP = 10;
        final int numThreads = 8;

        long runtime = 0;
        long startTime = 0;
        for (int i = 0; i < WARMUP + ITERATIONS; i++) {
            if (i == WARMUP) { // WARMUP complete --> reset all timers
                runtime = 0;
            }
            // Create an executor with 8 threads
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);

            // Create a list to hold our tasks
            List<PrimeNumberBaseline> tasks = new ArrayList<>();
            startTime = System.nanoTime();

            // Divide the range into 8 subranges
            int step = range / 8;
            for (int j = 0; j < range; j += step) {
                int start = j + 1;
                int end = Math.min(j + step, range);
                PrimeNumberBaseline task = new PrimeNumberBaseline(start, end);
                tasks.add(task);
                executor.submit(task);
            }
            // Wait for all tasks to finish
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            runtime += System.nanoTime() - startTime;
        }

        // Get the total count of prime numbers
        // int count = 0;
        // for (PrimeNumberBaseline task : tasks) {
        // count += task.getCount();
        // }
        // System.out.println("Total prime count: " + count);

        System.out.println("--------------------------------");
        System.out.println("Number Threads : " + numThreads);
        System.out.println("--------------------------------\n");
        System.out.println("Throughput: " + (long) (ITERATIONS * 1e9) / runtime);
        System.out.println("Runtime: " + runtime / 1e6 + " millis");
    }
}
