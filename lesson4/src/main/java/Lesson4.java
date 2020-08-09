public class Lesson4 {
    private final Object monitor = new Object();

    public static void main(String[] args) {
        var lesson4 = new Lesson4();
        lesson4.start();
    }

    private static State currentState = State.WAIT_A;

    enum State {
        WAIT_A,
        WAIT_B,
        WAIT_C
    }

    public void start() {
        createThread("A").start();
        createThread("B").start();
        createThread("C").start();
    }

    private Thread createThread(String letter) {
        State state;
        State nextState;
        switch (letter) {
            case "A" -> {
                state = State.WAIT_A;
                nextState = State.WAIT_B;
            }
            case "B" -> {
                state = State.WAIT_B;
                nextState = State.WAIT_C;
            }
            case "C" -> {
                state = State.WAIT_C;
                nextState = State.WAIT_A;
            }
            default -> throw new RuntimeException("switch error. Unknown entered variable");
        }

        return new Thread(() -> printLetter(letter,state,nextState,Thread.currentThread()));
    }

    private void printLetter(String letter, State state, State nextState, Thread thread) {
        synchronized (monitor) {
            for (var i = 0; i < 5; ) {
                if (currentState == state) {
                    System.out.print(letter);
                    i++;
                    currentState = nextState;
                    monitor.notifyAll();
                } else {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
