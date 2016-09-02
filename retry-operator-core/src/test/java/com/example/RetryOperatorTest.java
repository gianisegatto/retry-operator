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

        this.retryOperator = new RetryOperator(3, 0, RuntimeException.class, RuntimeException.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldRetryThreeTimesAndThrowRuntimeException() {
        int request = 0;
        retryOperator.executeAndRetryIfFail(request1 -> throwExternalSupplierException(request1), request);
    }

    @Test
    public void shouldRetryTwoTimesAndSuceesExecution() {

        int request = 100;
        Integer response = retryOperator.executeAndRetryIfFail(request1 -> retryTwoTimes(request1), request);

        assertThat(response, is(request));
        assertThat(count, is(2));
    }

    @Test(expected = RuntimeException.class)
    public void shouldRetryOneTimeAndStopExecutionBasedOnErrorMeessage() {
        int request = 100;
        try {

            retryOperator.executeAndRetryIfFail(request1 -> retryByMessage(request1), request, RuntimeException.class, "Keep going");

        } catch (RuntimeException e) {
            assertThat(count, is(1));
            throw e;
        }
    }

    @Test
    public void shouldRetryThreeTimesAndExecuteRecover() {

        int request = 5;
        Integer response = retryOperator.executeAndRetryIfFail(request1 -> throwExternalSupplierException(request1), request1 -> request1 * 10, request, "Error after three attempts.");

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