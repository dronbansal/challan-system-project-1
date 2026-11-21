package traffic.sim;

/**
 * Car subclass of Vehicle.
 * Syllabus Concept: Unit 2 - Inheritance (Vehicle -> Car)
 */
public class Car extends Vehicle {
    public Car(String registrationNumber, String ownerName, String direction) {
        super(registrationNumber, ownerName, direction, "Car");
    }

    @Override
    public double calculateFine() {
        return 1000.0;
    }
}
