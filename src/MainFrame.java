import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainFrame extends JFrame {

    // small holder so the combo box can show a name but keep the numbers
    static class VehicleItem {
        String name, type;
        double mileage, range, price;
        public String toString() { return name + " (" + type + ")"; }
    }

    JTextField fromField = new JTextField(15);
    JTextField toField = new JTextField(15);
    JTextField tollField = new JTextField("0", 15);
    JComboBox<VehicleItem> vehicleBox = new JComboBox<>();
    JButton calcBtn = new JButton("Calculate");
    JTextArea result = new JTextArea(7, 30);

    public MainFrame() {
        setTitle("EcoRoute - Pre-Trip Cost Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // input form
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        form.add(new JLabel("From:"));        form.add(fromField);
        form.add(new JLabel("To:"));          form.add(toField);
        form.add(new JLabel("Toll (Rs):"));   form.add(tollField);
        form.add(new JLabel("Vehicle:"));     form.add(vehicleBox);
        add(form, BorderLayout.NORTH);

        // button
        JPanel mid = new JPanel();
        mid.add(calcBtn);
        add(mid, BorderLayout.CENTER);

        // result area
        result.setEditable(false);
        result.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(result);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(sp, BorderLayout.SOUTH);

        loadVehicles();
        calcBtn.addActionListener(e -> calculate());

        pack();
        setLocationRelativeTo(null);   // centre on screen
    }

    // fills the vehicle drop-down from MySQL
    private void loadVehicles() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("select * from vehicles")) {
            while (rs.next()) {
                VehicleItem v = new VehicleItem();
                v.name = rs.getString("name");
                v.type = rs.getString("type");
                v.mileage = rs.getDouble("mileage");
                v.range = rs.getDouble("tank_range");
                v.price = rs.getDouble("price_per_unit");
                vehicleBox.addItem(v);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not load vehicles. Is MySQL running?\n" + e.getMessage());
        }
    }

    private void calculate() {
        String from = fromField.getText().trim();
        String to = toField.getText().trim();
        VehicleItem v = (VehicleItem) vehicleBox.getSelectedItem();

        if (from.isEmpty() || to.isEmpty() || v == null) {
            result.setText("Please enter From, To and choose a vehicle.");
            return;
        }
        final double toll;
        try {
            toll = Double.parseDouble(tollField.getText().trim());
        } catch (NumberFormatException ex) {
            result.setText("Toll must be a number.");
            return;
        }

        calcBtn.setEnabled(false);
        result.setText("Getting distance from API...");

        // network call runs in the background so the window does not freeze
        new SwingWorker<Double, Void>() {
            protected Double doInBackground() throws Exception {
                return DistanceAPI.getDistanceKm(from, to);
            }
            protected void done() {
                try {
                    double distance = get();
                    double energy = (distance / v.mileage) * v.price;
                    int stops = Math.max(0, (int) Math.ceil(distance / v.range) - 1);
                    double total = energy + toll;

                    result.setText(String.format(
                        "Distance     : %.0f km%n"
                      + "Energy cost  : Rs %.0f%n"
                      + "Toll         : Rs %.0f%n"
                      + "Refuel stops : %d%n"
                      + "Total        : Rs %.0f",
                        distance, energy, toll, stops, total));
                } catch (Exception ex) {
                    Throwable c = (ex.getCause() != null) ? ex.getCause() : ex;
                    result.setText("Could not get distance: " + c.getMessage());
                }
                calcBtn.setEnabled(true);
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}