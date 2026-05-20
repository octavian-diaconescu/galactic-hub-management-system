package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.ViewLoader;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainWindowController {

    private static final String DARK_CLASS = "dark-theme";

    @FXML private BorderPane root;
    @FXML private TableView<DockingBay> bayTable;
    @FXML private TableColumn<DockingBay, String> bayNumCol;
    @FXML private TableColumn<DockingBay, String> bayNameCol;
    @FXML private TableColumn<DockingBay, String> baySizeCol;
    @FXML private TableColumn<DockingBay, String> bayStatusCol;
    @FXML private TableColumn<DockingBay, String> bayShipCol;
    @FXML private BorderPane contentPane;
    @FXML private Label themeToggle;
    @FXML private Label statusLabel;

    private final ObservableList<DockingBay> bayItems = FXCollections.observableArrayList();
    private Runnable contentRefresh = () -> {};
    private boolean darkMode = true;

    /** Filled before refreshAll() after mission dispatch so MissionFormController can restore the CLI report. */
    static String pendingMissionCliReport;

    @FXML
    private void initialize() {
        root.getStyleClass().add(DARK_CLASS);

        bayTable.setItems(bayItems);
        bayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        bayNumCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.valueOf(d.getValue().getBayNumber())));
        bayNameCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));
        baySizeCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getBaySize().toString()));
        bayStatusCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().isOccupied() ? "OCCUPIED" : "EMPTY"));
        bayShipCol.setCellValueFactory(d -> {
            DockingBay b = d.getValue();
            if (!b.isOccupied() || b.getSpaceShip() == null) {
                return new ReadOnlyStringWrapper("—");
            }
            return new ReadOnlyStringWrapper(b.getSpaceShip().getName());
        });

        bayStatusCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle(item.equals("OCCUPIED")
                        ? "-fx-text-fill: #fbbf24; -fx-font-weight: bold;"
                        : "-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            }
        });

        statusLabel.textProperty().bind(AppContext.statusProperty);
        themeToggle.setCursor(Cursor.HAND);
    }

    public void showInitialContent() {
        onDashboard();
        refreshBayTable();
    }

    public BorderPane getRoot() {
        return root;
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    @FXML private void onDashboard()      { showPanel("dashboard.fxml"); }
    @FXML private void onAddDockingBay()  { showPanel("add-docking-bay.fxml"); }
    @FXML private void onRemoveDockingBay() { showPanel("remove-docking-bay.fxml"); }
    @FXML private void onRegisterShip()   { showPanel("register-ship.fxml"); }
    @FXML private void onBilling()        { showPanel("billing.fxml"); }
    @FXML private void onEvacuation()     { showPanel("evacuation.fxml"); }
    @FXML private void onFuelDepot()      { showPanel("fuel-depot.fxml"); }
    @FXML private void onDockedShips()    { showPanel("docked-ships.fxml"); }
    @FXML private void onDockShip()       { showPanel("dock-ship-form.fxml"); }
    @FXML private void onUndock()         { showPanel("undock-form.fxml"); }
    @FXML private void onHeaviestCargo()  { showPanel("heaviest-cargo.fxml"); }
    @FXML private void onMission()        { showPanel("mission-form.fxml"); }
    @FXML private void onHazardScan()     { showPanel("hazard-scan.fxml"); }
    @FXML private void onPersonnel()      { showPanel("personnel.fxml"); }
    @FXML private void onAbout()          { showPanel("about.fxml"); }

    @FXML private void onExit() {
        Platform.exit();
    }

    @FXML private void onRefresh() {
        refreshAll();
        AppContext.setStatus("Data refreshed");
    }

    @FXML private void onToggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            root.getStyleClass().add(DARK_CLASS);
            themeToggle.setText("🔆Light Mode");
        } else {
            root.getStyleClass().remove(DARK_CLASS);
            themeToggle.setText("🌚Dark Mode");
        }
    }

    // -------------------------------------------------------------------------
    // Panel loading
    // -------------------------------------------------------------------------

    private void showPanel(String fxml) {
        ViewLoader.LoadedView<Object> loaded = ViewLoader.load(fxml);
        setContent(loaded.root(), loaded.controller());
    }

    private void setContent(Node content, Object controller) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.getStyleClass().add("content-scroll");
        contentPane.setCenter(scroll);

        if (controller instanceof MainWindowAware aware) {
            aware.setMainWindow(this);
        }
        if (controller instanceof RefreshablePanel refreshable) {
            this.contentRefresh = refreshable::refresh;
            refreshable.refresh();
        } else {
            this.contentRefresh = () -> {};
        }
    }

    public void refreshBayTable() {
        List<DockingBay> bays = new ArrayList<>(AppContext.getDockingRepo().findAll());
        bays.sort(Comparator.comparingInt(DockingBay::getBayNumber));
        bayItems.setAll(bays);
        bayTable.refresh();
    }

    public void refreshAll() {
        refreshBayTable();
        contentRefresh.run();
    }
}
