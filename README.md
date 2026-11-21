# Traffic Signal Simulation with Challan Records

**Course:** CSE2003 | Object Oriented Programming using JAVA | B.Tech CSE Semester 3 | Mini Project  
**Group:** Group 17  
**Summary:** A simulation of a traffic junction with signals as threads and a record of violations.

## Team Members
| Role | Name | UID |
| :--- | :--- | :--- |
| Leader | Prativa Nayak | 115830 |
| Member 1 | Dron Bansal | 126035 |
| Member 2 | Satyam Kumar | 117532 |
| Member 3 | Sahib Singh | 125983 |

## Modules Implemented
1. **Junction and signal setup** — 4-way junction (`NORTH`, `EAST`, `SOUTH`, `WEST`) with `Signal` instances and thread priorities.
2. **Signal cycle simulation** — Concurrent `Signal` threads cycling `RED -> GREEN -> YELLOW -> RED` via `Thread.sleep()`, `interrupt()`, and `setPriority()`.
3. **Vehicle entry** — `Car`, `Bike`, and `Truck` creation via GUI form or random simulator, persisted to `vehicles`.
4. **Violation detection** — `Violation` interface checks signal state on crossing and throws `SignalJumpException` on `RED`, logging to `violations`.
5. **Challan records** — Issues `Challan` fines (`Bike`: 500, `Car`: 1000, `Truck`: 2500), persisted via JDBC `Statement` & `PreparedStatement` in `challans`, with payment status updates in the UI.

## Syllabus Concepts Applied
- **Unit 1:** Classes `Signal`, `Vehicle`, `Challan`; constructors (including constructor chaining); `this` reference.
- **Unit 2:** Inheritance `Vehicle` -> `Car` / `Bike` / `Truck`; interface `Violation`; package `traffic.sim`.
- **Unit 3:** Signal thread life cycle (`sleep`, `interrupt`, priorities); custom exception `SignalJumpException`.
- **Unit 4:** JDBC `Statement` and `PreparedStatement` for `vehicles`, `violations`, and `challans` storage.
- **Unit 5:** Swing `GridLayout` signal panel with colour `JLabel`s; `FlowLayout`; mouse events (`MouseAdapter`).

## How to Compile & Run
```bash
# Compile all Java files in package traffic.sim
mkdir -p out
javac -d out src/traffic/sim/*.java

# Run with SQLite JDBC driver (recommended)
java -cp "out:sqlite-jdbc.jar" traffic.sim.Main

# Or run without external JARs (uses built-in in-memory fallback)
java -cp out traffic.sim.Main
```

To use MySQL instead of SQLite:
```bash
java -Ddb.url="jdbc:mysql://localhost:3306/traffic_sim" -Ddb.user="root" -Ddb.pass="your_password" -cp "out:mysql-connector-j.jar" traffic.sim.Main
```
