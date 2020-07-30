import boxesTask.Apple;
import boxesTask.FruitBox;
import boxesTask.Orange;

public class Lesson1 {
    public void start() {
        demoArraySwitcher();
        demoArrayConverter();
        demoBoxes();
    }

    private void demoArraySwitcher() {
        var arrSwitcher = new ArrayElementsSwitcher();
        arrSwitcher.start();
        Helper.separateLines();
    }

    private void demoArrayConverter() {
        var arrConverter = new ArrayToArrayListConverter();
        arrConverter.start();
        Helper.separateLines();
    }

    private void demoBoxes() {
        System.out.println("Достаём коробку для яблок");
        var appleBox1 = new FruitBox<Apple>();
        Helper.fillBoxWithApples(appleBox1, 10);

        System.out.println("\nДостаём коробку для апельсинов");
        var orangeBox = new FruitBox<Orange>();
        Helper.fillBoxWithOranges(orangeBox, 10);

        System.out.println("\nДостаём 2-ю коробку для яблок");
        var appleBox2 = new FruitBox<Apple>();
        Helper.fillBoxWithApples(appleBox2, 10);


        System.out.println("\nПересыпаем яблоки из 1-й во 2-ю коробку");
        appleBox1.moveFruitsTo(appleBox2);

        System.out.printf("Сравниваем вес 1-й коробки из под яблок(%dкг) и коробки из под апельсинов(%dкг). Эти коробки равны - %b\n",
                appleBox1.getWeight(), orangeBox.getWeight(), appleBox1.isEquals(orangeBox));
        System.out.printf("Сравниваем вес 2-й коробки из под яблок(%dкг) и коробки из под апельсинов(%dкг). Эти коробки равны - %b\n",
                appleBox2.getWeight(), orangeBox.getWeight(), appleBox2.isEquals(orangeBox));
    }
}
