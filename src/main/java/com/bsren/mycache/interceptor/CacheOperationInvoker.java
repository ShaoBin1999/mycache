package com.bsren.mycache.interceptor;

import org.springframework.lang.Nullable;

/**
 * Abstract the invocation of a cache operation.
 *
 * <p>Does not provide a way to transmit checked exceptions but
 * provide a special exception that should be used to wrap any
 * exception that was thrown by the underlying invocation.
 * Callers are expected to handle this issue type specifically.
 *
 * @author Stephane Nicoll
 * @since 4.1
 */
@FunctionalInterface
public interface CacheOperationInvoker {

    @Nullable
    Object invoke() throws ThrowableWrapper;

    /**
     * Wrap any exception thrown while invoking {@link #invoke()}.
     */
    @SuppressWarnings("serial")
    class ThrowableWrapper extends RuntimeException {

        private final Throwable original;

        public ThrowableWrapper(Throwable original) {
            super(original.getMessage(), original);
            this.original = original;
        }

        public Throwable getOriginal() {
            return this.original;
        }
    }
}

