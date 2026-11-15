<h1 align="center">EcoRoute</h1>
<h3 align="center">Pre-Trip Cost Calculator and Comparator</h3>

<p align="center">
  Plan a trip, pick a vehicle, and see the exact cost with every formula explained.<br>
  <b>Created by <a href="https://github.com/XXkuldeepmehtaXX">Kuldeep Mehta</a></b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-11%2B-orange" alt="Java 11+">
  <img src="https://img.shields.io/badge/GUI-Swing-blue" alt="Swing">
  <img src="https://img.shields.io/badge/Database-MySQL-00758f" alt="MySQL">
  <img src="https://img.shields.io/badge/JSON-Gson-brightgreen" alt="Gson">
  <img src="https://img.shields.io/badge/Maps-OSRM%20%2B%20Nominatim-7ebc6f" alt="OSRM and Nominatim">
</p>

<p align="center">
  <a href="#about">About</a> |
  <a href="#features">Features</a> |
  <a href="#how-the-parts-fit-together">Architecture</a> |
  <a href="#how-the-cost-is-calculated">Formulas</a> |
  <a href="#database">Database</a> |
  <a href="#maps-and-navigation">Maps</a> |
  <a href="#logic-work">Logic</a> |
  <a href="#how-to-run">Run</a>
</p>

---

## About
Trip costs found through word of mouth or crowd-sourced prices are often inflated or inaccurate. EcoRoute works out the estimate from the vehicle's own mileage, range and fuel or charging price, together with the toll for the route (cash or FASTag).

The user enters a source, a destination, the toll amount and a vehicle (Fuel, EV or Hybrid). The app shows the distance, energy cost, toll, number of refuel or charge stops and the total cost, and a **Help** button explains every formula step by step.

## Features
- Three vehicle types (Fuel, EV, Hybrid) stored in a MySQL database
- Road distance between any two places, fetched from the **Nominatim** and **OSRM** APIs
- Cost calculation: energy cost, toll and refuel-stop count
- **Help button** that shows the formulas and the working for your own trip
- **Navigate button** that opens the route in Google Maps
- Two front ends: a Swing window (`MainFrame`) and a console version (`TripApp`)

## Screenshots

**Main window (`MainFrame`)** with the From, To, Toll and Vehicle fields and the Calculate, Navigate and Help buttons

<img src="screenshots/gui-main.png" alt="EcoRoute main window" width="420">

**Console version (`TripApp`)**: distance from the API, vehicle menu and trip summary

<img src="screenshots/console-run.png" alt="Console run of TripApp" width="700">

<!--
More screenshots: save your own screenshots in the screenshots folder with these names,
then remove the comment marks around this block.

**Help window**
<img src="screenshots/gui-help.png" alt="Help window with formulas and working" width="520">

**Result after Calculate**
<img src="screenshots/gui-result.png" alt="Window after clicking Calculate" width="520">

**Navigate button (Google Maps)**
<img src="screenshots/google-maps.png" alt="Route opened in Google Maps" width="620">
-->

## How the parts fit together

<img src="screenshots/architecture.png" alt="Architecture diagram" width="900">

| Class | Responsibility |
|---|---|
| `DBConnection` | opens the JDBC connection to MySQL |
| `DistanceAPI` | place names to road distance (Nominatim + OSRM, parsed with Gson) |
| `CostCalculator` | the four formulas, the short summary, the help text and the step-by-step working |
| `MainFrame` | Swing window with the Calculate, Navigate and Help buttons |
| `TripApp` | the same flow in the console |

## How the cost is calculated

<img src="screenshots/formulas.png" alt="The four formulas with a worked example" width="900">

| Step | Formula |
|---|---|
| 1. Energy needed | Energy = Distance / Mileage (litres for Fuel and Hybrid, kWh for EV) |
| 2. Energy cost | Energy cost = Energy x Price per unit |
| 3. Refuel / charge stops | Stops = ceil(Distance / Range) - 1 |
| 4. Total cost | Total = Energy cost + Toll |

