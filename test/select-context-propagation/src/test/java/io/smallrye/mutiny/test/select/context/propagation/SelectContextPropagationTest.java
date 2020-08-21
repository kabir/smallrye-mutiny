package io.smallrye.mutiny.test.select.context.propagation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.core.ReactiveStreamsEngineResolver;
import org.eclipse.microprofile.reactive.streams.operators.spi.ReactiveStreamsEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.smallrye.mutiny.streams.Engine;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SelectContextPropagationTest {

    @Before
    public void before() {
        TestProvider.reset();
    }

    @Test
    public void testContextPropagation() throws Exception {
        SimpleSubscriber<Integer> subscriber = new SimpleSubscriber<>();
        ReactiveStreams.of(1, 2, 3)
                .buildRs()
                .subscribe(subscriber);

        Assert.assertNull(subscriber.error);
        checkValues(subscriber.values, 1, 2, 3);
        TestProvider.assertPropagated();
    }

    @Test
    public void testNoContextPropagation() throws Exception {
        ReactiveStreamsEngine engine = ReactiveStreamsEngineResolver.instance();
        if (engine instanceof Engine) {
            engine = ((Engine) engine).addHints("skip.io.smallrye.mutiny.context.ContextPropagationMultiInterceptor", "a");
        }

        //        Set<String>
        //        System.out.println(hints.);

        SimpleSubscriber<Integer> subscriber = new SimpleSubscriber<>();
        ReactiveStreams.of(1, 2, 3)
                .buildRs(engine)
                .subscribe(subscriber);

        Assert.assertNull(subscriber.error);
        checkValues(subscriber.values, 1, 2, 3);
        TestProvider.assertNotInvoked();
    }

    private <T> void checkValues(List<T> values, T... expected) {
        Assert.assertEquals(values.size(), expected.length);
        for (int i = 0; i < values.size(); i++) {
            Assert.assertEquals(expected[i], values.get(i));
        }
    }

    private static class SimpleSubscriber<T> implements Subscriber<T> {
        private final List<T> values = new ArrayList<>();

        private Throwable error;

        private volatile Subscription subscription;

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T t) {
            System.out.println("Got: " + t);
            values.add(t);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
            error = throwable;
            subscription.cancel();
        }

        @Override
        public void onComplete() {
            System.out.println("Complete");
            subscription.cancel();
        }
    }
}
