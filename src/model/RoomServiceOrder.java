package model;

public class RoomServiceOrder {
    private String roomType;
    private int roomNumber;
    private String item;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String timestamp;

    public RoomServiceOrder(String roomType, int roomNumber, String item,
                            int quantity, double unitPrice, double totalPrice, String timestamp) {
        this.roomType   = roomType;
        this.roomNumber = roomNumber;
        this.item       = item;
        this.quantity   = quantity;
        this.unitPrice  = unitPrice;
        this.totalPrice = totalPrice;
        this.timestamp  = timestamp;
    }

    // Getters
    public String getRoomType()   { return roomType; }
    public int getRoomNumber()    { return roomNumber; }
    public String getItem()       { return item; }
    public int getQuantity()      { return quantity; }
    public double getUnitPrice()  { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getTimestamp()  { return timestamp; }

    // Convenience: "Single-3"
    public String getRoomLabel()  { return roomType + "-" + roomNumber; }
}
