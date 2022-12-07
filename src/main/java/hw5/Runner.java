package hw5;

import java.util.*;
import java.util.concurrent.*;

import hw5.PrimeNumbers;

import hw5.WorkStealing.workStealing;

public class Runner {

    public static void main(String[] args) throws Throwable {
        PrimeNumbers primes = new PrimeNumbers(10000);
        // ForkJoinPool pool = ForkJoinPool.commonPool(); 
        // create custom forkJoinPool object here 
        workStealing<PrimeNumbers> pool = new workStealing<>(4);
        pool.submit(primes);
        pool.shutdown(); 
        System.out.println(primes.noOfPrimeNumbers());
        // System.out.println(pool.getStealCount());
        System.out.println("Complete");
        return;
    }
}
