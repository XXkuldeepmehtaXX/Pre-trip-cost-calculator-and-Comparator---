import java.sql.*;
import java.util.Scanner;

public class TripApp {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Connection con = DBConnection.getConnection();
        Statement st = con.createStatement();

        // Route: distance comes from the API
        System.out.print("From: ");
        String from = sc.nextLine();
        System.out.print("To: ");
        String to = sc.nextLine();

        double distance;
        try {
            distance = DistanceAPI.getDistanceKm(from, to);
        } catch (Exception e) {
            System.out.println("Could not get distance: " + e.getMessage());
            con.close();
            return;
        }
        System.out.println("Distance (from API): " + Math.round(distance) + " km");

        System.out.print("Toll for this trip (Rs): ");
        double toll = sc.nextDouble();

        // Vehicles show here
        ResultSet v = st.executeQuery("select * from vehicles");
        System.out.println("--- Vehicles ---");
        while (v.next()) {
            System.out.println(v.getInt("id") + ". " + v.getString("name")
                + " (" + v.getString("type") + ")");
        }
        System.out.print("Choose vehicle id: ");
        int vehicleID = sc.nextInt();

        // Chosen vehicle
        ResultSet vv = st.executeQuery("select * from vehicles where id = " + vehicleID);
        vv.next();
        double mileage = vv.getDouble("mileage");
        double range = vv.getDouble("tank_range");
        double price = vv.getDouble("price_per_unit");

        // Calculations
        double energyCost = (distance / mileage) * price;
        int stops = (int) Math.ceil(distance / range) - 1;
        double total = energyCost + toll;

        System.out.println("\n--- Trip Summary ---");
        System.out.println("Distance     : " + Math.round(distance) + " km");
        System.out.println("Energy cost  : Rs " + Math.round(energyCost));
        System.out.println("Toll         : Rs " + toll);
        System.out.println("Refuel stops : " + stops);
        System.out.println("Total        : Rs " + Math.round(total));

        con.close();
    }
}