package model;

public class Customer {
    private String name;
    private String contact;
    private String roomType;
    private int roomNumber;       // e.g. Single-3 → roomNumber = 3
    private String status;
    private int guests;
    private int days;
    private String checkInDate;
    private String checkOutDate;
    private double totalBill;

    public Customer(String name, String contact, String roomType, int roomNumber,
                    String status, int guests, int days, String checkInDate, String checkOutDate, double totalBill) {
        this.name         = name;
        this.contact      = contact;
        this.roomType     = roomType;
        this.roomNumber   = roomNumber;
        this.status       = status;
        this.guests       = guests;
        this.days         = days;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalBill    = totalBill;
    }

    // Getters
    public String getName()      { return name; }
    public String getContact()   { return contact; }
    public String getRoomType()  { return roomType; }
    public int getRoomNumber()   { return roomNumber; }
    public String getStatus()       { return status; }
    public int getGuests()          { return guests; }
    public int getDays()            { return days; }
    public String getCheckInDate()  { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public double getTotalBill()    { return totalBill; }

    // Convenience: "Single-3"
    public String getRoomLabel() { return roomType + "-" + roomNumber; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}