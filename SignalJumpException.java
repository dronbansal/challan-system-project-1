package traffic.sim;

/**
 * Custom checked exception thrown when a vehicle crosses a junction
 * while its direction's traffic signal is RED.
 * Syllabus Concept: Unit 3 - Custom Exception (SignalJumpException)
 */
public class SignalJumpException extends Exception {
    private final String vehicleNumber;
    private final String direction;
    private final Signal.SignalColor signalState;

    public SignalJumpException(String vehicleNumber, String direction, Signal.SignalColor signalState) {
        super("Signal Jump Violation! Vehicle [" + vehicleNumber + "] crossed "
                + direction + " junction during " + signalState + " signal.");
        this.vehicleNumber = vehicleNumber;
        this.direction = direction;
        this.signalState = signalState;
    }

    public String getVehicleNumber() {
        return this.vehicleNumber;
    }

    public String getDirection() {
        return this.direction;
    }

    public Signal.SignalColor getSignalState() {
        return this.signalState;
    }
}
