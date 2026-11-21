package traffic.sim;

import javax.swing.SwingUtilities;

/**
 * Application entry point for Group 17 Mini Project:
 * Traffic Signal Simulation with Challan Records.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrafficSimulationFrame frame = new TrafficSimulationFrame();
            frame.setVisible(true);
        });
    }
}
