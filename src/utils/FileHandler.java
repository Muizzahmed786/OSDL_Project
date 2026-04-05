package utils;

import java.io.*;
import java.util.*;
import model.Customer;

public class FileHandler {

    private static final String FILE_PATH     = "data/bookings.txt";
    private static final String CHECKOUT_PATH = "data/checkouts.txt";

    // ── Active Bookings ───────────────────────────────────────────────────────

    public static void save(List<Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Customer c : customers) {
                writer.write(
                    c.getName()        + "," +
                    c.getContact()     + "," +
                    c.getRoomType()    + "," +
                    c.getRoomNumber()  + "," +
                    c.getStatus()      + "," +
                    c.getDays()        + "," +
                    c.getTotalBill()
                );
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving bookings file: " + e.getMessage());
        }
    }

    public static List<Customer> load() {
        return readFromFile(FILE_PATH);
    }

    // ── Checkouts (append-only history) ──────────────────────────────────────

    /** Appends a single checked-out customer record to checkouts.txt. */
    public static void saveCheckout(Customer c) {
        // Ensure the data directory exists
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHECKOUT_PATH, true))) {
            writer.write(
                c.getName()        + "," +
                c.getContact()     + "," +
                c.getRoomType()    + "," +
                c.getRoomNumber()  + "," +
                "Checked Out"      + "," +
                c.getDays()        + "," +
                c.getTotalBill()
            );
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving checkout record: " + e.getMessage());
        }
    }

    /** Loads all past checkout records from checkouts.txt. */
    public static List<Customer> loadCheckouts() {
        return readFromFile(CHECKOUT_PATH);
    }

    // ── Shared Helper ─────────────────────────────────────────────────────────

    private static List<Customer> readFromFile(String path) {
        List<Customer> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 7) {
                    try {
                        int roomNumber = Integer.parseInt(parts[3].trim());
                        int days       = Integer.parseInt(parts[5].trim());
                        double bill    = Double.parseDouble(parts[6].trim());
                        list.add(new Customer(
                            parts[0], parts[1], parts[2],
                            roomNumber, parts[4], days, bill
                        ));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
        }
        return list;
    }
}