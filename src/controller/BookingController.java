package controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import model.Customer;
import model.Room;
import model.RoomServiceOrder;
import utils.FileHandler;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BookingController implements Initializable {

    // ── Tab 1 — Bookings 
    @FXML private TextField        nameField;
    @FXML private TextField        contactField;
    @FXML private DatePicker       checkInPicker;
    @FXML private DatePicker       checkOutPicker;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> roomType;
    @FXML private TextField        guestsField;

    @FXML private Label billLabel;
    @FXML private Label availLabel;

    @FXML private TableView<Customer>            table;
    @FXML private TableColumn<Customer, Number>  indexCol;
    @FXML private TableColumn<Customer, String>  nameCol;
    @FXML private TableColumn<Customer, String>  contactCol;
    @FXML private TableColumn<Customer, String>  roomCol;
    @FXML private TableColumn<Customer, Integer> guestsCol;
    @FXML private TableColumn<Customer, String>  checkInCol;
    @FXML private TableColumn<Customer, String>  checkOutCol;
    @FXML private TableColumn<Customer, Integer> daysCol;
    @FXML private TableColumn<Customer, Double>  billCol;
    @FXML private TableColumn<Customer, String>  statusCol;

    // ── Tab 2 — Rooms 
    @FXML private Label     singleCountLabel;
    @FXML private Label     doubleCountLabel;
    @FXML private Label     deluxeCountLabel;

    @FXML private FlowPane  singleGrid;
    @FXML private FlowPane  doubleGrid;
    @FXML private FlowPane  deluxeGrid;

    // ── Tab 4: Checkout 
    @FXML private TextField                      checkoutSearchField;
    @FXML private TableView<Customer>            checkoutTable;
    @FXML private TableColumn<Customer, Number>  coIndexCol;
    @FXML private TableColumn<Customer, String>  coNameCol;
    @FXML private TableColumn<Customer, String>  coContactCol;
    @FXML private TableColumn<Customer, String>  coRoomCol;
    @FXML private TableColumn<Customer, Integer> coGuestsCol;
    @FXML private TableColumn<Customer, String>  coCheckInCol;
    @FXML private TableColumn<Customer, String>  coCheckOutCol;
    @FXML private TableColumn<Customer, Integer> coDaysCol;
    @FXML private TableColumn<Customer, Double>  coBillCol;

    @FXML private VBox  checkoutSummaryBox;
    @FXML private Label checkoutSummaryLabel;
    @FXML private Button checkoutBtn;

    // ── Tab 5 — Room Service 
    @FXML private ComboBox<String> rsRoomCombo;
    @FXML private ComboBox<String> rsItemCombo;
    @FXML private TextField        rsQuantityField;
    @FXML private Label            rsPriceLabel;
    @FXML private TextField        rsSearchField;
    @FXML private Label            rsTotalLabel;

    @FXML private TableView<RoomServiceOrder>            rsTable;
    @FXML private TableColumn<RoomServiceOrder, Number>  rsIndexCol;
    @FXML private TableColumn<RoomServiceOrder, String>  rsRoomCol;
    @FXML private TableColumn<RoomServiceOrder, String>  rsItemCol;
    @FXML private TableColumn<RoomServiceOrder, Integer> rsQtyCol;
    @FXML private TableColumn<RoomServiceOrder, Double>  rsPriceCol;
    @FXML private TableColumn<RoomServiceOrder, String>  rsTimeCol;

    // ── Tab 6 — Manage Rooms 
    @FXML private ComboBox<String> mRoomTypeCombo;
    @FXML private TextField        mRoomNumberField;
    
    @FXML private TableView<Room>  mSingleTable;
    @FXML private TableColumn<Room, Number> mSingleNumCol;
    @FXML private TableColumn<Room, String> mSingleStatusCol;

    @FXML private TableView<Room>  mDoubleTable;
    @FXML private TableColumn<Room, Number> mDoubleNumCol;
    @FXML private TableColumn<Room, String> mDoubleStatusCol;

    @FXML private TableView<Room>  mDeluxeTable;
    @FXML private TableColumn<Room, Number> mDeluxeNumCol;
    @FXML private TableColumn<Room, String> mDeluxeStatusCol;

    // ── Data 
    private ObservableList<Customer>         data     = FXCollections.observableArrayList();
    private ObservableList<Room>             rooms    = FXCollections.observableArrayList();
    private ObservableList<RoomServiceOrder> rsData   = FXCollections.observableArrayList();

    // ── Room Service Menu Prices 
    private static final Map<String, Double> MENU = new LinkedHashMap<>();
    static {
        MENU.put("Tea",            50.0);
        MENU.put("Coffee",         80.0);
        MENU.put("Juice",         100.0);
        MENU.put("Snacks",        150.0);
        MENU.put("Meal",          300.0);
        MENU.put("Bottled Water",  30.0);
    }

    // ── Initializable 
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initRooms();

        // ── Tab 1: Bookings setup ────────────────────────────────────────────
        roomType.getItems().addAll("Single", "Double", "Deluxe");
        roomType.setOnAction(e -> updateAvailLabel());

        indexCol.setCellValueFactory(col ->
            new ReadOnlyObjectWrapper<>(table.getItems().indexOf(col.getValue()) + 1));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomLabel"));
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("guests"));
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        daysCol.setCellValueFactory(new PropertyValueFactory<>("days"));
        billCol.setCellValueFactory(new PropertyValueFactory<>("totalBill"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        FilteredList<Customer> filteredData = new FilteredList<>(data, p -> true);
        searchField.textProperty().addListener((obs, o, newVal) -> {
            filteredData.setPredicate(c -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return c.getName().toLowerCase().contains(lower)
                    || c.getContact().toLowerCase().contains(lower)
                    || c.getRoomType().toLowerCase().contains(lower)
                    || c.getRoomLabel().toLowerCase().contains(lower);
            });
        });
        table.setItems(filteredData);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, newSel) -> {
            if (newSel != null) {
                billLabel.setText(String.format(
                    "Selected: %s  |  Room: %s  |  Guests: %d  |  Days: %d  |  Total Bill: ₹ %.2f",
                    newSel.getName(), newSel.getRoomLabel(), newSel.getGuests(), newSel.getDays(), newSel.getTotalBill()
                ));
            } else {
                billLabel.setText("Select a booking or add one to see the bill.");
            }
        });

        // ── Tab 3: Checkout table setup ──────────────────────────────────────
        coIndexCol.setCellValueFactory(col ->
            new ReadOnlyObjectWrapper<>(checkoutTable.getItems().indexOf(col.getValue()) + 1));
        coNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        coContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        coRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomLabel"));
        coGuestsCol.setCellValueFactory(new PropertyValueFactory<>("guests"));
        coCheckInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        coCheckOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        coDaysCol.setCellValueFactory(new PropertyValueFactory<>("days"));
        coBillCol.setCellValueFactory(new PropertyValueFactory<>("totalBill"));

        // Checkout table shares same filtered source but is separately filterable
        FilteredList<Customer> checkoutFiltered = new FilteredList<>(data, p -> true);
        checkoutSearchField.textProperty().addListener((obs, o, nv) -> {
            checkoutFiltered.setPredicate(c -> {
                if (nv == null || nv.isBlank()) return true;
                String lower = nv.toLowerCase();
                return c.getName().toLowerCase().contains(lower)
                    || c.getRoomLabel().toLowerCase().contains(lower)
                    || c.getContact().toLowerCase().contains(lower);
            });
        });
        checkoutTable.setItems(checkoutFiltered);

        checkoutTable.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) {
                double rsCharges = FileHandler.getRoomServiceCharges(sel.getRoomType(), sel.getRoomNumber());
                double grandTotal = sel.getTotalBill() + rsCharges;
                String rsSummary = rsCharges > 0
                    ? String.format("\nRoom Service: ₹ %.2f     Grand Total: ₹ %.2f", rsCharges, grandTotal)
                    : "";
                checkoutSummaryLabel.setText(String.format(
                    "Guest: %s     Contact: %s\nRoom: %s     Guests: %d     Stay: %d day(s)\nRoom Bill: ₹ %.2f%s",
                    sel.getName(), sel.getContact(),
                    sel.getRoomLabel(), sel.getGuests(), sel.getDays(), sel.getTotalBill(), rsSummary
                ));
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");
                checkoutSummaryBox.getStyleClass().add("checkout-summary-active");
            } else {
                checkoutSummaryLabel.setText("← Select a row above to preview checkout details.");
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");
            }
        });

        // ── Tab 5: Room Service setup ───────────────────────────────────────
        rsItemCombo.getItems().addAll(MENU.keySet());

        rsIndexCol.setCellValueFactory(col ->
            new ReadOnlyObjectWrapper<>(rsTable.getItems().indexOf(col.getValue()) + 1));
        rsRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomLabel"));
        rsItemCol.setCellValueFactory(new PropertyValueFactory<>("item"));
        rsQtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        rsPriceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        rsTimeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Load saved orders
        rsData.addAll(FileHandler.loadRoomServiceOrders());

        FilteredList<RoomServiceOrder> rsFiltered = new FilteredList<>(rsData, p -> true);
        rsSearchField.textProperty().addListener((obs, o, nv) -> {
            rsFiltered.setPredicate(order -> {
                if (nv == null || nv.isBlank()) return true;
                String lower = nv.toLowerCase();
                return order.getRoomLabel().toLowerCase().contains(lower)
                    || order.getItem().toLowerCase().contains(lower);
            });
        });
        rsTable.setItems(rsFiltered);

        // Price preview listener
        rsItemCombo.setOnAction(e -> updateRsPricePreview());
        rsQuantityField.textProperty().addListener((obs, o, nv) -> updateRsPricePreview());

        // Update total when room selection changes
        rsRoomCombo.setOnAction(e -> updateRsTotalLabel());

        // ── Tab 6: Manage Rooms setup ───────────────────────────────────────
        mRoomTypeCombo.getItems().addAll("Single", "Double", "Deluxe");
        
        setupRoomTable(mSingleTable, mSingleNumCol, mSingleStatusCol, "Single");
        setupRoomTable(mDoubleTable, mDoubleNumCol, mDoubleStatusCol, "Double");
        setupRoomTable(mDeluxeTable, mDeluxeNumCol, mDeluxeStatusCol, "Deluxe");

        // ── Load saved data & restore room states ────────────────────────────
        List<Customer> loaded = FileHandler.load();
        data.addAll(loaded);
        for (Customer c : loaded) {
            List<Room> rList = getRoomList(c.getRoomType());
            for (Room r : rList) {
                if (r.getRoomNumber() == c.getRoomNumber()) {
                    r.book();
                    break;
                }
            }
        }

        updateAvailLabel();
        refreshRoomGrids();

        // Default dates: Today and Tomorrow
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        // Disable past dates for check-in
        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Disable past dates and dates before check-in for check-out
        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(checkInPicker.getValue().plusDays(1)));
            }
        });
    }

    // ── Tab 1: Add Booking 

    @FXML
    private void handleAddBooking() {
        String name     = nameField.getText().trim();
        String contact  = contactField.getText().trim();
        String room     = roomType.getValue();
        String guestsStr = guestsField.getText().trim();
        LocalDate checkIn  = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        Task<Customer> bookingTask = new Task<>() {
            @Override
            protected Customer call() throws Exception {
                // Validation (in background thread)
                if (name.isEmpty() || contact.isEmpty() || room == null || guestsStr.isEmpty() || checkIn == null || checkOut == null) {
                    Platform.runLater(() -> showAlert("Please fill all fields!"));
                    return null;
                }

                int guests;
                try {
                    guests = Integer.parseInt(guestsStr);
                } catch (NumberFormatException e) {
                    Platform.runLater(() -> showAlert("Number of guests must be a valid number!"));
                    return null;
                }

                if (guests <= 0) {
                    Platform.runLater(() -> showAlert("Number of guests must be at least 1!"));
                    return null;
                }
                if ("Single".equals(room) && guests > 1) {
                    Platform.runLater(() -> showAlert("Single room can accommodate at most 1 guest."));
                    return null;
                }
                if ("Double".equals(room) && guests > 2) {
                    Platform.runLater(() -> showAlert("Double room can accommodate at most 2 guests."));
                    return null;
                }
                if ("Deluxe".equals(room) && guests > 4) {
                    Platform.runLater(() -> showAlert("Deluxe room can accommodate at most 4 guests."));
                    return null;
                }
                if (!contact.matches("\\d{10}")) {
                    Platform.runLater(() -> showAlert("Contact must be a valid 10-digit number!"));
                    return null;
                }

                if (checkOut.isBefore(checkIn.plusDays(1))) {
                    Platform.runLater(() -> showAlert("Check-out date must be at least one day after check-in!"));
                    return null;
                }

                int days = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

                // Room allocation
                Room available = getAvailableRoom(room);
                if (available == null) {
                    int sCount = availableCount("Single");
                    int dCount = availableCount("Double");
                    int dxCount = availableCount("Deluxe");
                    Platform.runLater(() -> showAlert("All " + room + " rooms are fully booked!\n"
                        + "Available — Single: " + sCount
                        + ", Double: " + dCount
                        + ", Deluxe: " + dxCount));
                    return null;
                }

                // Bill calculation
                double total = getRoomPrice(room) * days;
                available.book();
                Customer c = new Customer(name, contact, room, available.getRoomNumber(), "Booked", guests, days, 
                                         checkIn.toString(), checkOut.toString(), total);

                // File saving
                List<Customer> copyForSave = new ArrayList<>(data);
                copyForSave.add(c);
                FileHandler.save(copyForSave);

                return c;
            }
        };

        bookingTask.setOnSucceeded(e -> {
            Customer c = bookingTask.getValue();
            if (c != null) {
                // UI updates
                data.add(c);
                updateAvailLabel();
                refreshRoomGrids();

                billLabel.setText(String.format(
                    "New Booking: %s  |  Room: %s  |  Guests: %d  |  Days: %d  |  Total Bill: ₹ %.2f",
                    c.getName(), c.getRoomLabel(), c.getGuests(), c.getDays(), c.getTotalBill()
                ));

                nameField.clear(); contactField.clear(); guestsField.clear();
                roomType.setValue(null);
                checkInPicker.setValue(LocalDate.now());
                checkOutPicker.setValue(LocalDate.now().plusDays(1));
            }
        });

        bookingTask.setOnFailed(e -> {
            Throwable ex = bookingTask.getException();
            if (ex != null) ex.printStackTrace();
            showAlert("An error occurred during booking.");
        });

        Thread backgroundThread = new Thread(bookingTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    // ── Tab 3: Checkout 

    @FXML
    private void handleCheckout() {
        // Try checkout table first, fall back to bookings table
        Customer selected = checkoutTable.getSelectionModel().getSelectedItem();
        if (selected == null) selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a booking to checkout!"); return;
        }

        final Customer toCheckout = selected;
        double rsCharges = FileHandler.getRoomServiceCharges(toCheckout.getRoomType(), toCheckout.getRoomNumber());
        double grandTotal = toCheckout.getTotalBill() + rsCharges;

        String confirmMsg = String.format(
            "Confirm checkout for %s (Room: %s)?\n\nRoom Charges: ₹ %.2f\nRoom Service: ₹ %.2f\nGrand Total: ₹ %.2f",
            toCheckout.getName(), toCheckout.getRoomLabel(),
            toCheckout.getTotalBill(), rsCharges, grandTotal
        );

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, confirmMsg);
        confirm.setHeaderText("Checkout Confirmation");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                freeRoom(toCheckout.getRoomType(), toCheckout.getRoomNumber());
                FileHandler.saveCheckout(toCheckout);   // persist to checkouts.txt
                data.remove(toCheckout);
                FileHandler.save(data);
                updateAvailLabel();
                refreshRoomGrids();

                checkoutSummaryLabel.setText(
                    "✔  Checkout successful. Room " + toCheckout.getRoomLabel() + " is now available.");
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");

                billLabel.setText("Checkout successful. Room " + toCheckout.getRoomLabel() + " is now available.");
                showBillPopup(toCheckout, rsCharges);
            }
        });
    }

    private void showBillPopup(Customer c, double rsCharges) {
        Alert billAlert = new Alert(Alert.AlertType.INFORMATION);
        billAlert.setTitle("Checkout Final Bill");
        billAlert.setHeaderText("Checkout Successful for " + c.getName());

        double grandTotal = c.getTotalBill() + rsCharges;
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Guest Name: %s\n", c.getName()));
        sb.append(String.format("Contact Number: %s\n", c.getContact()));
        sb.append(String.format("Room Booked: %s\n", c.getRoomLabel()));
        sb.append(String.format("Total Guests: %d\n", c.getGuests()));
        sb.append(String.format("Check-In Date: %s\n", c.getCheckInDate()));
        sb.append(String.format("Check-Out Date: %s\n", c.getCheckOutDate()));
        sb.append(String.format("Duration of Stay: %d day(s)\n", c.getDays()));
        sb.append("──────────────────────────────\n");
        sb.append(String.format("Room Charges: ₹ %.2f\n", c.getTotalBill()));
        if (rsCharges > 0) {
            sb.append(String.format("Room Service Charges: ₹ %.2f\n", rsCharges));
            sb.append("──────────────────────────────\n");
        }
        sb.append(String.format("Grand Total: ₹ %.2f", grandTotal));
        
        billAlert.setContentText(sb.toString());
        billAlert.showAndWait();
    }

    // ── Tab 2: Rooms tab refresh 

    @FXML
    private void handleRoomsTabSelected() {
        refreshRoomGrids();
    }

    private void refreshRoomGrids() {
        buildGrid(singleGrid, getRoomList("Single"));
        buildGrid(doubleGrid, getRoomList("Double"));
        buildGrid(deluxeGrid, getRoomList("Deluxe"));
        updateCountLabels();
    }

    private void buildGrid(FlowPane grid, List<Room> roomList) {
        grid.getChildren().clear();
        for (Room r : roomList) {
            Label cell = new Label(r.getLabel());
            cell.getStyleClass().add("room-cell");
            cell.getStyleClass().add(r.isBooked() ? "room-cell-booked" : "room-cell-available");
            grid.getChildren().add(cell);
        }
    }

    private void updateCountLabels() {
        singleCountLabel.setText(availableCount("Single") + " / " + totalCount("Single") + " available");
        doubleCountLabel.setText(availableCount("Double") + " / " + totalCount("Double") + " available");
        deluxeCountLabel.setText(availableCount("Deluxe") + " / " + totalCount("Deluxe") + " available");
    }

    // ── Room Helpers 

    private void initRooms() {
        rooms.setAll(FileHandler.loadRooms());
    }

    private Room getAvailableRoom(String type) {
        List<Room> rList = getRoomList(type);
        for (Room r : rList) if (!r.isBooked()) return r;
        return null;
    }

    private void freeRoom(String type, int number) {
        List<Room> rList = getRoomList(type);
        for (Room r : rList) if (r.getRoomNumber() == number) { r.checkout(); return; }
    }

    private List<Room> getRoomList(String type) {
        return rooms.filtered(r -> r.getRoomType().equals(type));
    }

    private int availableCount(String type) {
        return (int) rooms.stream()
                .filter(r -> r.getRoomType().equals(type) && !r.isBooked())
                .count();
    }

    private int totalCount(String type) {
        return (int) rooms.stream()
                .filter(r -> r.getRoomType().equals(type))
                .count();
    }

    private void updateAvailLabel() {
        availLabel.setText(String.format(
            "Single: %d/%d   |   Double: %d/%d   |   Deluxe: %d/%d",
            availableCount("Single"), totalCount("Single"),
            availableCount("Double"), totalCount("Double"),
            availableCount("Deluxe"), totalCount("Deluxe")
        ));
    }

    // ── Tab 5: Room Service 

    @FXML
    private void handleRoomServiceTabSelected() {
        refreshBookedRoomsList();
    }

    private void refreshBookedRoomsList() {
        rsRoomCombo.getItems().clear();
        for (Customer c : data) {
            String label = c.getRoomLabel();
            if (!rsRoomCombo.getItems().contains(label)) {
                rsRoomCombo.getItems().add(label);
            }
        }
    }

    private void updateRsPricePreview() {
        String item = rsItemCombo.getValue();
        String qtyStr = rsQuantityField.getText().trim();
        if (item != null && !qtyStr.isEmpty()) {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    double price = MENU.getOrDefault(item, 0.0) * qty;
                    rsPriceLabel.setText(String.format("💰 %s × %d = ₹ %.2f", item, qty, price));
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }
        rsPriceLabel.setText("Select item and quantity to see price.");
    }

    private void updateRsTotalLabel() {
        String roomLabel = rsRoomCombo.getValue();
        if (roomLabel == null) {
            rsTotalLabel.setText("Select a room to view total charges.");
            return;
        }
        // Parse room label "Type-Number"
        String[] parts = roomLabel.split("-");
        if (parts.length == 2) {
            try {
                double total = FileHandler.getRoomServiceCharges(parts[0], Integer.parseInt(parts[1]));
                rsTotalLabel.setText(String.format("Total Room Service for %s: ₹ %.2f", roomLabel, total));
            } catch (NumberFormatException e) {
                rsTotalLabel.setText("Select a room to view total charges.");
            }
        }
    }

    @FXML
    private void handlePlaceRoomServiceOrder() {
        String roomLabel = rsRoomCombo.getValue();
        String item      = rsItemCombo.getValue();
        String qtyStr    = rsQuantityField.getText().trim();

        Task<RoomServiceOrder> rsTask = new Task<>() {
            @Override
            protected RoomServiceOrder call() throws Exception {
                // Validation
                if (roomLabel == null || item == null || qtyStr.isEmpty()) {
                    Platform.runLater(() -> showAlert("Please fill all fields!"));
                    return null;
                }

                int qty;
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    Platform.runLater(() -> showAlert("Quantity must be a valid number!"));
                    return null;
                }
                if (qty <= 0) {
                    Platform.runLater(() -> showAlert("Quantity must be at least 1!"));
                    return null;
                }

                // Parse room label
                String[] parts = roomLabel.split("-");
                if (parts.length != 2) {
                    Platform.runLater(() -> showAlert("Invalid room selection!"));
                    return null;
                }
                String rType = parts[0];
                int rNum;
                try {
                    rNum = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    Platform.runLater(() -> showAlert("Invalid room number!"));
                    return null;
                }

                double unitPrice  = MENU.getOrDefault(item, 0.0);
                double totalPrice = unitPrice * qty;
                String timestamp  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                RoomServiceOrder order = new RoomServiceOrder(rType, rNum, item, qty, unitPrice, totalPrice, timestamp);

                // Save to file
                FileHandler.saveRoomServiceOrder(order);

                return order;
            }
        };

        rsTask.setOnSucceeded(e -> {
            RoomServiceOrder order = rsTask.getValue();
            if (order != null) {
                rsData.add(order);
                rsQuantityField.clear();
                rsItemCombo.setValue(null);
                rsPriceLabel.setText(String.format(
                    "✔ Ordered: %s × %d = ₹ %.2f for Room %s",
                    order.getItem(), order.getQuantity(), order.getTotalPrice(), order.getRoomLabel()
                ));
                updateRsTotalLabel();
                showInfo("Room service order placed successfully!");
            }
        });

        rsTask.setOnFailed(e -> {
            Throwable ex = rsTask.getException();
            if (ex != null) ex.printStackTrace();
            showAlert("An error occurred while placing the order.");
        });

        Thread bgThread = new Thread(rsTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    // ── Tab 6: Manage Rooms Actions 

    private void setupRoomTable(TableView<Room> table, TableColumn<Room, Number> numCol, 
                                TableColumn<Room, String> statusCol, String type) {
        numCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        statusCol.setCellValueFactory(cell -> 
            new ReadOnlyObjectWrapper<>(cell.getValue().isBooked() ? "Occupied" : "Available"));
        
        table.setItems(rooms.filtered(r -> r.getRoomType().equals(type)));
    }

    @FXML
    private void handleAddRoom() {
        String type = mRoomTypeCombo.getValue();
        String numStr = mRoomNumberField.getText().trim();

        if (type == null || numStr.isEmpty()) {
            showAlert("Please fill both Fields!"); return;
        }

        try {
            int num = Integer.parseInt(numStr);
            if (num <= 0) throw new NumberFormatException();
            
            // Check uniqueness within the same type
            for (Room r : rooms) {
                if (r.getRoomType().equals(type) && r.getRoomNumber() == num) {
                    showAlert("Room " + type + "-" + num + " already exists!");
                    return;
                }
            }

            rooms.add(new Room(type, num));
            FileHandler.saveRooms(new ArrayList<>(rooms));
            mRoomNumberField.clear();
            updateAvailLabel();
            refreshRoomGrids();
            showInfo("Room " + type + "-" + num + " added successfully.");

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid positive room number!");
        }
    }

    @FXML
    private void handleDeleteRoom() {
        Room selected = mSingleTable.getSelectionModel().getSelectedItem();
        if (selected == null) selected = mDoubleTable.getSelectionModel().getSelectedItem();
        if (selected == null) selected = mDeluxeTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Please select a room from any table to delete!"); return;
        }

        if (selected.isBooked()) {
            showAlert("Cannot delete a booked room! Please checkout the guest first.");
            return;
        }

        final Room toDelete = selected;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Delete room " + toDelete.getLabel() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                rooms.remove(toDelete);
                FileHandler.saveRooms(new ArrayList<>(rooms));
                updateAvailLabel();
                refreshRoomGrids();
                showInfo("Room deleted successfully.");
            }
        });
    }

    private double getRoomPrice(String room) {
        switch (room) {
            case "Single": return 1000;
            case "Double": return 2000;
            case "Deluxe": return 3000;
            default:       return 0;
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Input Error"); a.setHeaderText(null); a.setContentText(msg); a.show();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success"); a.setHeaderText(null); a.setContentText(msg); a.show();
    }
}