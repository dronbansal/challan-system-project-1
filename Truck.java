package traffic.sim;

/**
 * Truck subclass of Vehicle.
 * Syllabus Concept: Unit 2 - Inheritance (Vehicle -> Truck)
 */
public class Truck extends Vehicle {
    public Truck(String registrationNumber, String ownerName, String direction) {
        super(registrationNumber, ownerName, direction, "Truck");
    }

    @Override
    public double calculateFine() {
        return 2500.0;
    }
}
