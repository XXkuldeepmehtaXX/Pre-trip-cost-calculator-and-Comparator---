import java.util.Locale;

public class CostCalculator {

    // ---------- the formulas ----------

    // Formula 1: energy needed (litres for fuel/hybrid, kWh for EV)
    public static double energyUsed(double distance, double mileage) {
        return distance / mileage;
    }

    // Formula 2: energy cost
    public static double energyCost(double distance, double mileage, double price) {
        return energyUsed(distance, mileage) * price;
    }

    // Formula 3: refuel / charge stops (trip starts with a full tank, so subtract 1)
    public static int stops(double distance, double range) {
        return Math.max(0, (int) Math.ceil(distance / range) - 1);
    }

    // Formula 4: total cost
    public static double total(double energyCost, double toll) {
        return energyCost + toll;
    }

    // ---------- short result shown after Calculate ----------

    public static String summary(double distance, double mileage, double range,
                                 double price, double toll) {
        double energy = energyCost(distance, mileage, price);
        int s = stops(distance, range);
        double t = total(energy, toll);
        return String.format(Locale.US,
            "Distance     : %.1f km%n"
          + "Energy cost  : Rs %.0f%n"
          + "Toll         : Rs %.0f%n"
          + "Refuel stops : %d%n"
          + "Total        : Rs %.0f",
            distance, energy, toll, s, t);
    }

    // ---------- general explanation shown in the Help window ----------

    public static String helpText() {
        String nl = System.lineSeparator();
        return "HOW THE COST CALCULATOR WORKS" + nl
             + "============================" + nl
             + "Inputs: distance (from the API), vehicle mileage, vehicle range," + nl
             + "price per unit (Rs per litre or per kWh) and the toll." + nl + nl
             + "1. Energy needed = Distance / Mileage" + nl
             + "   (litres for Fuel and Hybrid, kWh for EV)" + nl + nl
             + "2. Energy cost = Energy needed x Price per unit" + nl + nl
             + "3. Stops = ceil(Distance / Range) - 1" + nl
             + "   ceil rounds up, because we cannot do half a refuel." + nl
             + "   We subtract 1 because the trip starts with a full tank." + nl + nl
             + "4. Total cost = Energy cost + Toll";
    }

    // ---------- step-by-step working with the real numbers ----------

    public static String report(String name, String type, double distance,
                                double mileage, double range, double price, double toll) {

        String unit = type.equalsIgnoreCase("EV") ? "kWh" : "litres";
        String nl = System.lineSeparator();

        double units = energyUsed(distance, mileage);
        double energy = energyCost(distance, mileage, price);
        double fills = distance / range;
        int fillsRounded = (int) Math.ceil(fills);
        int stopCount = stops(distance, range);
        double totalCost = total(energy, toll);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Vehicle  : %s (%s)%n", name, type));
        sb.append(String.format(Locale.US, "Distance : %.1f km%n%n", distance));

        sb.append("STEP 1: Energy needed").append(nl);
        sb.append("  Formula : Energy = Distance / Mileage").append(nl);
        sb.append(String.format(Locale.US, "  Working : %.1f / %.1f = %.2f %s%n%n",
                distance, mileage, units, unit));

        sb.append("STEP 2: Energy cost").append(nl);
        sb.append("  Formula : Energy cost = Energy x Price per unit").append(nl);
        sb.append(String.format(Locale.US, "  Working : %.2f x %.2f = Rs %.2f%n%n",
                units, price, energy));

        sb.append("STEP 3: Refuel / charge stops").append(nl);
        sb.append("  Formula : Stops = ceil(Distance / Range) - 1").append(nl);
        sb.append(String.format(Locale.US,
                "  Working : ceil(%.1f / %.1f) - 1 = ceil(%.2f) - 1 = %d - 1 = %d%n%n",
                distance, range, fills, fillsRounded, stopCount));

        sb.append("STEP 4: Total cost").append(nl);
        sb.append("  Formula : Total = Energy cost + Toll").append(nl);
        sb.append(String.format(Locale.US, "  Working : %.2f + %.2f = Rs %.2f%n%n",
                energy, toll, totalCost));

        sb.append(String.format(Locale.US, "RESULT  : Rs %.0f total, %d %s",
                totalCost, stopCount, stopCount == 1 ? "stop" : "stops"));

        return sb.toString();
    }
}