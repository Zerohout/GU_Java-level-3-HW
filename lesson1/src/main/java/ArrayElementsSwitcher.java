public class ArrayElementsSwitcher {
    public void start() {
        var firstElemIndex = 3;
        var secondElemIndex = 7;
        var arraySize = 10;

        var stringArr = Helper.createStringArray(arraySize);
        var integerArr = Helper.createIntegerArray(arraySize);

        switchArrayElements(stringArr,firstElemIndex,secondElemIndex);
        switchArrayElements(integerArr,firstElemIndex,secondElemIndex);
    }

    private <T> void switchArrayElements(T[] array, int firstElemIndex, int secondElemIndex) {
        if (array == null || isIndexOfElementIncorrect(array, firstElemIndex)
                || isIndexOfElementIncorrect(array, secondElemIndex)) {
            throw new RuntimeException("Array or index of element is incorrect.");
        }

        System.out.println("\nМассив до операции:");
        Helper.printArray(array,true);

        System.out.printf("Меняем местами элементы под индексами %d и %d...\n",firstElemIndex, secondElemIndex);

        var tempValue = array[firstElemIndex];
        array[firstElemIndex] = array[secondElemIndex];
        array[secondElemIndex] = tempValue;

        System.out.println("Массив после операции:");
        Helper.printArray(array,true);
    }

    private <T> boolean isIndexOfElementIncorrect(T[] array, int elemIndex) {return elemIndex < 0 || elemIndex >= array.length;}
}
