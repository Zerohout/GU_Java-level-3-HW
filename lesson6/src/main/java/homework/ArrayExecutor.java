package homework;

import java.util.Arrays;

public class ArrayExecutor {
    public static void main(String[] args) {
        var arrayExecutor = new ArrayExecutor();
        System.out.println(Arrays.toString(arrayExecutor.executeQuadroArray(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9})));
        System.out.println(arrayExecutor.executeOneFourValuesArray(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    public int[] executeQuadroArray(int[] arr) {
        if (arr == null) throw new RuntimeException("Array is null");
        if (!checkQuadroArray(arr)) throw new RuntimeException("Array is not contains value \"4\".");
        if (arr.length < 5) throw new RuntimeException("Array size is not correct for this operation");
        var size = arr.length % 4;
        var out = new int[size];
        for (var i = 0; i < arr.length; i += 4) {
            if (i + 4 < arr.length) continue;
            for (var j = 0; j < size; j++, i++) {
                out[j] = arr[i];
            }
            break;
        }
        return out;
    }

    private boolean checkQuadroArray(int[] arr) {
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] == 4) return true;
        }
        return false;
    }

    public boolean executeOneFourValuesArray(int[] arr) {
        if (arr == null || arr.length < 2) return false;
        var flagOfOne = false;
        var flagOfFour = false;
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] == 1) {
                if (flagOfFour) return true;
                else flagOfOne = true;
                continue;
            }
            if (arr[i] == 4) {
                if (flagOfOne) return true;
                else flagOfFour = true;
            }
        }
        return false;
    }


}
