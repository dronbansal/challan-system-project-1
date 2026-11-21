-- Database Schema for Traffic Signal Simulation with Challan Records
-- Course: CSE2003 | Object Oriented Programming using JAVA | Group 17
-- Tables: vehicles, violations, challans

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT,
    registration_number VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    entry_time VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS violations (
    violation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    direction VARCHAR(10) NOT NULL,
    signal_state VARCHAR(10) NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    violation_time VARCHAR(30) NOT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
);

CREATE TABLE IF NOT EXISTS challans (
    challan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    violation_id INTEGER NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    violation_type VARCHAR(50) NOT NULL,
    fine_amount DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    issued_at VARCHAR(30) NOT NULL,
    FOREIGN KEY (violation_id) REFERENCES violations(violation_id)
);
