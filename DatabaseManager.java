package traffic.sim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles JDBC database persistence for vehicles, violations, and challans.
 * Syllabus Concept: Unit 4 - JDBC Statement and PreparedStatement for challan storage.
 *
 * Supports SQLite (default: jdbc:sqlite:traffic_sim.db) or MySQL (via system properties).
 * Falls back gracefully to an in-memory store if no JDBC driver JAR is on the classpath
 * so the Swing simulation can always be demonstrated.
 */
public class DatabaseManager {
    private static final String DEFAULT_URL = System.getProperty("db.url", "jdbc:sqlite:traffic_sim.db");
    private static final String DEFAULT_USER = System.getProperty("db.user", "");
    private static final String DEFAULT_PASS = System.getProperty("db.pass", "");

    private Connection connection;
    private boolean jdbcActive;

    // In-memory fallback stores when JDBC driver JAR is not attached
    private final List<Challan> memoryChallans = new ArrayList<>();
    private int memoryVehicleSeq = 1;
    private int memoryViolationSeq = 1;
    private int memoryChallanSeq = 1;

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DatabaseManager() {
        initConnection();
    }

    private void initConnection() {
        try {
            if (DEFAULT_USER.isEmpty()) {
                this.connection = DriverManager.getConnection(DEFAULT_URL);
            } else {
                this.connection = DriverManager.getConnection(DEFAULT_URL, DEFAULT_USER, DEFAULT_PASS);
            }
            this.jdbcActive = true;
            initializeTables();
        } catch (SQLException e) {
            this.jdbcActive = false;
            System.err.println("[DatabaseManager] JDBC driver not found or DB unreachable ("
                    + e.getMessage() + "). Running with in-memory fallback store.");
        }
    }

    public boolean isJdbcActive() {
        return this.jdbcActive;
    }

    /**
     * Creates the vehicles, violations, and challans tables using JDBC Statement.
     */
    private void initializeTables() throws SQLException {
        String createVehicles = "CREATE TABLE IF NOT EXISTS vehicles ("
                + "vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "registration_number VARCHAR(20) NOT NULL, "
                + "vehicle_type VARCHAR(20) NOT NULL, "
                + "owner_name VARCHAR(100) NOT NULL, "
                + "direction VARCHAR(10) NOT NULL, "
                + "entry_time VARCHAR(30) NOT NULL"
                + ")";

        String createViolations = "CREATE TABLE IF NOT EXISTS violations ("
                + "violation_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "vehicle_id INTEGER NOT NULL, "
                + "direction VARCHAR(10) NOT NULL, "
                + "signal_state VARCHAR(10) NOT NULL, "
                + "violation_type VARCHAR(50) NOT NULL, "
                + "violation_time VARCHAR(30) NOT NULL, "
                + "FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)"
                + ")";

        String createChallans = "CREATE TABLE IF NOT EXISTS challans ("
                + "challan_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "violation_id INTEGER NOT NULL, "
                + "vehicle_number VARCHAR(20) NOT NULL, "
                + "vehicle_type VARCHAR(20) NOT NULL, "
                + "owner_name VARCHAR(100) NOT NULL, "
                + "direction VARCHAR(10) NOT NULL, "
                + "violation_type VARCHAR(50) NOT NULL, "
                + "fine_amount DOUBLE NOT NULL, "
                + "status VARCHAR(20) NOT NULL, "
                + "issued_at VARCHAR(30) NOT NULL, "
                + "FOREIGN KEY (violation_id) REFERENCES violations(violation_id)"
                + ")";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(createVehicles);
            stmt.execute(createViolations);
            stmt.execute(createChallans);
        }
    }

    /**
     * Inserts a Vehicle record into the 'vehicles' table using PreparedStatement.
     */
    public int recordVehicleEntry(Vehicle vehicle) {
        String now = LocalDateTime.now().format(TS_FMT);
        if (!this.jdbcActive) {
            int id = this.memoryVehicleSeq++;
            vehicle.setVehicleId(id);
            return id;
        }

        String sql = "INSERT INTO vehicles (registration_number, vehicle_type, owner_name, direction, entry_time) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vehicle.getRegistrationNumber());
            ps.setString(2, vehicle.getVehicleType());
            ps.setString(3, vehicle.getOwnerName());
            ps.setString(4, vehicle.getDirection());
            ps.setString(5, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    vehicle.setVehicleId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error inserting vehicle: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Inserts a Violation record into the 'violations' table using PreparedStatement.
     */
    public int recordViolation(Vehicle vehicle, Signal.SignalColor signalState) {
        String now = LocalDateTime.now().format(TS_FMT);
        if (!this.jdbcActive) {
            return this.memoryViolationSeq++;
        }

        String sql = "INSERT INTO violations (vehicle_id, direction, signal_state, violation_type, violation_time) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, vehicle.getVehicleId());
            ps.setString(2, vehicle.getDirection());
            ps.setString(3, signalState.name());
            ps.setString(4, vehicle.getViolationType());
            ps.setString(5, now);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error inserting violation: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Inserts a Challan record into the 'challans' table using PreparedStatement.
     */
    public Challan recordChallan(Challan challan) {
        if (!this.jdbcActive) {
            challan.setChallanId(this.memoryChallanSeq++);
            this.memoryChallans.add(challan);
            return challan;
        }

        String sql = "INSERT INTO challans (violation_id, vehicle_number, vehicle_type, owner_name, "
                + "direction, violation_type, fine_amount, status, issued_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, challan.getViolationId());
            ps.setString(2, challan.getVehicleNumber());
            ps.setString(3, challan.getVehicleType());
            ps.setString(4, challan.getOwnerName());
            ps.setString(5, challan.getDirection());
            ps.setString(6, challan.getViolationType());
            ps.setDouble(7, challan.getFineAmount());
            ps.setString(8, challan.getStatus());
            ps.setString(9, challan.getIssuedAt());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    challan.setChallanId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error inserting challan: " + e.getMessage());
        }
        return challan;
    }

    /**
     * Updates a Challan status to PAID using PreparedStatement.
     */
    public boolean markChallanPaid(int challanId) {
        if (!this.jdbcActive) {
            for (Challan c : this.memoryChallans) {
                if (c.getChallanId() == challanId) {
                    c.setStatus("PAID");
                    return true;
                }
            }
            return false;
        }

        String sql = "UPDATE challans SET status = 'PAID' WHERE challan_id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setInt(1, challanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error updating challan: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all Challan records using JDBC Statement.
     */
    public List<Challan> getAllChallans() {
        if (!this.jdbcActive) {
            return new ArrayList<>(this.memoryChallans);
        }

        List<Challan> list = new ArrayList<>();
        String sql = "SELECT challan_id, violation_id, vehicle_number, vehicle_type, owner_name, "
                + "direction, violation_type, fine_amount, status, issued_at FROM challans ORDER BY challan_id DESC";
        try (Statement stmt = this.connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Challan(
                        rs.getInt("challan_id"),
                        rs.getInt("violation_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getString("owner_name"),
                        rs.getString("direction"),
                        rs.getString("violation_type"),
                        rs.getDouble("fine_amount"),
                        rs.getString("status"),
                        rs.getString("issued_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error querying challans: " + e.getMessage());
        }
        return list;
    }

    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
