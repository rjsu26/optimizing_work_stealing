package hw5;

import java.util.*;
import java.util.concurrent.*;

import hw5.PrimeNumbers;

import hw5.WorkStealing.workStealing;

public class Runner {
    private static final int ITERATIONS = 100;
    private static final int WARMUP = 10;

    public static void main(String[] args) throws Throwable {
        workStealing pool = new workStealing(8);
        PrimeNumbers primes;
        long startTime = 0;

        for (int i = 0; i < WARMUP+ITERATIONS; i++) {
            if(i==WARMUP) // WARMUP complete --> start time
                startTime = System.nanoTime();
            primes = new PrimeNumbers(pool, 1000);
            primes.compute();
            pool.awaitTermination();
        }
        long endTime = System.nanoTime();

        long runtime = endTime - startTime;
        System.out.println("Runtime: " + runtime + " nanos");   
        System.out.println("Throughput: " + (long)(ITERATIONS*1e9)/runtime);   

        pool.shutdown(); 
        Thread.sleep(100);
        return;
    }
}
