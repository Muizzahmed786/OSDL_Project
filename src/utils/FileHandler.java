package utils;

import java.io.*;
import java.util.*;
import model.Customer;

public class FileHandler {

    private static final String FILE_PATH = "data/bookings.txt";

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
            System.err.println("Error saving file: " + e.getMessage());
        }
    }

    public static List<Customer> load() {
        List<Customer> list = new ArrayList<>();
        File file = new File(FILE_PATH);
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
            System.err.println("No previous data found.");
        }
        return list;
    }
}