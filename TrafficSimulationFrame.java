package traffic.sim;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Main Swing GUI for Traffic Signal Simulation with Challan Records.
 *
 * Modules Implemented:
 * 1. Junction and signal setup
 * 2. Signal cycle simulation
 * 3. Vehicle entry
 * 4. Violation detection
 * 5. Challan records
 *
 * Syllabus Concept: Unit 5 - Swing GridLayout signal panel with colour JLabels;
 * FlowLayout; Mouse events (MouseAdapter).
 */
public class TrafficSimulationFrame extends JFrame implements Signal.SignalChangeListener {

    private static final String[] DIRECTIONS = {"NORTH", "EAST", "SOUTH", "WEST"};

    private final Map<String, Signal> signals = new LinkedHashMap<>();
    private final Map<String, JLabel> signalLabels = new LinkedHashMap<>();
    private final DatabaseManager dbManager;

    private final JTextField regField = new JTextField(10);
    private final JTextField ownerField = new JTextField(10);
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Car", "Bike", "Truck"});
    private final JComboBox<String> dirCombo = new JComboBox<>(DIRECTIONS);

    private final JTextArea logArea = new JTextArea(6, 40);
    private final DefaultTableModel challanModel;
    private final JTable challanTable;
    private final JLabel dbStatusLabel = new JLabel();

    private final Random random = new Random();

    public TrafficSimulationFrame() {
        super("Group 17 | Traffic Signal Simulation with Challan Records");
        this.dbManager = new DatabaseManager();

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);

        // Module 1: Junction and Signal Setup
        setupJunctionSignals();

        // Top Panel: FlowLayout controls for Module 2 (Simulation) & DB status
        JPanel topControlPanel = buildControlPanel();
        add(topControlPanel, BorderLayout.NORTH);

        // Center-Left: Unit 5 Swing GridLayout Signal Panel with colour JLabels + Mouse Events
        JPanel junctionPanel = buildSignalGridPanel();

        // Center-Right: Module 3 (Vehicle Entry) + Module 4 (Violation Log) + Module 5 (Challan Records)
        String[] columns = {"Challan ID", "Vehicle No", "Type", "Owner", "Direction", "Violation", "Fine (INR)", "Status", "Issued At"};
        this.challanModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.challanTable = new JTable(this.challanModel);
        JPanel rightPanel = buildRightPanel();

        JPanel centerContainer = new JPanel(new GridLayout(1, 2, 12, 12));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        centerContainer.add(junctionPanel);
        centerContainer.add(rightPanel);
        add(centerContainer, BorderLayout.CENTER);

