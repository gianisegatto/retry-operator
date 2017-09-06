package com.example;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

public class RetryOperatorTest {

    private RetryOperator<Integer, Integer> retryOperator;

    private int count;

    @Before
    public void setup() {

        this.count = 0;

        int[] x = new int[] {1, 2, 3};

        this.retryOperator = new RetryOperator<>(3, 0, RuntimeException.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldRetryThreeTimesAndThrowRuntimeException() {
        int request = 0;
        retryOperator.executeAndRetryIfFail(this::throwExternalSupplierException, request);
    }

    @Test
    public void shouldRetryTwoTimesAndSuccessExecution() {

        int request = 100;
        Integer response = retryOperator.executeAndRetryIfFail(this::retryTwoTimes, request);

        assertThat(response, is(request));
        assertThat(count, is(2));
    }

    @Test(expected = RuntimeException.class)
    public void shouldRetryOneTimeAndStopExecutionBasedOnErrorMessage() {
        int request = 100;
        try {

            retryOperator.executeAndRetryIfFail(this::retryByMessage, request, RuntimeException.class, "Keep going");

        } catch (RuntimeException e) {
            assertThat(count, is(1));
            throw e;
        }
    }

    @Test
    public void shouldRetryThreeTimesAndExecuteRecover() {

        int request = 5;
        Integer response = retryOperator.executeAndRetryIfFail(this::throwExternalSupplierException, request1 -> request1 * 10, request, "Error after three attempts.");

        assertThat(response, is(50));
        assertThat(count, is(3));
    }

    private int throwExternalSupplierException(int request) {
        count++;
        throw new RuntimeException("");
    }

    private int retryTwoTimes(int request) {
        count++;
        if (count < 2) {
            throw new RuntimeException("");
        }
        return request;
    }

    private int retryByMessage(int request) {
        count++;
        if (count < 1) {
            throw new RuntimeException("Keep going");
        } else {
            throw new RuntimeException("Stop going");
        }
    }
}