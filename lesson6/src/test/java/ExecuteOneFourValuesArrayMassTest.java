import homework.ArrayExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ExecuteOneFourValuesArrayMassTest {
    @Parameterized.Parameters
    public static Collection<Object[]> checkArrayData() {
        return Arrays.asList(new Object[][]{
                {false, new int[]{1, 2, 3}},
                {true, new int[]{1, 2, 3, 4}},
                {false, new int[]{1, 2, 3, 5, 6}},
                {false, null},
                {false, new int[0]},
                {true, new int[]{4, 3, 5, 6, 7, 1}}
        });
    }

    private boolean expected;
    private int[] testValues;

    public ExecuteOneFourValuesArrayMassTest(boolean expected, int[] testValues) {
        this.expected = expected;
        this.testValues = testValues;
    }

    @Test
    public void executeOneFourValuesArrayTest() {
        var arrayExecutor = new ArrayExecutor();
        Assert.assertEquals(expected,arrayExecutor.executeOneFourValuesArray(testValues));
    }
}
