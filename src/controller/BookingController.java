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
import java.util.List;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    // ── Tab 1 — Bookings ─────────────────────────────────────────────────────
    @FXML private TextField        nameField;
    @FXML private TextField        contactField;
    @FXML private TextField        daysField;
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> roomType;

    @FXML private Label billLabel;
    @FXML private Label availLabel;

    @FXML private TableView<Customer>            table;
    @FXML private TableColumn<Customer, Number>  indexCol;
    @FXML private TableColumn<Customer, String>  nameCol;
    @FXML private TableColumn<Customer, String>  contactCol;
    @FXML private TableColumn<Customer, String>  roomCol;
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

    // ── Tab 3 — Checkout ─────────────────────────────────────────────────────
    @FXML private TextField                      checkoutSearchField;
    @FXML private TableView<Customer>            checkoutTable;
    @FXML private TableColumn<Customer, Number>  coIndexCol;
    @FXML private TableColumn<Customer, String>  coNameCol;
    @FXML private TableColumn<Customer, String>  coContactCol;
    @FXML private TableColumn<Customer, String>  coRoomCol;
    @FXML private TableColumn<Customer, Integer> coDaysCol;
    @FXML private TableColumn<Customer, Double>  coBillCol;

    @FXML private VBox  checkoutSummaryBox;
    @FXML private Label checkoutSummaryLabel;
    @FXML private Button checkoutBtn;

    // ── Data ─────────────────────────────────────────────────────────────────
    private ObservableList<Customer> data = FXCollections.observableArrayList();

    // ── Room Inventory ────────────────────────────────────────────────────────
    private static final int SINGLE_COUNT = 10;
    private static final int DOUBLE_COUNT = 20;
    private static final int DELUXE_COUNT = 8;

    private Room[] singleRooms = new Room[SINGLE_COUNT];
    private Room[] doubleRooms = new Room[DOUBLE_COUNT];
    private Room[] deluxeRooms = new Room[DELUXE_COUNT];

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
                    "Selected: %s  |  Room: %s  |  Days: %d  |  Total Bill: ₹ %.2f",
                    newSel.getName(), newSel.getRoomLabel(), newSel.getDays(), newSel.getTotalBill()
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
                    "Guest: %s     Contact: %s\nRoom: %s     Stay: %d day(s)\nTotal Bill: ₹ %.2f",
                    sel.getName(), sel.getContact(),
                    sel.getRoomLabel(), sel.getDays(), sel.getTotalBill()
                ));
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");
                checkoutSummaryBox.getStyleClass().add("checkout-summary-active");
            } else {
                checkoutSummaryLabel.setText("← Select a row above to preview checkout details.");
                checkoutSummaryBox.getStyleClass().removeAll("checkout-summary-active");
            }
        });

        // ── Load saved data & restore room states ────────────────────────────
        List<Customer> loaded = FileHandler.load();
        data.addAll(loaded);
        for (Customer c : loaded) {
            Room[] rooms = getRoomArray(c.getRoomType());
            if (rooms != null)
                for (Room r : rooms)
                    if (r.getRoomNumber() == c.getRoomNumber()) { r.book(); break; }
        }

        updateAvailLabel();
        refreshRoomGrids();
    }

    // ── Tab 1: Add Booking ────────────────────────────────────────────────────

    @FXML
    private void handleAddBooking() {
        String name     = nameField.getText().trim();
        String contact  = contactField.getText().trim();
        String room     = roomType.getValue();
        String daysText = daysField.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || room == null || daysText.isEmpty()) {
            showAlert("Please fill all fields!"); return;
        }
        if (!contact.matches("\\d{10}")) {
            showAlert("Contact must be a valid 10-digit number!"); return;
        }

        int days;
        try {
            days = Integer.parseInt(daysText);
            if (days <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert("Enter a valid positive number of days!"); return;
        }

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
        Customer c = new Customer(name, contact, room, available.getRoomNumber(), "Booked", days, total);
        data.add(c);
        FileHandler.save(data);
        updateAvailLabel();
        refreshRoomGrids();

        billLabel.setText(String.format(
            "New Booking: %s  |  Room: %s  |  Days: %d  |  Total Bill: ₹ %.2f",
            name, available.getLabel(), days, total
        ));

        nameField.clear(); contactField.clear();
        roomType.setValue(null); daysField.clear();
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
        buildGrid(singleGrid, singleRooms);
        buildGrid(doubleGrid, doubleRooms);
        buildGrid(deluxeGrid, deluxeRooms);
        updateCountLabels();
    }

    private void buildGrid(FlowPane grid, Room[] rooms) {
        grid.getChildren().clear();
        for (Room r : rooms) {
            Label cell = new Label(r.getLabel());
            cell.getStyleClass().add("room-cell");
            cell.getStyleClass().add(r.isBooked() ? "room-cell-booked" : "room-cell-available");
            grid.getChildren().add(cell);
        }
    }

    private void updateCountLabels() {
        singleCountLabel.setText(availableCount("Single") + " / " + SINGLE_COUNT + " available");
        doubleCountLabel.setText(availableCount("Double") + " / " + DOUBLE_COUNT + " available");
        deluxeCountLabel.setText(availableCount("Deluxe") + " / " + DELUXE_COUNT + " available");
    }

    // ── Room Helpers ──────────────────────────────────────────────────────────

    private void initRooms() {
        for (int i = 0; i < SINGLE_COUNT; i++) singleRooms[i] = new Room("Single", i + 1);
        for (int i = 0; i < DOUBLE_COUNT; i++) doubleRooms[i] = new Room("Double", i + 1);
        for (int i = 0; i < DELUXE_COUNT; i++) deluxeRooms[i] = new Room("Deluxe", i + 1);
    }

    private Room getAvailableRoom(String type) {
        Room[] rooms = getRoomArray(type);
        if (rooms == null) return null;
        for (Room r : rooms) if (!r.isBooked()) return r;
        return null;
    }

    private void freeRoom(String type, int number) {
        Room[] rooms = getRoomArray(type);
        if (rooms == null) return;
        for (Room r : rooms) if (r.getRoomNumber() == number) { r.checkout(); return; }
    }

    private Room[] getRoomArray(String type) {
        switch (type) {
            case "Single": return singleRooms;
            case "Double": return doubleRooms;
            case "Deluxe": return deluxeRooms;
            default:       return null;
        }
    }

    private int availableCount(String type) {
        Room[] rooms = getRoomArray(type);
        if (rooms == null) return 0;
        int count = 0;
        for (Room r : rooms) if (!r.isBooked()) count++;
        return count;
    }

    private void updateAvailLabel() {
        availLabel.setText(String.format(
            "Single: %d/%d   |   Double: %d/%d   |   Deluxe: %d/%d",
            availableCount("Single"), SINGLE_COUNT,
            availableCount("Double"), DOUBLE_COUNT,
            availableCount("Deluxe"), DELUXE_COUNT
        ));
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