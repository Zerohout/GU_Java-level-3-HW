import Other.ControlPanel;

public class Lesson2 {
    public static void main(String[] args) {
        new Thread(ControlPanel::new).start();
    }
}
