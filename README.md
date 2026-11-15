# EcoRoute: Pre-Trip Cost Calculator and Comparator

A Java application that estimates the cost of a planned trip before the journey starts. The user enters a source, a destination, the toll amount and a vehicle (Fuel, EV or Hybrid). The app shows the distance, energy cost, toll, number of refuel or charge stops and the total cost, and can explain every formula step by step.

## Why this project
Trip costs found through word of mouth or crowd-sourced prices are often inflated or inaccurate. EcoRoute works out the estimate from the vehicle's own mileage, range and fuel or charging price, together with the toll for the route (cash or FASTag).

## Features
- Three vehicle types (Fuel, EV, Hybrid) stored in a MySQL database
- Road distance between any two places, fetched from the **Nominatim** and **OSRM** APIs
- Cost calculation: energy cost, toll and refuel-stop count
- **Help button** that shows how the calculator works, with each formula and the working for your own trip
- **Navigate button** that opens the route in Google Maps
- Two front ends: a Swing window (`MainFrame`) and a console version (`TripApp`)

## How the cost is calculated

| Step | Formula |
|---|---|
| 1. Energy needed | Energy = Distance / Mileage (litres for Fuel and Hybrid, kWh for EV) |
| 2. Energy cost | Energy cost = Energy x Price per unit |
| 3. Refuel / charge stops | Stops = ceil(Distance / Range) - 1 |
| 4. Total cost | Total = Energy cost + Toll |

`ceil` rounds up because a trip cannot have half a refuel stop. One is subtracted because the trip starts with a full tank.

**Example:** 300 km, Honda Activa (mileage 50, range 400, Rs 100 per litre), toll Rs 400
1. 300 / 50 = 6 litres
2. 6 x 100 = Rs 600
3. ceil(300 / 400) - 1 = 1 - 1 = 0 stops
4. 600 + 400 = Rs 1000

## Tech stack
- Java (JDK 11 or newer)
- Swing for the graphical interface
- MySQL (XAMPP) with JDBC (`mysql-connector-j`)
- Gson for reading the JSON returned by the APIs
- Nominatim and OSRM (OpenStreetMap) for place lookup and road distance
- VS Code, compiled and run from the command prompt

## Project structure
```
src/
  DBConnection.java    opens the JDBC connection to MySQL
  DistanceAPI.java     place names to road distance (Nominatim + OSRM, parsed with Gson)
  CostCalculator.java  the formulas, the short summary and the step-by-step working
  MainFrame.java       Swing GUI with Calculate, Navigate and Help buttons
  TripApp.java         console version
lib/                   mysql-connector-j and gson jars
database.sql           creates the trip_db database with sample data
```

## How it works
```
User input (From, To, Toll, Vehicle)
        |
        v
DistanceAPI --> Nominatim (place to coordinates) --> OSRM (coordinates to distance)
        |
        v
CostCalculator <-- vehicle data from MySQL (via DBConnection)
        |
        v
Summary on screen, full working in the Help window
```

## How to run
1. Install JDK 11 or newer and XAMPP.
2. Start **MySQL** in the XAMPP Control Panel.
3. Open phpMyAdmin, go to the **SQL** tab and run `database.sql`.
4. Make sure the `mysql-connector-j` and `gson` jars are in the `lib` folder.
5. In the project folder, compile:
   ```
   javac -cp "lib/*" -d bin src/*.java
   ```
6. Run the GUI:
   ```
   java -cp "bin;lib/*" MainFrame
   ```
   or the console version:
   ```
   java -cp "bin;lib/*" TripApp
   ```

On Mac or Linux, use `:` instead of `;` in the classpath. An internet connection is needed for the distance lookup and the Navigate button.

## Using the app
1. Type the **From** and **To** places and the **toll** amount, and choose a **vehicle**.
2. Click **Calculate** to see the distance, energy cost, toll, refuel stops and total.
3. Click **Help** to see the formulas and the step-by-step working for your trip.
4. Click **Navigate** to open the route in Google Maps.

## Limitations
- Toll values in `database.sql` are sample data and must be verified before real use.
- Nominatim and OSRM are free public servers with rate limits.
- Distances come from OpenStreetMap data, so they can differ a little from the route shown on Google Maps.
- Vehicle prices and mileage are sample values and can be edited in the `vehicles` table.

## Planned work
- Compare all vehicles for one trip and highlight the cheapest option
- Route and toll tables with verified toll data
- Round trip and lodging cost

## Team
Kuldeep, Ayush and Aakash. The API integration (`DistanceAPI`) was done by Kuldeep.
