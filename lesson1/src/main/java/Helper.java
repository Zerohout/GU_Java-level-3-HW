import boxesTask.Apple;
import boxesTask.FruitBox;
import boxesTask.Orange;

public class Helper {
    private static final String SEPARATOR = "============================================";

    public static String[] createStringArray(int size){
        var out = new String[size];
        for(var i = 0; i < size; i++){
            var num = i + 1;
            out[i] = "s" + num;
        }
        return out;
    }

    public static Integer[] createIntegerArray(int size){
        var out = new Integer[size];
        for(var i = 0; i < size; i++){
            var num = i + 1;
            out[i] = num;
        }
        return out;
    }

    public static <T> void printArray(T[] array, boolean isPrintClassName){
        var out = new StringBuilder();

        if(isPrintClassName){
            out.append(array.getClass().getSimpleName());
            out.append(" ");
        }

        for (var i = 0; i < array.length; i++){
            if(i == 0) out.append("[");
            if(i == array.length - 1){
                out.append(array[i]);
                out.append("]");
                break;
            }
            out.append(array[i]);
            out.append(", ");
        }

        System.out.println(out);
    }

    public static void separateLines(){
        System.out.println("\n" + SEPARATOR);}

    public static void fillBoxWithApples(FruitBox<Apple> box, int fruitCount){
        System.out.printf("Заполняем коробку яблоками в количестве %d штук...\n",fruitCount);
        for(var i = 0; i < fruitCount; i++){
            box.putFruit(new Apple());
        }
        System.out.printf("Теперь вес коробки составляет: %dкг\n",box.getWeight());
    }

    public static void fillBoxWithOranges(FruitBox<Orange> box, int fruitCount){
        System.out.printf("Заполняем коробку апельсинами в количестве %d штук...\n",fruitCount);
        for(var i = 0; i < fruitCount; i++){
            box.putFruit(new Orange());
        }
        System.out.printf("Теперь вес коробки составляет: %dкг\n",box.getWeight());
    }
}