`ceil` rounds up because a trip cannot have half a refuel stop. One is subtracted because the trip starts with a full tank.

## Database

<img src="screenshots/database.png" alt="Database tables" width="800">

`database.sql` creates the `trip_db` database with 10 sample vehicles and 12 sample routes.

## Maps and navigation

EcoRoute uses two kinds of map services.

### 1. Distance (OpenStreetMap services)
Both are free web APIs. The program sends an HTTP request and reads the JSON reply with Gson.

| Service | Job | Example request |
|---|---|---|
| **Nominatim** | place name to latitude and longitude | `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=Delhi` |
| **OSRM** | two coordinates to driving distance | `https://router.project-osrm.org/route/v1/driving/77.2090,28.6139;75.7873,26.9124?overview=false` |

- OSRM expects coordinates as **longitude,latitude**, the opposite order from Nominatim's `lat` and `lon`.
- OSRM returns the distance in metres, so the program divides by 1000 to get kilometres.
- Nominatim asks for a `User-Agent` header and about one request per second, so `DistanceAPI` sends the header and waits 1.1 seconds between the two lookups.

### 2. Navigate button (Google Maps)
The **Navigate** button builds a Google Maps directions link from the From and To boxes and opens it in the default browser:

```
https://www.google.com/maps/dir/?api=1&origin=Delhi&destination=Jaipur&travelmode=driving
```

| Part | Meaning |
|---|---|
| `origin` | the From place |
| `destination` | the To place |
| `travelmode=driving` | show car directions |

It needs no API key, because the program only opens a link. Place names are made safe for the URL with `URLEncoder`, and the browser is opened with `java.awt.Desktop`.

The distance used in the calculation comes from OSRM, while the route shown on Google Maps is Google's own, so the two kilometre values can differ a little.

## Logic work

### What happens when the user clicks Calculate
1. **Read and check the input.** From, To and the vehicle must be filled in, and the toll must be a number. Otherwise a message is shown and the calculation stops.
2. **Get the distance.** `DistanceAPI.getDistanceKm(from, to)` converts both places to coordinates with Nominatim, sends them to OSRM and returns the distance in km. This runs in a background thread (`SwingWorker`), so the window does not freeze while waiting for the internet.
3. **Get the vehicle data.** The selected vehicle's mileage, range and price per unit come from the `vehicles` table in MySQL.
4. **Apply the formulas** in `CostCalculator`.
5. **Show the result.** The short summary appears in the window, and the full working is saved so the **Help** button can display it.

### Flow in pseudo-code
```
read From, To, Toll, Vehicle
if any input is missing or Toll is not a number: show message, stop

distance = DistanceAPI.getDistanceKm(From, To)     (background thread)
vehicle  = row from the vehicles table

energy     = distance / vehicle.mileage
energyCost = energy * vehicle.price
stops      = max(0, ceil(distance / vehicle.range) - 1)
total      = energyCost + Toll

show summary(distance, energyCost, Toll, stops, total)
store step-by-step working for the Help window
```

### Error handling
| Situation | What the program does |
|---|---|
| MySQL is not running | shows "Could not load vehicles. Is MySQL running?" |
| Place not found or no internet | shows "Could not get distance" with the reason |
| Toll is not a number | shows "Toll must be a number." |
| From or To is empty | shows a message and does not open Maps or calculate |

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
  DistanceAPI.java     place names to road distance
  CostCalculator.java  formulas, summary, help text, step-by-step working
  MainFrame.java       Swing GUI with Calculate, Navigate and Help buttons
  TripApp.java         console version
lib/                   mysql-connector-j and gson jars
screenshots/           images used in this README
database.sql           creates the trip_db database with sample data
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

## Author
Created by **[Kuldeep Mehta](https://github.com/XXkuldeepmehtaXX)**.
