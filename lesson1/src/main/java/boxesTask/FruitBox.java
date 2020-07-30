package boxesTask;

import java.util.ArrayList;

public class FruitBox<T extends Fruit> {
    ArrayList<T> box;

    public FruitBox() {
        box = new ArrayList<>();
    }

    public int getWeight() {
        var out = 0;
        for(var fruit : box){
            out += fruit.weight;
        }
        return out;
    }

    public boolean isEquals(FruitBox<?> otherBox){
        return this.getWeight() == otherBox.getWeight();
    }

    public void moveFruitsTo(FruitBox<T> otherBox){
        if(otherBox == null) return;
        if(otherBox.box.isEmpty()) return;
        if(this == otherBox) return;

        otherBox.box.addAll(this.box);

        box.clear();
        System.out.println("Все фрукты из коробки успешно пересыпаны.");
    }

    public void putFruit(T fruit){box.add(fruit);}
}
