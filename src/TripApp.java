import java.sql.*;
import java.util.Scanner;

public class TripApp {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Connection con = DBConnection.getConnection();
        Statement st = con.createStatement();

        // Routes show here
        ResultSet r = st.executeQuery("select * from routes");
        System.out.println("--- Routes ---");
        while (r.next()) {
            System.out.println(r.getInt("id") + ". " + r.getString("source")
                + " -> " + r.getString("destination")
                + " (" + r.getDouble("distance_km") + " km)");
        }
        System.out.print("Choose route id: ");
        int routeID = sc.nextInt();

        // Vehicles show here
        ResultSet v = st.executeQuery("select * from vehicles");
        System.out.println("--- Vehicles ---");
        while (v.next()) {
            System.out.println(v.getInt("id") + ". " + v.getString("name")
                + " (" + v.getString("type") + ")");
        }
        System.out.print("Choose vehicle id: ");
        int vehicleID = sc.nextInt();

        System.out.print("Toll payment (1 = FASTag, 2 = Cash): ");
        int pay = sc.nextInt();

        // Chosen route
        ResultSet rr = st.executeQuery("select * from routes where id = " + routeID);
        rr.next();
        double distance = rr.getDouble("distance_km");
        double toll = (pay == 1) ? rr.getDouble("toll_fastag") : rr.getDouble("toll_cash");

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
        System.out.println("Energy cost  : Rs " + energyCost);
        System.out.println("Toll         : Rs " + toll);
        System.out.println("Refuel stops : " + stops);
        System.out.println("Total        : Rs " + total);

        con.close();
    }
}
