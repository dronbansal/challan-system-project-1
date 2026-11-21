package traffic.sim;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a traffic challan issued for a signal jump violation.
 * Syllabus Concept: Unit 1 - Class Challan, constructors, 'this' reference
 */
public class Challan {
    private int challanId;
    private int violationId;
    private final String vehicleNumber;
    private final String vehicleType;
    private final String ownerName;
    private final String direction;
    private final String violationType;
    private final double fineAmount;
    private String status;
    private final String issuedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor chaining using 'this'
    public Challan(String vehicleNumber, String vehicleType, String ownerName,
                   String direction, String violationType, double fineAmount) {
        this(0, 0, vehicleNumber, vehicleType, ownerName, direction,
             violationType, fineAmount, "UNPAID", LocalDateTime.now().format(FORMATTER));
    }

    // Full parameterized constructor using 'this' reference
    public Challan(int challanId, int violationId, String vehicleNumber, String vehicleType,
                   String ownerName, String direction, String violationType,
                   double fineAmount, String status, String issuedAt) {
        this.challanId = challanId;
        this.violationId = violationId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
        this.direction = direction;
        this.violationType = violationType;
        this.fineAmount = fineAmount;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    public int getChallanId() {
        return this.challanId;
    }

    public void setChallanId(int challanId) {
        this.challanId = challanId;
    }

    public int getViolationId() {
        return this.violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public String getVehicleNumber() {
        return this.vehicleNumber;
    }

    public String getVehicleType() {
        return this.vehicleType;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public String getDirection() {
        return this.direction;
    }

    public String getViolationType() {
        return this.violationType;
    }

    public double getFineAmount() {
        return this.fineAmount;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuedAt() {
        return this.issuedAt;
    }
}
