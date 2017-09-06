package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RetryOperator<RQ, RS> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryOperator.class);

    private RetryTemplate retryTemplate;

    public RetryOperator(final int maxAttempts, final int delay, final Class<? extends RuntimeException>... exceptions) {

         this.retryTemplate = new RetryTemplate();

        Map<Class<? extends Throwable>, Boolean> exceptionMap = Stream.of(exceptions)
                .collect(Collectors.toMap(exception -> exception, exception -> true));

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, exceptionMap);
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(delay);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
    }

    public RS executeAndRetryIfFail(final Function<RQ, RS> executeFunction, final RQ request) {

        RS response = retryTemplate.execute(
                context -> {
                    LOGGER.info(String.format("Executing attempt %d", context.getRetryCount() + 1));
                    return executeFunction.apply(request);
                }
        );
        return response;
    }

    public RS executeAndRetryIfFail(final Function<RQ, RS> executeFunction, final RQ request, final Class<? extends RuntimeException> throwableClass, final String... retryMessages) {
        RS response = null;
        try {
            response = retryTemplate.execute(
                    context -> {
                        if (context.getLastThrowable() == null || Stream.of(retryMessages).collect(Collectors.toList()).contains(context.getLastThrowable().getMessage())) {
                            LOGGER.info(String.format("Executing attempt %d", context.getRetryCount() + 1));
                            return executeFunction.apply(request);
                        } else {
                            RuntimeException throwable = throwableClass.newInstance();
                            throwable.setStackTrace(context.getLastThrowable().getStackTrace());
                            throw throwable;
                        }
                    }
            );
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Error on create the throwable exception class", e);
        }
        return response;
    }

    public RS executeAndRetryIfFail(final Function<RQ, RS> executeFunction, final Function<RQ, RS> recoveryFunction, final RQ request, final String errorMessage) {

        RS response = retryTemplate.execute(
                context -> {
                    LOGGER.info(String.format("Executing attempt %d", context.getRetryCount() + 1));
                    return executeFunction.apply(request);
                },
                context -> {
                    LOGGER.error(String.format(errorMessage + " Exception: %s", context.getLastThrowable()));
                    return recoveryFunction.apply(request);
                }
        );
        return response;
    }
}