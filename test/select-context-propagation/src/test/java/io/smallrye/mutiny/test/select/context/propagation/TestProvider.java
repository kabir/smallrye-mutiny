package io.smallrye.mutiny.test.select.context.propagation;

import java.util.Map;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;
import org.junit.Assert;

/**
 * @author <a href="mailto:kabir.khan@jboss.com">Kabir Khan</a>
 */
public class TestProvider implements ThreadContextProvider {
    private static boolean current;
    private static boolean cleared;
    private static int level;

    static void reset() {
        current = false;
        cleared = false;
        level = 0;
    }

    static void assertPropagated() {
        Assert.assertFalse(cleared);
        Assert.assertTrue(current);
        Assert.assertEquals(3, level);
    }

    static void assertNotInvoked() {
        Assert.assertFalse(cleared);
        Assert.assertFalse(current);
        Assert.assertEquals(0, level);
    }

    static void assertCleared() {
        Assert.assertTrue(cleared);
        Assert.assertFalse(current);
        Assert.assertEquals(3, level);
    }

    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        Assert.assertFalse(cleared);
        current = true;
        level = 1;
        return () -> {
            Assert.assertFalse(cleared);
            level = 2;
            return () -> {
                Assert.assertFalse(cleared);
                level = 3;
            };
        };
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        Assert.assertFalse(current);
        cleared = true;
        level = 1;
        return () -> {
            Assert.assertFalse(current);
            level = 2;
            return () -> {
                Assert.assertFalse(current);
                level = 3;
            };
        };
    }

    @Override
    public String getThreadContextType() {
        return "Test";
    }
}
