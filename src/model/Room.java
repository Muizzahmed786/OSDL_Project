package model;

public class Room {
    private String roomType;
    private int roomNumber;
    private boolean isBooked;

    public Room(String roomType, int roomNumber) {
        this.roomType = roomType;
        this.roomNumber = roomNumber;
        this.isBooked = false;
    }

    public String getRoomType()  { return roomType; }
    public int getRoomNumber()   { return roomNumber; }
    public boolean isBooked()    { return isBooked; }

    public void book()     { this.isBooked = true; }
    public void checkout() { this.isBooked = false; }

    // Display label e.g. "Single-3"
    public String getLabel() { return roomType + "-" + roomNumber; }
}