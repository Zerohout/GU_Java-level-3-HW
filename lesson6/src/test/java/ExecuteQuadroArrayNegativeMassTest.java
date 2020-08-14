import homework.ArrayExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ExecuteQuadroArrayNegativeMassTest {

    @Parameterized.Parameters
    public static Collection<Object[]> checkArrayData() {
        return Arrays.asList(new Object[][]{
                {new int[]{1, 2, 3}},
                {new int[]{1, 2, 3, 4}},
                {new int[]{1, 2, 3, 5, 6}},
                {null},
                {new int[0]}
        });
    }

    private int[] testValues;

    public ExecuteQuadroArrayNegativeMassTest(int[] testValues) {
        this.testValues = testValues;
    }

    @Test(expected = RuntimeException.class)
    public void executeArrayNegativeTest() {
        var arrayExecutor = new ArrayExecutor();
        arrayExecutor.executeQuadroArray(testValues);
    }
}