        // Load any existing challans from DB
        refreshChallanTable();
        updateAllSignalLabels();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopAllSignals();
                dbManager.close();
            }
        });
    }

    /**
     * Module 1: Junction and signal setup with thread priorities.
     */
    private void setupJunctionSignals() {
        Signal north = new Signal("NORTH", Signal.SignalColor.GREEN, 4000, 1500, 4000, Thread.MAX_PRIORITY);
        Signal south = new Signal("SOUTH", Signal.SignalColor.GREEN, 4000, 1500, 4000, Thread.MAX_PRIORITY);
        Signal east = new Signal("EAST", Signal.SignalColor.RED, 4000, 1500, 4000, Thread.NORM_PRIORITY);
        Signal west = new Signal("WEST", Signal.SignalColor.RED, 4000, 1500, 4000, Thread.NORM_PRIORITY);

        this.signals.put("NORTH", north);
        this.signals.put("SOUTH", south);
        this.signals.put("EAST", east);
        this.signals.put("WEST", west);

        for (Signal signal : this.signals.values()) {
            signal.setSignalChangeListener(this);
        }
    }

    /**
     * Builds the top FlowLayout toolbar for starting/stopping signal threads and paying challans.
     */
    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Module 2: Signal Cycle Simulation & Controls (FlowLayout)"));

        JButton startBtn = new JButton("Start Signals");
        JButton stopBtn = new JButton("Stop Signals");
        JButton randomVehicleBtn = new JButton("Simulate Random Vehicle");
        JButton payChallanBtn = new JButton("Mark Selected Challan PAID");

        startBtn.addActionListener(e -> startAllSignals());
        stopBtn.addActionListener(e -> stopAllSignals());
        randomVehicleBtn.addActionListener(e -> simulateRandomVehicle());
        payChallanBtn.addActionListener(e -> markSelectedChallanPaid());

        this.dbStatusLabel.setText(this.dbManager.isJdbcActive()
                ? "DB: JDBC Connected"
                : "DB: In-Memory Mode (Attach JDBC Driver for SQLite/MySQL)");
        this.dbStatusLabel.setForeground(this.dbManager.isJdbcActive() ? new Color(0, 128, 0) : new Color(180, 90, 0));

        panel.add(startBtn);
        panel.add(stopBtn);
        panel.add(randomVehicleBtn);
        panel.add(payChallanBtn);
        panel.add(this.dbStatusLabel);

        return panel;
    }

    /**
     * Unit 5: Swing GridLayout signal panel with colour JLabels and MouseListener events.
     * Clicking any signal JLabel interrupts that signal thread to toggle its state.
     */
    private JPanel buildSignalGridPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 12, 12));
        panel.setBorder(BorderFactory.createTitledBorder("Junction Signals (GridLayout) - Click a Signal to Interrupt/Toggle"));

        for (String dir : DIRECTIONS) {
            JLabel label = new JLabel(dir + ": RED", SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("SansSerif", Font.BOLD, 18));
            label.setForeground(Color.WHITE);
            label.setPreferredSize(new Dimension(180, 140));
            label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

            // Unit 5: Mouse events (click to interrupt/toggle signal, hover to highlight border)
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Signal sig = signals.get(dir);
                    if (sig != null) {
                        sig.interruptAndToggle();
                        appendLog("[MOUSE EVENT] Manually interrupted " + dir + " signal -> " + sig.getCurrentColor());
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    label.setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                }
            });

            this.signalLabels.put(dir, label);
            panel.add(label);
        }

        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));

        // Module 3: Vehicle Entry Panel using FlowLayout
        JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        entryPanel.setBorder(BorderFactory.createTitledBorder("Module 3: Vehicle Entry (FlowLayout)"));
        this.regField.setText("DL-01-AB-1234");
        this.ownerField.setText("Prativa Nayak");
        JButton enterBtn = new JButton("Cross Junction");
        enterBtn.addActionListener(e -> handleVehicleEntry());

        entryPanel.add(new JLabel("Reg No:"));
        entryPanel.add(this.regField);
        entryPanel.add(new JLabel("Owner:"));
        entryPanel.add(this.ownerField);
        entryPanel.add(new JLabel("Type:"));
        entryPanel.add(this.typeCombo);
        entryPanel.add(new JLabel("Dir:"));
        entryPanel.add(this.dirCombo);
        entryPanel.add(enterBtn);

        // Module 4: Violation Detection Log
        this.logArea.setEditable(false);
        this.logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(this.logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Module 4: Violation Detection Log"));

        // Module 5: Challan Records Table
        JScrollPane tableScroll = new JScrollPane(this.challanTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Module 5: Challan Records (JDBC)"));

        JPanel logsAndTable = new JPanel(new GridLayout(2, 1, 8, 8));
        logsAndTable.add(logScroll);
        logsAndTable.add(tableScroll);

        rightPanel.add(entryPanel, BorderLayout.NORTH);
        rightPanel.add(logsAndTable, BorderLayout.CENTER);
        return rightPanel;
    }

    private void startAllSignals() {
        for (Signal signal : this.signals.values()) {
            signal.startSignal();
        }
        appendLog("[SIMULATION] All 4 junction signal threads started.");
    }

    private void stopAllSignals() {
        for (Signal signal : this.signals.values()) {
            signal.stopSignal();
        }
        appendLog("[SIMULATION] All junction signal threads stopped.");
    }

    /**
     * Module 3 & 4: Create Vehicle subclass, store in DB, and check for SignalJumpException.
     */
    private void handleVehicleEntry() {
        String reg = this.regField.getText().trim();
        String owner = this.ownerField.getText().trim();
        String type = (String) this.typeCombo.getSelectedItem();
        String dir = (String) this.dirCombo.getSelectedItem();

        if (reg.isEmpty() || owner.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Registration Number and Owner Name.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Vehicle vehicle = createVehicle(type, reg, owner, dir);
        processVehicleCrossing(vehicle);
    }

    private void simulateRandomVehicle() {
        String[] types = {"Car", "Bike", "Truck"};
        String[] owners = {"Dron Bansal", "Satyam Kumar", "Sahib Singh", "Prativa Nayak"};
        String type = types[this.random.nextInt(types.length)];
        String dir = DIRECTIONS[this.random.nextInt(DIRECTIONS.length)];
        String owner = owners[this.random.nextInt(owners.length)];
        String reg = String.format("DL-%02d-XY-%04d", 1 + this.random.nextInt(12), 1000 + this.random.nextInt(9000));

        Vehicle vehicle = createVehicle(type, reg, owner, dir);
        processVehicleCrossing(vehicle);
    }

    private Vehicle createVehicle(String type, String reg, String owner, String dir) {
        switch (type) {
            case "Bike":
                return new Bike(reg, owner, dir);
            case "Truck":
                return new Truck(reg, owner, dir);
            case "Car":
            default:
                return new Car(reg, owner, dir);
        }
    }

    private void processVehicleCrossing(Vehicle vehicle) {
        this.dbManager.recordVehicleEntry(vehicle);
        Signal activeSignal = this.signals.get(vehicle.getDirection());

        try {
            vehicle.checkViolation(activeSignal);
            appendLog("[CLEAR] " + vehicle + " crossed safely on " + activeSignal.getCurrentColor() + " signal.");
        } catch (SignalJumpException ex) {
            appendLog("[VIOLATION] " + ex.getMessage());
            int violationId = this.dbManager.recordViolation(vehicle, activeSignal.getCurrentColor());

            Challan challan = new Challan(
                    vehicle.getRegistrationNumber(),
                    vehicle.getVehicleType(),
                    vehicle.getOwnerName(),
                    vehicle.getDirection(),
                    vehicle.getViolationType(),
                    vehicle.calculateFine()
            );
            challan.setViolationId(violationId);
            this.dbManager.recordChallan(challan);
            refreshChallanTable();
        }
    }

    private void markSelectedChallanPaid() {
        int selectedRow = this.challanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a challan row first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int challanId = (int) this.challanModel.getValueAt(selectedRow, 0);
        if (this.dbManager.markChallanPaid(challanId)) {
            appendLog("[CHALLAN] Challan #" + challanId + " marked as PAID.");
            refreshChallanTable();
        }
    }

    private void refreshChallanTable() {
        this.challanModel.setRowCount(0);
        List<Challan> challans = this.dbManager.getAllChallans();
        for (Challan c : challans) {
            this.challanModel.addRow(new Object[]{
                    c.getChallanId(),
                    c.getVehicleNumber(),
                    c.getVehicleType(),
                    c.getOwnerName(),
                    c.getDirection(),
                    c.getViolationType(),
                    c.getFineAmount(),
                    c.getStatus(),
                    c.getIssuedAt()
            });
        }
    }

    @Override
    public void onSignalChanged(Signal signal) {
        SwingUtilities.invokeLater(() -> updateSignalLabel(signal));
    }

    private void updateAllSignalLabels() {
        for (Signal signal : this.signals.values()) {
            updateSignalLabel(signal);
        }
    }

    private void updateSignalLabel(Signal signal) {
        JLabel label = this.signalLabels.get(signal.getDirection());
        if (label == null) {
            return;
        }
        Signal.SignalColor color = signal.getCurrentColor();
        label.setText("<html><center>" + signal.getDirection() + "<br/>" + color
                + "<br/><small>Priority: " + signal.getThreadPriority() + "</small></center></html>");
        switch (color) {
            case RED:
                label.setBackground(new Color(200, 35, 35));
                label.setForeground(Color.WHITE);
                break;
            case YELLOW:
                label.setBackground(new Color(235, 180, 20));
                label.setForeground(Color.BLACK);
                break;
            case GREEN:
                label.setBackground(new Color(34, 150, 60));
                label.setForeground(Color.WHITE);
                break;
        }
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            this.logArea.append(message + "\n");
            this.logArea.setCaretPosition(this.logArea.getDocument().getLength());
        });
    }
}
