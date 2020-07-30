import java.util.ArrayList;
import java.util.Arrays;

public class ArrayToArrayListConverter {
    public void start(){
        var stringArray = Helper.createStringArray(10);
        System.out.println("Создан массив:");
        Helper.printArray(stringArray, true);

        var arrList = convertArrayToArrayList(stringArray);
        System.out.printf("%s %s\n",arrList.getClass().getSimpleName(), arrList);
    }

    private <T> ArrayList<T> convertArrayToArrayList(T[] array){
        System.out.println("Преобразовываем массив в ArrayList...");
        return new ArrayList<T>(Arrays.asList(array));
    }
}
