import java.util.Locale;

public class CostCalculator {

    // Builds the full trip report: each formula, then the same formula with the real numbers put in.
    public static String report(String name, String type, double distance,
                                double mileage, double range, double price, double toll) {

        boolean ev = type.equalsIgnoreCase("EV");
        String unit = ev ? "kWh" : "litres";

        // calculations
        double units = distance / mileage;            // litres or kWh needed
        double energy = units * price;                // energy cost
        double fills = distance / range;              // tanks or charges needed
        int fillsRounded = (int) Math.ceil(fills);    // round up, whole tanks only
        int stops = Math.max(0, fillsRounded - 1);    // first fill is at the start
        double total = energy + toll;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Vehicle  : %s (%s)%n", name, type));
        sb.append(String.format(Locale.US, "Distance : %.1f km%n%n", distance));

        sb.append("STEP 1: Energy needed").append(System.lineSeparator());
        sb.append("  Formula : Energy = Distance / Mileage").append(System.lineSeparator());
        sb.append(String.format(Locale.US, "  Working : %.1f / %.1f = %.2f %s%n%n",
                distance, mileage, units, unit));

        sb.append("STEP 2: Energy cost").append(System.lineSeparator());
        sb.append("  Formula : Energy cost = Energy x Price per unit").append(System.lineSeparator());
        sb.append(String.format(Locale.US, "  Working : %.2f x %.2f = Rs %.2f%n%n",
                units, price, energy));

        sb.append("STEP 3: Refuel / charge stops").append(System.lineSeparator());
        sb.append("  Formula : Stops = ceil(Distance / Range) - 1").append(System.lineSeparator());
        sb.append(String.format(Locale.US, "  Working : ceil(%.1f / %.1f) - 1 = ceil(%.2f) - 1 = %d - 1 = %d%n%n",
                distance, range, fills, fillsRounded, stops));

        sb.append("STEP 4: Total cost").append(System.lineSeparator());
        sb.append("  Formula : Total = Energy cost + Toll").append(System.lineSeparator());
        sb.append(String.format(Locale.US, "  Working : %.2f + %.2f = Rs %.2f%n%n",
                energy, toll, total));

        sb.append("RESULT  : Rs ").append(String.format(Locale.US, "%.0f", total))
          .append(" total, ").append(stops).append(stops == 1 ? " stop" : " stops");

        return sb.toString();
    }
}