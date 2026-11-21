package traffic.sim;

/**
 * Interface defining traffic violation detection and penalty calculation.
 * Syllabus Concept: Unit 2 - Interface Violation
 */
public interface Violation {
    /**
     * Checks whether the vehicle committed a signal jump violation.
     *
     * @param signal The active Signal for the vehicle's direction.
     * @throws SignalJumpException if the signal is RED when crossing.
     */
    void checkViolation(Signal signal) throws SignalJumpException;

    /**
     * Returns the violation type label.
     */
    String getViolationType();

    /**
     * Returns the base challan fine amount in INR for this vehicle type.
     */
    double calculateFine();
}
