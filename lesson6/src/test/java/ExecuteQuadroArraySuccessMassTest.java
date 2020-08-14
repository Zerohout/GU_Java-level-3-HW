import homework.ArrayExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ExecuteQuadroArraySuccessMassTest {

    @Parameterized.Parameters
    public static Collection<Object[]> checkArrayData() {
        return Arrays.asList(new Object[][]{
                {new int[]{5}, new int[]{1, 2, 3, 4, 5}},
                {new int[]{13, 14, 15}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}},
                {new int[]{3, 2}, new int[]{15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2}}
        });
    }

    private int[] expected;
    private int[] array;

    public ExecuteQuadroArraySuccessMassTest(int[] expected, int[] array) {
        this.expected = expected;
        this.array = array;
    }

    @Test
    public void executeArraySuccessTest(){
        var arrayExecutor = new ArrayExecutor();
        Assert.assertArrayEquals(expected,arrayExecutor.executeQuadroArray(array));
    }
}
