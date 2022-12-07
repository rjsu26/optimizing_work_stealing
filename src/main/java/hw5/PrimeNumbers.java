package hw5;

// Java program to demonstrate the working of Fork/Join Framework

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

public class PrimeNumbers extends RecursiveAction {

    private int startNum;
    private int endNum;
    private int THRESHOLD;
    private AtomicInteger noOfPrimeNumbers;

    PrimeNumbers(int start, int end, int threshold, AtomicInteger noOfPrimeNumbers) {
        this.startNum = start;
        this.endNum = end;
        this.THRESHOLD = threshold;
        this.noOfPrimeNumbers = noOfPrimeNumbers;
    }

    PrimeNumbers(int end) {
        this(1, end, 100, new AtomicInteger(0));
    }

    private PrimeNumbers(int start, int end, AtomicInteger noOfPrimeNumbers) {
        this(start, end, 100, noOfPrimeNumbers);
    }


    @Override
    protected void compute() {
        // System.out.println("Compute called..");
        if (((endNum + 1) - startNum) > THRESHOLD) {
            int mid = (startNum + endNum) / 2;
            PrimeNumbers task1 = new PrimeNumbers(startNum, mid, noOfPrimeNumbers);
            PrimeNumbers task2 = new PrimeNumbers(mid + 1, endNum, noOfPrimeNumbers);
            task1.fork();
            task2.compute();
            task1.join();
        } else {
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
                // System.out.println("primes : "+ noOfPrimeNumbers());
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

        int noOfNaturalNumbers = 0;

        for (int i = 1; i <= number; i++) {
            if (number % i == 0) {
                noOfNaturalNumbers++;
            }
        }

        return noOfNaturalNumbers == 2;
    }

    public int noOfPrimeNumbers() {
        return noOfPrimeNumbers.intValue();
    }
}