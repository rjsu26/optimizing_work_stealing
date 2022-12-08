package hw5;

// Java program to demonstrate the working of Fork/Join Framework

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import hw5.WorkStealing.workStealing;

public class PrimeNumbers implements Runnable {

    private int startNum;
    private int endNum;
    private int THRESHOLD;
    private AtomicInteger noOfPrimeNumbers;
    private static workStealing pool;

    PrimeNumbers(workStealing pools, int start, int end, int threshold, AtomicInteger noOfPrimeNumbers) {
        pool = pools;
        this.startNum = start;
        this.endNum = end;
        this.THRESHOLD = threshold;
        this.noOfPrimeNumbers = noOfPrimeNumbers;
    }

    PrimeNumbers(workStealing pool, int end) {
        this(pool, 1, end, 100, new AtomicInteger(0));
    }

    private PrimeNumbers(workStealing pool, int start, int end, AtomicInteger noOfPrimeNumbers) {
        this(pool, start, end, 100, noOfPrimeNumbers);
    }


    protected void compute() {
        // System.out.println("Compute called..");
        if (((endNum + 1) - startNum) > THRESHOLD) {
            int mid = (startNum + endNum) / 2;
            PrimeNumbers task1 = new PrimeNumbers(pool, startNum, mid, noOfPrimeNumbers);
            PrimeNumbers task2 = new PrimeNumbers(pool,mid + 1, endNum, noOfPrimeNumbers);
            pool.submit(task1);
            pool.submit(task2);
        } else {
            // System.out.println("Seq for : "+startNum+" - "+endNum);
            findPrimeNumbers();
        }
    }

    public void run(){
        compute();
    }

    void findPrimeNumbers() {
        for (int num = startNum; num <= endNum; num++) {
            if (isPrime(num)) {
                noOfPrimeNumbers.getAndIncrement();
            }
        }
    }

    private boolean isPrime(int number) {
        if (number == 2) {
            return true;
        }

        if (number == 1 || number % 2 == 0) {
            return false;
        }

        for (int i = 2; i*i <= number; i++) {
            if (number % i == 0) {
                return false;
            }
        }

        return true;
    }

    public int noOfPrimeNumbers() {
        return noOfPrimeNumbers.intValue();
    }
}