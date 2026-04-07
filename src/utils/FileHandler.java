package utils;

import java.io.*;
import java.util.*;
import model.Customer;
import model.Room;
import model.RoomServiceOrder;

public class FileHandler {

    private static final String FILE_PATH        = "data/bookings.txt";
    private static final String CHECKOUT_PATH    = "data/checkouts.txt";
    private static final String ROOM_PATH        = "data/rooms.txt";
    private static final String ROOMSERVICE_PATH = "data/roomservice.txt";

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
                    c.getGuests()      + "," +
                    c.getDays()        + "," +
                    c.getCheckInDate() + "," +
                    c.getCheckOutDate() + "," +
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
                c.getGuests()      + "," +
                c.getDays()        + "," +
                c.getCheckInDate() + "," +
                c.getCheckOutDate() + "," +
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

    // ── Rooms (persistence) ───────────────────────────────────────────────────

    public static void saveRooms(List<Room> rooms) {
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOM_PATH))) {
            for (Room r : rooms) {
                writer.write(r.getRoomType() + "," + r.getRoomNumber());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving rooms file: " + e.getMessage());
        }
    }

    public static List<Room> loadRooms() {
        List<Room> list = new ArrayList<>();
        File file = new File(ROOM_PATH);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        int num = Integer.parseInt(parts[1].trim());
                        list.add(new Room(parts[0].trim(), num));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed room line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading rooms file: " + ROOM_PATH);
        }
        return list;
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
                if (parts.length >= 9) {
                    try {
                        int roomNumber = Integer.parseInt(parts[3].trim());
                        int guests = 1;
                        int daysIdx = 5;
                        if (parts.length >= 10) {
                            guests = Integer.parseInt(parts[5].trim());
                            daysIdx = 6;
                        }
                        int days       = Integer.parseInt(parts[daysIdx].trim());
                        String checkIn  = parts[daysIdx + 1].trim();
                        String checkOut = parts[daysIdx + 2].trim();
                        double bill    = Double.parseDouble(parts[daysIdx + 3].trim());
                        list.add(new Customer(
                            parts[0], parts[1], parts[2],
                            roomNumber, parts[4], guests, days, checkIn, checkOut, bill
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

    // ── Room Service ─────────────────────────────────────────────────────────

    /** Appends a single room-service order to roomservice.txt. */
    public static void saveRoomServiceOrder(RoomServiceOrder order) {
        new File("data").mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOMSERVICE_PATH, true))) {
            writer.write(
                order.getRoomType()   + "," +
                order.getRoomNumber() + "," +
                order.getItem()       + "," +
                order.getQuantity()   + "," +
                order.getUnitPrice()  + "," +
                order.getTotalPrice() + "," +
                order.getTimestamp()
            );
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving room service order: " + e.getMessage());
        }
    }

    /** Loads all room-service orders from roomservice.txt. */
    public static List<RoomServiceOrder> loadRoomServiceOrders() {
        List<RoomServiceOrder> list = new ArrayList<>();
        File file = new File(ROOMSERVICE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    try {
                        int roomNum    = Integer.parseInt(parts[1].trim());
                        int qty        = Integer.parseInt(parts[3].trim());
                        double uPrice  = Double.parseDouble(parts[4].trim());
                        double tPrice  = Double.parseDouble(parts[5].trim());
                        list.add(new RoomServiceOrder(
                            parts[0].trim(), roomNum, parts[2].trim(),
                            qty, uPrice, tPrice, parts[6].trim()
                        ));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed room-service line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading room service file: " + ROOMSERVICE_PATH);
        }
        return list;
    }

    /** Returns total room-service charges for a specific room. */
    public static double getRoomServiceCharges(String roomType, int roomNumber) {
        double total = 0;
        for (RoomServiceOrder o : loadRoomServiceOrders()) {
            if (o.getRoomType().equals(roomType) && o.getRoomNumber() == roomNumber) {
                total += o.getTotalPrice();
            }
        }
        return total;
    }
}