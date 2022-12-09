package hw5;

import java.util.*;
import java.util.concurrent.*;

import hw5.PrimeNumbers;

import hw5.WorkStealing.workStealing;

public class Runner {
    private static final int ITERATIONS = 100;
    private static final int WARMUP = 10;
    private static final int numThreads = 1;
    private static final String steal_algo = "firstAvailable"; //firstAvailable, bestOfTwoRandom , bestofAll, memoStealing

    public static void main(String[] args) throws Throwable {
        workStealing pool = new workStealing(numThreads, steal_algo);
        PrimeNumbers primes;
        long runtime = 0, totalStealTime=0, totalStealAttempt=0, totalStealSuccess=0;
        long startTime = 0;

        for (int i = 0; i < WARMUP+ITERATIONS; i++) {
            if(i==WARMUP){ // WARMUP complete --> reset all timers
                runtime = 0;
                totalStealTime=0;
                totalStealAttempt=0;
                totalStealSuccess=0;
            }
            
            primes = new PrimeNumbers(pool, 1000);
            startTime = System.nanoTime();
            primes.compute();
            pool.awaitTermination();
            runtime += System.nanoTime() - startTime;
            totalStealTime += pool.getStealTime();
            totalStealAttempt+=pool.getStealAttempt();
            totalStealSuccess += pool.getStealSuccess();
        }
        System.out.println("--------------------------------");
        System.out.println("Number Threads : "+numThreads);
        System.out.println("Steal algo used : "+steal_algo);
        System.out.println("--------------------------------\n");
        System.out.println("Throughput: " + (long)(ITERATIONS*1e9)/runtime);   
        System.out.println("Runtime: " + runtime/1e6 + " millis");   
        System.out.println("Time spent on stealing per thread: "+((totalStealTime*100/ITERATIONS/numThreads)/runtime)+" % of total runtime");
        System.out.println("Total Steal Attempts : "+ totalStealAttempt);
        System.out.println("Total Steal Success : "+ totalStealSuccess );
        System.out.println("Steal success x1000 : "+ (totalStealSuccess*1000.0*100)/totalStealAttempt + " %");

        pool.shutdown(); 
        // Thread.sleep(100);
        return;
    }
}
