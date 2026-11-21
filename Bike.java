package traffic.sim;

/**
 * Bike subclass of Vehicle.
 * Syllabus Concept: Unit 2 - Inheritance (Vehicle -> Bike)
 */
public class Bike extends Vehicle {
    public Bike(String registrationNumber, String ownerName, String direction) {
        super(registrationNumber, ownerName, direction, "Bike");
    }

    @Override
    public double calculateFine() {
        return 500.0;
    }
}
