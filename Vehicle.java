package traffic.sim;

/**
 * Base class representing a vehicle entering the traffic junction.
 * Syllabus Concepts:
 * - Unit 1: Class Vehicle, constructors, 'this' reference
 * - Unit 2: Inheritance base class, implements Violation interface
 */
public abstract class Vehicle implements Violation {
    private int vehicleId;
    private final String registrationNumber;
    private final String ownerName;
    private final String direction;
    private final String vehicleType;

    public Vehicle(String registrationNumber, String ownerName, String direction, String vehicleType) {
        this.registrationNumber = registrationNumber;
        this.ownerName = ownerName;
        this.direction = direction;
        this.vehicleType = vehicleType;
    }

    public int getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRegistrationNumber() {
        return this.registrationNumber;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public String getDirection() {
        return this.direction;
    }

    public String getVehicleType() {
        return this.vehicleType;
    }

    @Override
    public void checkViolation(Signal signal) throws SignalJumpException {
        if (signal.getCurrentColor() == Signal.SignalColor.RED) {
            throw new SignalJumpException(this.registrationNumber, this.direction, signal.getCurrentColor());
        }
    }

    @Override
    public String getViolationType() {
        return "Red Light Signal Jump";
    }

    @Override
    public String toString() {
        return this.vehicleType + " [" + this.registrationNumber + "] (" + this.direction + ")";
    }
}
