package homework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Race {
    private ArrayList<Stage> stages;
    private AtomicBoolean isWinnerExists;

    public ArrayList<Stage> getStages() {
        return stages;
    }

    public AtomicBoolean isWinnerExists() {
        return this.isWinnerExists;
    }

    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
        isWinnerExists = new AtomicBoolean(false);
    }
}