package traffic.sim;

/**
 * Represents a traffic signal at one arm of the junction.
 * Runs on its own thread demonstrating sleep, interrupt, and thread priorities.
 * Syllabus Concepts:
 * - Unit 1: Class Signal, constructors, 'this' reference
 * - Unit 3: Thread life cycle (sleep, interrupt, priorities)
 */
public class Signal implements Runnable {

    public enum SignalColor {
        RED, YELLOW, GREEN
    }

    public interface SignalChangeListener {
        void onSignalChanged(Signal signal);
    }

    private final String direction;
    private volatile SignalColor currentColor;
    private final int greenDurationMs;
    private final int yellowDurationMs;
    private final int redDurationMs;
    private int threadPriority;
    private volatile boolean running;
    private Thread workerThread;
    private SignalChangeListener listener;

    // Default constructor using 'this' constructor chaining
    public Signal(String direction) {
        this(direction, SignalColor.RED, 4000, 1500, 4000, Thread.NORM_PRIORITY);
    }

    // Parameterized constructor using 'this' reference
    public Signal(String direction, SignalColor initialColor,
                  int greenDurationMs, int yellowDurationMs, int redDurationMs,
                  int threadPriority) {
        this.direction = direction;
        this.currentColor = initialColor;
        this.greenDurationMs = greenDurationMs;
        this.yellowDurationMs = yellowDurationMs;
        this.redDurationMs = redDurationMs;
        this.threadPriority = threadPriority;
        this.running = false;
    }

    public void setSignalChangeListener(SignalChangeListener listener) {
        this.listener = listener;
    }

    public synchronized void startSignal() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.workerThread = new Thread(this, "SignalThread-" + this.direction);
        this.workerThread.setPriority(this.threadPriority);
        this.workerThread.start();
    }

    public synchronized void stopSignal() {
        this.running = false;
        if (this.workerThread != null && this.workerThread.isAlive()) {
            this.workerThread.interrupt();
        }
    }

    /**
     * Interrupts the current sleep cycle to force an immediate state transition
     * (e.g., emergency override or manual click in Swing UI).
     */
    public void interruptAndToggle() {
        if (this.workerThread != null && this.workerThread.isAlive()) {
            this.workerThread.interrupt();
        } else {
            advanceColor();
        }
    }

    public void setThreadPriority(int priority) {
        this.threadPriority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, priority));
        if (this.workerThread != null) {
            this.workerThread.setPriority(this.threadPriority);
        }
    }

    public int getThreadPriority() {
        return this.threadPriority;
    }

    public String getDirection() {
        return this.direction;
    }

    public SignalColor getCurrentColor() {
        return this.currentColor;
    }

    public boolean isRunning() {
        return this.running;
    }

    private void advanceColor() {
        switch (this.currentColor) {
            case RED:
                this.currentColor = SignalColor.GREEN;
                break;
            case GREEN:
                this.currentColor = SignalColor.YELLOW;
                break;
            case YELLOW:
                this.currentColor = SignalColor.RED;
                break;
        }
        notifyListener();
    }

    private void notifyListener() {
        if (this.listener != null) {
            this.listener.onSignalChanged(this);
        }
    }

    @Override
    public void run() {
        notifyListener();
        while (this.running) {
            try {
                int duration;
                switch (this.currentColor) {
                    case GREEN:
                        duration = this.greenDurationMs;
                        break;
                    case YELLOW:
                        duration = this.yellowDurationMs;
                        break;
                    case RED:
                    default:
                        duration = this.redDurationMs;
                        break;
                }
                Thread.sleep(duration);
                if (this.running) {
                    advanceColor();
                }
            } catch (InterruptedException e) {
                if (!this.running) {
                    // Thread interrupted to stop simulation cleanly
                    break;
                }
                // Thread interrupted for manual/emergency override -> advance immediately
                advanceColor();
            }
        }
    }
}
