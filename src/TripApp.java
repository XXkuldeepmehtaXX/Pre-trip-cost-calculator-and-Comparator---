import java.sql.*;
import java.util.Scanner;

public class TripApp {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Connection con = DBConnection.getConnection();
        Statement st = con.createStatement();

        double distance;
        double toll;

        System.out.println("1. Saved route (verified toll from database)");
        System.out.println("2. Any route (distance from API)");
        System.out.print("Choose: ");
        int mode = sc.nextInt();

        if (mode == 1) {
            // ---- route from the database ----
            ResultSet r = st.executeQuery("select * from routes");
            System.out.println("--- Routes ---");
            while (r.next()) {
                System.out.println(r.getInt("id") + ". " + r.getString("source")
                    + " -> " + r.getString("destination")
                    + " (" + r.getDouble("distance_km") + " km)");
            }
            System.out.print("Choose route id: ");
            int routeID = sc.nextInt();

            System.out.print("Toll payment (1 = FASTag, 2 = Cash): ");
            int pay = sc.nextInt();

            ResultSet rr = st.executeQuery("select * from routes where id = " + routeID);
            rr.next();
            distance = rr.getDouble("distance_km");
            toll = (pay == 1) ? rr.getDouble("toll_fastag") : rr.getDouble("toll_cash");

        } else {
            // ---- distance from the API ----
            sc.nextLine();   // clears the leftover Enter
            System.out.print("From: ");
            String from = sc.nextLine();
            System.out.print("To: ");
            String to = sc.nextLine();

            try {
                distance = DistanceAPI.getDistanceKm(from, to);
            } catch (Exception e) {
                System.out.println("Could not get distance: " + e.getMessage());
                con.close();
                return;
            }
            System.out.println("Distance (from API): " + Math.round(distance) + " km");

            System.out.print("Toll for this trip (Rs): ");
            toll = sc.nextDouble();
        }

        // ---- vehicles ----
        ResultSet v = st.executeQuery("select * from vehicles");
        System.out.println("--- Vehicles ---");
        while (v.next()) {
            System.out.println(v.getInt("id") + ". " + v.getString("name")
                + " (" + v.getString("type") + ")");
        }
        System.out.print("Choose vehicle id: ");
        int vehicleID = sc.nextInt();

        ResultSet vv = st.executeQuery("select * from vehicles where id = " + vehicleID);
        vv.next();
        String vName = vv.getString("name");
        String vType = vv.getString("type");
        double mileage = vv.getDouble("mileage");
        double range = vv.getDouble("tank_range");
        double price = vv.getDouble("price_per_unit");

        // ---- short result ----
        System.out.println();
        System.out.println("--- Trip Summary ---");
        System.out.println(CostCalculator.summary(distance, mileage, range, price, toll));

        // ---- help: how it was calculated ----
        System.out.println();
        System.out.print("Help: show how this was calculated? (1 = Yes, 2 = No): ");
        if (sc.nextInt() == 1) {
            System.out.println();
            System.out.println(CostCalculator.helpText());
            System.out.println();
            System.out.println(CostCalculator.report(vName, vType, distance, mileage, range, price, toll));
        }

        con.close();
    }
}