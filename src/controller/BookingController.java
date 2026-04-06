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
import utils.FileHandler;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    // ── Tab 1 — Bookings ─────────────────────────────────────────────────────
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

    // ── Tab 2 — Rooms ────────────────────────────────────────────────────────
    @FXML private Label     singleCountLabel;
    @FXML private Label     doubleCountLabel;
    @FXML private Label     deluxeCountLabel;

    @FXML private FlowPane  singleGrid;
    @FXML private FlowPane  doubleGrid;
    @FXML private FlowPane  deluxeGrid;

    // ── Tab 4: Checkout ──────────────────────────────────────────────────────
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

    // ── Tab 5 — Manage Rooms ────────────────────────────────────────────────
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

    // ── Data ─────────────────────────────────────────────────────────────────
    private ObservableList<Customer> data = FXCollections.observableArrayList();
    private ObservableList<Room>     rooms = FXCollections.observableArrayList();

    // ── Initializable ─────────────────────────────────────────────────────────
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
                checkoutSummaryLabel.setText(String.format(
                    "Guest: %s     Contact: %s\nRoom: %s     Guests: %d     Stay: %d day(s)\nTotal Bill: ₹ %.2f",
                    sel.getName(), sel.getContact(),
                    sel.getRoomLabel(), sel.getGuests(), sel.getDays(), sel.getTotalBill()
                ));
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");
                checkoutSummaryBox.getStyleClass().add("checkout-summary-active");
            } else {
                checkoutSummaryLabel.setText("← Select a row above to preview checkout details.");
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");
            }
        });

        // ── Tab 5: Manage Rooms setup ───────────────────────────────────────
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

    // ── Tab 1: Add Booking ────────────────────────────────────────────────────

    @FXML
    private void handleAddBooking() {
        String name     = nameField.getText().trim();
        String contact  = contactField.getText().trim();
        String room     = roomType.getValue();
        String guestsStr = guestsField.getText().trim();
        LocalDate checkIn  = checkInPicker.getValue();
        LocalDate checkOut = checkOutPicker.getValue();

        if (name.isEmpty() || contact.isEmpty() || room == null || guestsStr.isEmpty() || checkIn == null || checkOut == null) {
            showAlert("Please fill all fields!"); return;
        }

        int guests;
        try {
            guests = Integer.parseInt(guestsStr);
        } catch (NumberFormatException e) {
            showAlert("Number of guests must be a valid number!"); return;
        }

        if (guests <= 0) {
            showAlert("Number of guests must be at least 1!"); return;
        }
        if ("Single".equals(room) && guests > 1) {
            showAlert("Single room can accommodate at most 1 guest."); return;
        }
        if ("Double".equals(room) && guests > 2) {
            showAlert("Double room can accommodate at most 2 guests."); return;
        }
        if ("Deluxe".equals(room) && guests > 4) {
            showAlert("Deluxe room can accommodate at most 4 guests."); return;
        }
        if (!contact.matches("\\d{10}")) {
            showAlert("Contact must be a valid 10-digit number!"); return;
        }

        if (checkOut.isBefore(checkIn.plusDays(1))) {
            showAlert("Check-out date must be at least one day after check-in!"); return;
        }

        int days = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        Room available = getAvailableRoom(room);
        if (available == null) {
            showAlert("All " + room + " rooms are fully booked!\n"
                + "Available — Single: " + availableCount("Single")
                + ", Double: " + availableCount("Double")
                + ", Deluxe: " + availableCount("Deluxe"));
            return;
        }

        double total = getRoomPrice(room) * days;
        available.book();
        Customer c = new Customer(name, contact, room, available.getRoomNumber(), "Booked", guests, days, 
                                 checkIn.toString(), checkOut.toString(), total);
        data.add(c);
        FileHandler.save(data);
        updateAvailLabel();
        refreshRoomGrids();

        billLabel.setText(String.format(
            "New Booking: %s  |  Room: %s  |  Guests: %d  |  Days: %d  |  Total Bill: ₹ %.2f",
            name, available.getLabel(), guests, days, total
        ));

        nameField.clear(); contactField.clear(); guestsField.clear();
        roomType.setValue(null);
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
    }

    // ── Tab 3: Checkout ───────────────────────────────────────────────────────

    @FXML
    private void handleCheckout() {
        // Try checkout table first, fall back to bookings table
        Customer selected = checkoutTable.getSelectionModel().getSelectedItem();
        if (selected == null) selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a booking to checkout!"); return;
        }

        final Customer toCheckout = selected;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Confirm checkout for " + toCheckout.getName()
            + " (Room: " + toCheckout.getRoomLabel() + ")?");
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
                showInfo("Checkout Successful for " + toCheckout.getName() + "!");
            }
        });
    }

    // ── Tab 2: Rooms tab refresh ──────────────────────────────────────────────

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

    // ── Room Helpers ──────────────────────────────────────────────────────────

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

    // ── Tab 5: Manage Rooms Actions ──────────────────────────────────────────

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