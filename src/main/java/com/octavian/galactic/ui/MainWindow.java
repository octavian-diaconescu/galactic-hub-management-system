package com.octavian.galactic.ui;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.mission.Mission;
import com.octavian.galactic.model.mission.MissionResult;
import com.octavian.galactic.model.mission.MissionType;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.spaceship.SpaceShipFactory;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.service.MissionDispatcher;
import com.octavian.galactic.service.DockingFeeBreakdown;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.*;

public class MainWindow {

    private static final String DARK_CLASS = "dark-theme";

    private final Stage stage;
    private final BorderPane root;
    private final BorderPane contentPane;
    private TableView<DockingBay> bayTable;
    /** Stable list so updates use setAll; TableView.refresh() avoids stale cells when entity equals() is identity-only (UUID). */
    private final ObservableList<DockingBay> bayItems = FXCollections.observableArrayList();
    private Label statusLabel;
    private Label themeToggle;
    private boolean darkMode = true;

    /** Refresh callback for the currently visible right-panel. */
    private Runnable contentRefresh = () -> {};

    /** Filled before {@link #refreshAll()} after a mission dispatch so {@link #showMissionForm()} can restore the CLI report into the new TextArea. */
    private String pendingMissionCliReport;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public MainWindow(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.contentPane = new BorderPane();

        root.getStyleClass().add(DARK_CLASS);

        VBox topBar = new VBox();
        topBar.getChildren().addAll(buildMenuBar(), buildToolBar());
        root.setTop(topBar);

        SplitPane split = new SplitPane();
        split.getStyleClass().add("main-split");
        split.getItems().addAll(buildLeftPanel(), buildContentWrapper());
        split.setDividerPositions(0.28);
        root.setCenter(split);

        root.setBottom(buildStatusBar());
    }

    public void show() {
        Scene scene = new Scene(root, 1280, 820);
        String css = Objects.requireNonNull(
                getClass().getResource("app.css"),
                "app.css not found on classpath"
        ).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.setTitle("Omega Station — Galactic Hub Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();

        showDashboard();
        refreshBayTable();
    }

    // -------------------------------------------------------------------------
    // Menu bar
    // -------------------------------------------------------------------------

    private MenuBar buildMenuBar() {
        MenuBar bar = new MenuBar();
        bar.getStyleClass().add("top-menu-bar");

        // Station
        Menu station = new Menu("Station");
        addItem(station, "Dashboard",              e -> showDashboard());
        addItem(station, "Add docking bay",        e -> showAddDockingBayPanel());
        addItem(station, "Register ship (factory)", e -> showRegisterFactoryShipPanel());
        station.getItems().add(new SeparatorMenuItem());
        addItem(station, "End-of-Day Billing",     e -> showBillingPanel());
        addItem(station, "Emergency Evacuation",   e -> showEvacuationPanel());
        addItem(station, "Manage Fuel Depot",      e -> showFuelDepotPanel());
        station.getItems().add(new SeparatorMenuItem());
        addItem(station, "Exit",                   e -> Platform.exit());

        // Ships
        Menu ships = new Menu("Ships");
        addItem(ships, "Docked Ship Stats",        e -> showDockedShipsPanel());
        ships.getItems().add(new SeparatorMenuItem());
        addItem(ships, "Dock a Ship",              e -> showDockShipForm());
        addItem(ships, "Undock a Ship",            e -> showUndockForm());
        ships.getItems().add(new SeparatorMenuItem());
        addItem(ships, "Find Heaviest Cargo Ship", e -> showHeaviestCargoPanel());

        // Operations
        Menu ops = new Menu("Operations");
        addItem(ops, "Dispatch Mission",           e -> showMissionForm());
        addItem(ops, "Hazard Scan",                e -> showHazardScanPanel());
        addItem(ops, "Personnel Report",           e -> showPersonnelPanel());

        // Help
        Menu help = new Menu("Help");
        addItem(help, "About",                     e -> showAboutPanel());

        bar.getMenus().addAll(station, ships, ops, help);
        return bar;
    }

    private void addItem(Menu menu, String label, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        MenuItem item = new MenuItem(label);
        item.setOnAction(handler);
        menu.getItems().add(item);
    }

    // -------------------------------------------------------------------------
    // Tool bar
    // -------------------------------------------------------------------------

    private HBox buildToolBar() {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("tool-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(5, 14, 5, 14));

        Button refresh = new Button("↻  Refresh");
        refresh.getStyleClass().add("toolbar-btn");
        refresh.setOnAction(e -> { refreshAll(); setStatus("Data refreshed"); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        themeToggle = new Label("☀  Light Mode");
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.setCursor(Cursor.HAND);
        themeToggle.setOnMouseClicked(e -> toggleTheme());

        bar.getChildren().addAll(refresh, spacer, themeToggle);
        return bar;
    }

    // -------------------------------------------------------------------------
    // Left panel — docking bay list
    // -------------------------------------------------------------------------

    private Node buildLeftPanel() {
        bayTable = new TableView<>(bayItems);
        bayTable.getStyleClass().add("bay-table");
        bayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        bayTable.setPlaceholder(new Label("No docking bays"));

        TableColumn<DockingBay, String> numCol = col("#", b -> String.valueOf(b.getBayNumber()), 40);
        TableColumn<DockingBay, String> nameCol = col("Name", b -> b.getName(), 120);
        TableColumn<DockingBay, String> sizeCol = col("Size", b -> b.getBaySize().toString(), 70);
        TableColumn<DockingBay, String> statusCol = col("Status", b -> b.isOccupied() ? "OCCUPIED" : "EMPTY", 80);
        TableColumn<DockingBay, String> shipCol = col("Ship", b -> {
            if (!b.isOccupied() || b.getSpaceShip() == null) return "—";
            return b.getSpaceShip().getName();
        }, 120);

        statusCol.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("OCCUPIED")
                        ? "-fx-text-fill: #fbbf24; -fx-font-weight: bold;"
                        : "-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            }
        });

        //noinspection unchecked
        bayTable.getColumns().addAll(numCol, nameCol, sizeCol, statusCol, shipCol);
        VBox.setVgrow(bayTable, Priority.ALWAYS);

        Label title = new Label("DOCKING BAYS");
        title.getStyleClass().add("panel-section-title");

        VBox panel = new VBox(8, title, bayTable);
        panel.getStyleClass().add("left-panel");
        panel.setPadding(new Insets(12, 8, 12, 12));
        VBox.setVgrow(bayTable, Priority.ALWAYS);
        return panel;
    }

    private void refreshBayTable() {
        List<DockingBay> bays = new ArrayList<>(AppContext.getDockingRepo().findAll());
        bays.sort(Comparator.comparingInt(DockingBay::getBayNumber));
        bayItems.setAll(bays);
        bayTable.refresh();
    }

    // -------------------------------------------------------------------------
    // Right content wrapper
    // -------------------------------------------------------------------------

    private Node buildContentWrapper() {
        contentPane.getStyleClass().add("content-pane");
        return contentPane;
    }

    /** Show content in the right panel and register a zero-arg refresh callback for it. */
    private void setContent(Node content, Runnable refresh) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.getStyleClass().add("content-scroll");
        contentPane.setCenter(scroll);
        this.contentRefresh = refresh;
    }

    /** Convenience overload for panels that have no meaningful refresh (static views). */
    private void setContent(Node content) {
        setContent(content, () -> {});
    }

    /** Refresh the bay table on the left AND re-render the current right panel. */
    private void refreshAll() {
        refreshBayTable();
        contentRefresh.run();
    }

    // -------------------------------------------------------------------------
    // Status bar
    // -------------------------------------------------------------------------

    private Node buildStatusBar() {
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-text");
        statusLabel.textProperty().bind(AppContext.statusProperty);

        Label dbLabel = new Label("● DB Connected");
        dbLabel.getStyleClass().addAll("status-text", "status-db");

        HBox bar = new HBox();
        bar.getStyleClass().add("status-bar");
        bar.setPadding(new Insets(5, 14, 5, 14));
        bar.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(statusLabel, spacer, dbLabel);
        return bar;
    }

    private void setStatus(String msg) {
        AppContext.setStatus(msg);
    }

    private void setStatus(String msg, boolean error) {
        AppContext.setStatus((error ? "✗  " : "✓  ") + msg);
    }

    // -------------------------------------------------------------------------
    // Theme toggle
    // -------------------------------------------------------------------------

    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            root.getStyleClass().add(DARK_CLASS);
            themeToggle.setText("☀  Light Mode");
        } else {
            root.getStyleClass().remove(DARK_CLASS);
            themeToggle.setText("🌙  Dark Mode");
        }
    }

    // =========================================================================
    // Content panels
    // =========================================================================

    // -------------------------------------------------------------------------
    // Dashboard
    // -------------------------------------------------------------------------

    private void showDashboard() {
        refreshBayTable();
        Map<Integer, DockingBay> bays = AppContext.getHub().getDockingBays();
        int total = bays.size();
        long occupied = bays.values().stream().filter(DockingBay::isOccupied).count();
        long free = total - occupied;
        int shipCount = AppContext.getShipRepo().findAll().size();
        FuelDepot depot = AppContext.getFuelDepot();
        double fuelPct = depot.getFuelCapacity() > 0
                ? depot.getFuelLevel() * 100.0 / depot.getFuelCapacity()
                : 0;

        Label heading = sectionLabel("Station Dashboard");

        // Stats row
        HBox stats = new HBox(12);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
                statCard("Total Bays",     String.valueOf(total),                    "stat-neutral"),
                statCard("Occupied",       String.valueOf(occupied),                  "stat-warning"),
                statCard("Available",      String.valueOf(free),                      "stat-success"),
                statCard("Registered Ships", String.valueOf(shipCount),              "stat-neutral"),
                statCard("Fuel Level",     String.format("%.0f%%", fuelPct),
                        fuelPct < 20 ? "stat-danger" : fuelPct < 50 ? "stat-warning" : "stat-success")
        );

        // Fuel progress
        Label fuelLabel = infoLabel("Fuel Depot: " + depot.getName());
        ProgressBar fuelBar = new ProgressBar(fuelPct / 100.0);
        fuelBar.getStyleClass().add("fuel-progress");
        fuelBar.setMaxWidth(Double.MAX_VALUE);

        Label fuelDetail = infoLabel(String.format("%,d / %,d units  (%.1f%%)",
                depot.getFuelLevel(), depot.getFuelCapacity(), fuelPct));
        fuelDetail.getStyleClass().add("secondary-text");

        // Quick actions hint
        Label hint = new Label("Use the menus above to dock ships, dispatch missions, run billing, and more.");
        hint.getStyleClass().addAll("secondary-text", "hint-label");
        hint.setWrapText(true);

        VBox content = new VBox(18, heading, stats, new Separator(),
                fuelLabel, fuelBar, fuelDetail, new Separator(), hint);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showDashboard);
        setStatus("Dashboard loaded");
    }

    private VBox statCard(String label, String value, String styleClass) {
        Label val = new Label(value);
        val.getStyleClass().addAll("stat-value", styleClass);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        VBox box = new VBox(2, val, lbl);
        box.getStyleClass().add("stat-card");
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setMinWidth(110);
        return box;
    }

    // -------------------------------------------------------------------------
    // Docked Ships Stats
    // -------------------------------------------------------------------------

    private void showDockedShipsPanel() {
        refreshBayTable();
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);

        Label heading = sectionLabel("Docked Ship Statistics");

        if (occupied.isEmpty()) {
            setContent(new VBox(18, heading, emptyState("No ships are currently docked.")), this::showDockedShipsPanel);
            setStatus("No docked ships");
            return;
        }

        List<SpaceShip> ships = occupied.stream().map(DockingBay::getSpaceShip).toList();

        TableView<SpaceShip> table = new TableView<>(FXCollections.observableArrayList(ships));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("data-table");

        //noinspection unchecked
        table.getColumns().addAll(
                col("Name",  s -> s.getName(),                              130),
                col("Type",  s -> s.getClass().getSimpleName(),             90),
                col("Size",  s -> s.getShipSize().toString(),               70),
                col("Fuel",  s -> s.getFuelLevel() + "%",                   60),
                col("Hull",  s -> s.getHullIntegrity() + "%",               60),
                col("Crew",  s -> String.valueOf(s.getCrewMembers().size()), 50),
                col("Cargo", s -> {
                    if (s instanceof CargoShip cs) {
                        SpaceShip loaded = AppContext.getShipRepo().findByIdWithCargo(cs.getId()).orElse(cs);
                        if (loaded instanceof CargoShip csLoaded) {
                            double w = csLoaded.getCargoManifest().entrySet().stream()
                                    .mapToDouble(e -> e.getKey().getWeight() * e.getValue()).sum();
                            return String.format("%.1f Tonnes", w);
                        }
                    }
                    return "N/A";
                }, 80)
        );

        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPrefHeight(350);

        VBox content = new VBox(16, heading, table);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showDockedShipsPanel);
        setStatus("Showing " + ships.size() + " docked ship(s)");
    }

    // -------------------------------------------------------------------------
    // Dock Ship Form  (the required "form" for the assignment)
    // -------------------------------------------------------------------------

    private void showDockShipForm() {
        refreshBayTable();
        List<SpaceShip> undocked = AppContext.getShipRepo().findUndockedShips();
        List<DockingBay> emptyBays = AppContext.getHub().getBaysByStatus(false);

        Label heading = sectionLabel("Dock a Ship");
        Label subheading = infoLabel("Assign an undocked ship to an available docking bay.");
        subheading.getStyleClass().add("secondary-text");

        ComboBox<SpaceShip> shipPicker = new ComboBox<>(FXCollections.observableArrayList(undocked));
        shipPicker.setPromptText("Select ship…");
        shipPicker.setMaxWidth(Double.MAX_VALUE);
        shipPicker.setConverter(new StringConverter<>() {
            public String toString(SpaceShip s) {
                return s == null ? "" : s.getName() + "  [" + s.getClass().getSimpleName() + " / " + s.getShipSize() + "]";
            }
            public SpaceShip fromString(String s) { return null; }
        });

        ComboBox<DockingBay> bayPicker = new ComboBox<>(FXCollections.observableArrayList(emptyBays));
        bayPicker.setPromptText("Select bay…");
        bayPicker.setMaxWidth(Double.MAX_VALUE);
        bayPicker.setConverter(new StringConverter<>() {
            public String toString(DockingBay b) {
                return b == null ? "" : "Bay " + b.getBayNumber() + " — " + b.getName() + "  [" + b.getBaySize() + "]";
            }
            public DockingBay fromString(String s) { return null; }
        });

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback-idle");

        Label dockSizeHint = new Label();
        dockSizeHint.setWrapText(true);
        dockSizeHint.getStyleClass().add("secondary-text");

        Button dockBtn = primaryBtn("Dock Ship");
        dockBtn.setDefaultButton(true);

        final boolean noShipsToDock = undocked.isEmpty();
        final boolean noEmptyBays = emptyBays.isEmpty();

        Runnable updateDockSizeHint = () -> {
            if (noShipsToDock || noEmptyBays) {
                return;
            }
            SpaceShip s = shipPicker.getValue();
            DockingBay b = bayPicker.getValue();
            if (s == null || b == null) {
                dockSizeHint.setText("");
                dockBtn.setDisable(false);
                return;
            }
            if (s.getShipSize().compareTo(b.getBaySize()) > 0) {
                dockSizeHint.setText(String.format(
                        "Cannot dock: ship «%s» is size %s but bay «%s» is only size %s. Choose a bay of size %s or larger.",
                        s.getName(), s.getShipSize(), b.getName(), b.getBaySize(), s.getShipSize()));
                dockSizeHint.getStyleClass().setAll("feedback-error");
                dockBtn.setDisable(true);
            } else {
                dockSizeHint.setText(String.format(
                        "Ship size %s fits this %s bay — ready to dock.",
                        s.getShipSize(), b.getBaySize()));
                dockSizeHint.getStyleClass().setAll("secondary-text");
                dockBtn.setDisable(false);
            }
        };
        shipPicker.valueProperty().addListener((o, a, n) -> updateDockSizeHint.run());
        bayPicker.valueProperty().addListener((o, a, n) -> updateDockSizeHint.run());

        dockBtn.setOnAction(e -> {
            SpaceShip ship = shipPicker.getValue();
            DockingBay bay  = bayPicker.getValue();
            if (ship == null || bay == null) {
                feedback.setText("Please select both a ship and a bay.");
                feedback.getStyleClass().setAll("feedback-error");
                setStatus("Validation failed", true);
                return;
            }
            if (ship.getShipSize().compareTo(bay.getBaySize()) > 0) {
                feedback.setText(String.format(
                        "Docking refused: a %s ship cannot use a %s bay. Select a bay with size at least %s.",
                        ship.getShipSize(), bay.getBaySize(), ship.getShipSize()));
                feedback.getStyleClass().setAll("feedback-error");
                setStatus("Ship too large for selected bay", true);
                return;
            }
            try {
                AppContext.getHub().assignShipToBay(ship.getId(), bay.getBayNumber());
                setStatus("Docked " + ship.getName() + " → " + bay.getName());
                refreshAll();
            } catch (Exception ex) {
                feedback.setText("✗  " + ex.getMessage());
                feedback.getStyleClass().setAll("feedback-error");
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("Dock Ship",
                formRow("Ship", shipPicker),
                formRow("Bay", bayPicker),
                dockSizeHint,
                dockBtn,
                feedback
        );

        if (noShipsToDock) {
            form.getChildren().add(0, infoNote("⚠  No undocked ships are currently waiting."));
            dockBtn.setDisable(true);
        }
        if (noEmptyBays) {
            form.getChildren().add(0, infoNote("⚠  All docking bays are currently occupied."));
            dockBtn.setDisable(true);
        }
        if (!noShipsToDock && !noEmptyBays) {
            updateDockSizeHint.run();
        }

        VBox content = new VBox(16, heading, subheading, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showDockShipForm);
        setStatus("Dock Ship form ready");
    }

    // -------------------------------------------------------------------------
    // Undock Form
    // -------------------------------------------------------------------------

    private void showUndockForm() {
        refreshBayTable();
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);

        Label heading = sectionLabel("Undock a Ship");
        Label sub = infoLabel("Select an occupied bay to clear.");
        sub.getStyleClass().add("secondary-text");

        ComboBox<DockingBay> bayPicker = new ComboBox<>(FXCollections.observableArrayList(occupied));
        bayPicker.setPromptText("Select occupied bay…");
        bayPicker.setMaxWidth(Double.MAX_VALUE);
        bayPicker.setConverter(new StringConverter<>() {
            public String toString(DockingBay b) {
                return b == null ? "" : "Bay " + b.getBayNumber() + " — " + b.getSpaceShip().getName() + "  [" + b.getBaySize() + "]";
            }
            public DockingBay fromString(String s) { return null; }
        });

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback-idle");

        Button undockBtn = dangerBtn("Undock Ship");
        undockBtn.setOnAction(e -> {
            DockingBay bay = bayPicker.getValue();
            if (bay == null) {
                feedback.setText("Please select a bay.");
                feedback.getStyleClass().setAll("feedback-error");
                return;
            }
            try {
                String shipName = bay.getSpaceShip().getName();
                AppContext.getHub().unassignShipFromBay(bay.getSpaceShip().getId());
                setStatus("Undocked " + shipName);
                refreshAll();
            } catch (Exception ex) {
                feedback.setText("✗  " + ex.getMessage());
                feedback.getStyleClass().setAll("feedback-error");
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("Undock Ship", formRow("Bay", bayPicker), undockBtn, feedback);

        if (occupied.isEmpty()) {
            form.getChildren().add(0, infoNote("⚠  No ships are currently docked."));
            undockBtn.setDisable(true);
        }

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showUndockForm);
        setStatus("Undock form ready");
    }

    // -------------------------------------------------------------------------
    // Mission Dispatch Form
    // -------------------------------------------------------------------------

    private void showMissionForm() {
        refreshBayTable();
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
        List<SpaceShip> docked = occupied.stream().map(DockingBay::getSpaceShip).toList();

        Label heading = sectionLabel("Dispatch Mission");
        Label sub = infoLabel("Select a docked ship, choose a mission type and distance.");
        sub.getStyleClass().add("secondary-text");

        ComboBox<SpaceShip> shipPicker = new ComboBox<>(FXCollections.observableArrayList(docked));
        shipPicker.setPromptText("Select ship…");
        shipPicker.setMaxWidth(Double.MAX_VALUE);
        shipPicker.setConverter(new StringConverter<>() {
            public String toString(SpaceShip s) {
                return s == null ? "" : s.getName() + "  [" + s.getClass().getSimpleName() + " | Fuel: " + s.getFuelLevel() + "%]";
            }
            public SpaceShip fromString(String s) { return null; }
        });

        ChoiceBox<MissionType> typePicker = new ChoiceBox<>(
                FXCollections.observableArrayList(MissionType.values()));
        typePicker.setMaxWidth(Double.MAX_VALUE);
        typePicker.getStyleClass().add("form-choice");

        TextField distanceField = new TextField();
        distanceField.setPromptText("e.g. 500");
        distanceField.getStyleClass().add("form-input");

        Label hint = new Label("Recommended: PATROL → FighterShip · EXPLORE → ScoutShip · HAUL → CargoShip");
        hint.getStyleClass().addAll("secondary-text", "hint-label");

        Label missionError = new Label();
        missionError.setWrapText(true);
        missionError.getStyleClass().add("feedback-idle");

        TextArea missionReport = new TextArea();
        missionReport.setEditable(false);
        missionReport.setWrapText(false);
        missionReport.setPrefRowCount(16);
        missionReport.setPromptText("Mission report will appear here after dispatch (same format as the CLI log).");
        missionReport.getStyleClass().add("log-area");

        Button dispatchBtn = primaryBtn("Dispatch Mission");
        dispatchBtn.setOnAction(e -> {
            SpaceShip ship = shipPicker.getValue();
            MissionType type = typePicker.getValue();
            String distText = distanceField.getText().strip();

            missionError.setText("");
            missionReport.clear();

            if (ship == null || type == null || distText.isBlank()) {
                missionError.setText("Please fill in all fields.");
                missionError.getStyleClass().setAll("feedback-error");
                return;
            }
            try {
                int distance = Integer.parseInt(distText);
                if (distance <= 0) throw new NumberFormatException("Distance must be positive");

                SpaceShip resolved = ship;
                if (type == MissionType.HAUL) {
                    resolved = AppContext.getShipRepo().findByIdWithCargo(ship.getId()).orElse(ship);
                }

                Mission mission = new Mission(
                        "Sector " + (int)(Math.random() * 100) + " Operation", type, distance, 1500.0);
                MissionResult result = MissionDispatcher.dispatch(resolved, mission);
                AppContext.getShipRepo().update(resolved);
                AppContext.getHub().unassignShipFromBay(resolved.getId());

                // refreshAll() rebuilds this panel and replaces missionReport — stash text first
                pendingMissionCliReport = result.formatCliSummary();
                missionError.getStyleClass().setAll("feedback-idle");
                setStatus((result.isSuccess() ? "✓ " : "✗ ") + ship.getName() + " — " + result.getNarrative());
                refreshAll();

            } catch (NumberFormatException nfe) {
                missionError.setText("✗  Distance must be a positive integer.");
                missionError.getStyleClass().setAll("feedback-error");
            } catch (Exception ex) {
                missionError.setText("✗  " + ex.getMessage());
                missionError.getStyleClass().setAll("feedback-error");
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("Mission Parameters",
                formRow("Ship", shipPicker),
                formRow("Mission Type", typePicker),
                formRow("Distance (units)", distanceField),
                hint,
                dispatchBtn,
                missionError,
                missionReport
        );

        if (docked.isEmpty()) {
            form.getChildren().add(0, infoNote("⚠  No ships are currently docked."));
            dispatchBtn.setDisable(true);
        }

        if (pendingMissionCliReport != null) {
            missionReport.setText(pendingMissionCliReport);
            pendingMissionCliReport = null;
        }

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showMissionForm);
        setStatus("Mission Dispatch form ready");
    }

    // -------------------------------------------------------------------------
    // Fuel Depot Panel
    // -------------------------------------------------------------------------

    private void showFuelDepotPanel() {
        refreshBayTable();
        FuelDepot depot = AppContext.getFuelDepotRepo().findAll().stream().findFirst().orElse(AppContext.getFuelDepot());
        AppContext.setFuelDepot(depot);

        double pct = depot.getFuelCapacity() > 0 ? depot.getFuelLevel() * 100.0 / depot.getFuelCapacity() : 0;

        Label heading = sectionLabel("Fuel Depot Management");

        Label name  = infoLabel("Depot:    " + depot.getName());
        Label level = infoLabel(String.format("Level:    %,d / %,d units", depot.getFuelLevel(), depot.getFuelCapacity()));
        Label pctLbl = infoLabel(String.format("Reserve:  %.1f%%", pct));
        pctLbl.getStyleClass().add(pct < 20 ? "text-danger" : pct < 50 ? "text-warning" : "text-success");

        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("fuel-progress");

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback-idle");

        Button refuelBtn = primaryBtn("Refuel to Capacity");
        refuelBtn.setDisable(depot.getFuelLevel() >= depot.getFuelCapacity());
        refuelBtn.setOnAction(e -> {
            try {
                int needed = depot.getFuelCapacity() - depot.getFuelLevel();
                depot.refuel(needed);
                AppContext.getFuelDepotRepo().update(depot);
                feedback.setText("✓  Depot refueled to capacity: " + depot.getFuelCapacity() + " units.");
                feedback.getStyleClass().setAll("feedback-success");
                setStatus("Fuel depot refueled");
                showFuelDepotPanel();
            } catch (Exception ex) {
                feedback.setText("✗  " + ex.getMessage());
                feedback.getStyleClass().setAll("feedback-error");
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("Depot Status", name, level, pctLbl, bar, refuelBtn, feedback);

        VBox content = new VBox(16, heading, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showFuelDepotPanel);
        setStatus("Fuel depot: " + String.format("%.1f%%", pct) + " remaining");
    }

    // -------------------------------------------------------------------------
    // Hazard Scan
    // -------------------------------------------------------------------------

    private void showHazardScanPanel() {
        refreshBayTable();
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);

        Label heading = sectionLabel("Hazard Scan");
        Label sub = infoLabel("Scans all docked ships for hazardous cargo.");
        sub.getStyleClass().add("secondary-text");

        TextArea results = new TextArea();
        results.setEditable(false);
        results.setPrefRowCount(14);
        results.getStyleClass().add("log-area");

        Button scanBtn = primaryBtn("Scan All Docked Ships");
        scanBtn.setDisable(occupied.isEmpty());
        scanBtn.setOnAction(e -> {
            if (occupied.isEmpty()) {
                results.setText("No ships docked. Nothing to scan.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("=== HAZARD SCAN INITIATED ===\n\n");
            for (DockingBay bay : occupied) {
                SpaceShip ship = bay.getSpaceShip();
                sb.append("Scanning: ").append(ship.getName())
                        .append(" [").append(ship.getClass().getSimpleName()).append("]\n");
                boolean hazard = AppContext.getHub().scanShipForHazards(ship.getId());
                if (ship instanceof CargoShip cs) {
                    SpaceShip loaded = AppContext.getShipRepo().findByIdWithCargo(cs.getId()).orElse(cs);
                    if (loaded instanceof CargoShip csLoaded) {
                        csLoaded.getCargoManifest().forEach((item, qty) -> {
                            String flag = item.getClass().getSimpleName().contains("Hazardous") ? " HAZARDOUS" : "";
                            sb.append(String.format("  - %s x%d (%.1f kg each)%s%n",
                                    item.getName(), qty, item.getWeight(), flag));
                        });
                    }
                }
                sb.append(hazard ? " HAZARDOUS MATERIALS DETECTED\n" : "  → Clean\n").append("\n");
            }
            sb.append("=== SCAN COMPLETE ===");
            results.setText(sb.toString());
            setStatus("Hazard scan complete");
        });

        VBox form = card("Scanner", scanBtn, results);
        if (occupied.isEmpty()) {
            form.getChildren().add(0, infoNote("⚠  No ships are currently docked."));
        }

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content);
        setStatus("Hazard scan ready");
    }

    // -------------------------------------------------------------------------
    // Personnel Report
    // -------------------------------------------------------------------------

    private void showPersonnelPanel() {
        refreshBayTable();
        Set<CrewMember> personnel = AppContext.getHub().generatePersonnelReport();

        Label heading = sectionLabel("Personnel Report");
        Label sub = infoLabel("All crew registered across known ships.");
        sub.getStyleClass().add("secondary-text");

        if (personnel.isEmpty()) {
            VBox content = new VBox(16, heading, sub, emptyState("No crew members registered."));
            content.setPadding(new Insets(20, 24, 24, 24));
            setContent(content, this::showPersonnelPanel);
            setStatus("No personnel found");
            return;
        }

        TableView<CrewMember> table = new TableView<>(FXCollections.observableArrayList(personnel));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("data-table");
        table.setPrefHeight(380);

        //noinspection unchecked
        table.getColumns().addAll(
                col("Name",    c -> c.getName(),               160),
                col("Rank",    c -> c.toString().replaceAll(".*\\((.*)\\).*", "$1"), 100),
                col("Species", c -> c.getSpecies().toString(), 100)
        );

        Label total = infoLabel("Total personnel: " + personnel.size());
        total.getStyleClass().add("secondary-text");

        VBox content = new VBox(16, heading, sub, table, total);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showPersonnelPanel);
        setStatus("Personnel: " + personnel.size() + " crew members");
    }

    // -------------------------------------------------------------------------
    // Heaviest Cargo
    // -------------------------------------------------------------------------

    private void showHeaviestCargoPanel() {
        refreshBayTable();
        Label heading = sectionLabel("Find Heaviest Cargo Ship");
        Label sub = infoLabel("Searches for the cargo ship carrying the most weight.");
        sub.getStyleClass().add("secondary-text");

        ToggleGroup scope = new ToggleGroup();
        RadioButton docked  = new RadioButton("Among docked ships");
        RadioButton allTime = new RadioButton("All time (registered)");
        docked.setToggleGroup(scope);
        allTime.setToggleGroup(scope);
        docked.setSelected(true);
        docked.getStyleClass().add("form-radio");
        allTime.getStyleClass().add("form-radio");

        Label result = new Label();
        result.setWrapText(true);
        result.getStyleClass().add("feedback-idle");

        Button findBtn = primaryBtn("Find Heaviest");
        findBtn.setOnAction(e -> {
            String filter = docked.isSelected() ? "docked" : "all time";
            Optional<CargoShip> found = AppContext.getHub().findHeaviestCargoShip(filter);
            if (found.isEmpty()) {
                result.setText("No cargo ships found in the selected scope.");
                result.getStyleClass().setAll("feedback-error");
                setStatus("No cargo ships found");
            } else {
                CargoShip ship = found.get();
                double weight = ship.getCargoManifest().entrySet().stream()
                        .mapToDouble(en -> en.getKey().getWeight() * en.getValue()).sum();
                result.setText(String.format("✓  Heaviest: %s\n   Cargo weight: %.2f kg\n   Type: %s | Size: %s | Fuel: %d%%",
                        ship.getName(), weight, ship.getClass().getSimpleName(), ship.getShipSize(), ship.getFuelLevel()));
                result.getStyleClass().setAll("feedback-success");
                setStatus("Heaviest: " + ship.getName() + " (" + String.format("%.1f", weight) + " kg)");
            }
        });

        VBox form = card("Search Scope",
                docked, allTime, new Separator(), findBtn, result);

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showHeaviestCargoPanel);
        setStatus("Heaviest cargo search ready");
    }

    // -------------------------------------------------------------------------
    // Add docking bay
    // -------------------------------------------------------------------------

    private void showAddDockingBayPanel() {
        refreshBayTable();

        Label heading = sectionLabel("Add Docking Bay");
        Label sub = infoLabel("Creates a new empty bay and saves it to the database. Bay number is assigned by the hub.");
        sub.getStyleClass().add("secondary-text");

        TextField nameField = new TextField();
        nameField.setPromptText("Bay display name");
        nameField.getStyleClass().add("form-input");

        ComboBox<Size> sizePicker = new ComboBox<>(FXCollections.observableArrayList(Size.values()));
        sizePicker.getSelectionModel().selectFirst();
        sizePicker.setMaxWidth(Double.MAX_VALUE);

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback-idle");

        Button createBtn = primaryBtn("Create bay");
        createBtn.setOnAction(e -> {
            String name = nameField.getText() == null ? "" : nameField.getText().strip();
            Size size = sizePicker.getValue();
            if (name.isEmpty()) {
                feedback.setText("Please enter a bay name.");
                feedback.getStyleClass().setAll("feedback-error");
                return;
            }
            if (size == null) {
                feedback.setText("Please select a bay size.");
                feedback.getStyleClass().setAll("feedback-error");
                return;
            }
            try {
                DockingBay bay = new DockingBay(name, size, false);
                AppContext.getHub().buildDockingBay(bay);
                feedback.setText("✓  Created bay «" + name + "» (" + size + "). It is now available for docking.");
                feedback.getStyleClass().setAll("feedback-success");
                setStatus("New docking bay: " + name);
                nameField.clear();
                refreshAll();
            } catch (Exception ex) {
                feedback.setText("✗  " + ex.getMessage());
                feedback.getStyleClass().setAll("feedback-error");
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("New bay",
                formRow("Name", nameField),
                formRow("Max ship size", sizePicker),
                createBtn,
                feedback
        );

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showAddDockingBayPanel);
        setStatus("Add docking bay");
    }

    // -------------------------------------------------------------------------
    // Register ship from factory
    // -------------------------------------------------------------------------

    private void showRegisterFactoryShipPanel() {
        refreshBayTable();

        Label heading = sectionLabel("Register ship (factory)");
        Label sub = infoLabel("Uses SpaceShipFactory.createRandomArrival() — random type, size, stats, and name — then persists the ship so it can dock.");
        sub.getStyleClass().add("secondary-text");

        Label lastShip = new Label();
        lastShip.setWrapText(true);
        lastShip.getStyleClass().add("secondary-text");

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback-idle");

        Button genBtn = primaryBtn("Generate & register incoming ship");
        genBtn.setOnAction(e -> {
            try {
                SpaceShip ship = SpaceShipFactory.createRandomArrival();
                AppContext.getHub().registerShip(ship);
                lastShip.setText(String.format("Last registered: %s  [%s | %s | fuel %d%% | hull %d%%]",
                        ship.getName(), ship.getClass().getSimpleName(), ship.getShipSize(),
                        ship.getFuelLevel(), ship.getHullIntegrity()));
                feedback.setText("✓  Ship saved to the database. It appears in «Dock a Ship» when undocked.");
                feedback.getStyleClass().setAll("feedback-success");
                setStatus("Registered: " + ship.getName());
                refreshAll();
            } catch (Exception ex) {
                feedback.setText("✗  " + ex.getMessage());
                feedback.getStyleClass().setAll("feedback-error");
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("Factory",
                genBtn,
                lastShip,
                feedback
        );

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showRegisterFactoryShipPanel);
        setStatus("Register factory ship");
    }

    // -------------------------------------------------------------------------
    // End-of-Day Billing
    // -------------------------------------------------------------------------

    private void showBillingPanel() {
        refreshBayTable();
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);

        Label heading = sectionLabel("End-of-Day Billing");
        Label sub = infoLabel("Calculates docking fees, refuels ships from the depot, and repairs hulls.");
        sub.getStyleClass().add("secondary-text");

        Label warning = infoNote("⚠  This will refuel and repair all docked ships. The fuel depot will be charged accordingly.");

        TextArea log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(22);
        log.getStyleClass().add("log-area");

        Label totalLabel = new Label();
        totalLabel.getStyleClass().addAll("stat-value", "stat-success");

        Button runBtn = primaryBtn("Run End-of-Day Billing");
        runBtn.setDisable(occupied.isEmpty());
        runBtn.setOnAction(e -> {
            List<DockingBay> baysNow = AppContext.getHub().getBaysByStatus(true);
            if (baysNow.isEmpty()) {
                log.setText("No ships docked.");
                return;
            }
            try {
                StringBuilder sb = new StringBuilder("=== END-OF-DAY BILLING REPORT ===\n\n");
                double total = 0.0;
                for (DockingBay bay : baysNow) {
                    SpaceShip ship = bay.getSpaceShip();
                    DockingFeeBreakdown inv = AppContext.getHub().billDockedShipWithBreakdown(ship.getId());
                    total += inv.totalCredits();
                    sb.append(inv.formatInvoice());
                    sb.append("\n");
                }
                sb.append(String.format("TOTAL REVENUE (all docked ships): %,.2f credits%n", total));
                sb.append("\n=== REPORT COMPLETE ===");
                log.setText(sb.toString());
                totalLabel.setText(String.format("Total: %,.2f credits", total));
                setStatus(String.format("Billing complete — %.2f credits collected", total));
                refreshBayTable();
            } catch (Exception ex) {
                log.setText("✗  Error during billing: " + ex.getMessage());
                setStatus(ex.getMessage(), true);
            }
        });

        VBox form = card("Billing", warning, runBtn, log, totalLabel);
        if (occupied.isEmpty()) {
            form.getChildren().add(0, infoNote("⚠  No ships are currently docked."));
        }

        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content, this::showBillingPanel);
        setStatus("Billing panel ready");
    }

    // -------------------------------------------------------------------------
    // Emergency Evacuation
    // -------------------------------------------------------------------------

    private void showEvacuationPanel() {
        refreshBayTable();
        Label heading = sectionLabel("Emergency Evacuation");
        heading.getStyleClass().add("text-danger");

        Label sub = infoLabel("Immediately undocks all ships and evacuates all personnel. This action cannot be undone.");
        sub.setWrapText(true);
        sub.getStyleClass().addAll("secondary-text");

        Label danger = new Label("⚠  DANGER ZONE — ALL SHIPS WILL BE UNDOCKED");
        danger.getStyleClass().add("danger-banner");
        danger.setMaxWidth(Double.MAX_VALUE);
        danger.setAlignment(Pos.CENTER);
        danger.setPadding(new Insets(10));

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.getStyleClass().add("feedback-idle");

        Button confirmBtn = dangerBtn("CONFIRM — Trigger Emergency Evacuation");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Emergency Evacuation");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This will undock ALL ships immediately. Proceed?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        AppContext.getHub().emergencyEvacuation();
                        feedback.setText("✓  Emergency evacuation complete. All ships undocked.");
                        feedback.getStyleClass().setAll("feedback-success");
                        setStatus("Emergency evacuation triggered");
                        refreshBayTable();
                    } catch (Exception ex) {
                        feedback.setText("✗  " + ex.getMessage());
                        feedback.getStyleClass().setAll("feedback-error");
                        setStatus(ex.getMessage(), true);
                    }
                }
            });
        });

        VBox form = card("Evacuation Control", danger, confirmBtn, feedback);
        VBox content = new VBox(16, heading, sub, form);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content);
        setStatus("Emergency evacuation panel ready");
    }

    // -------------------------------------------------------------------------
    // About
    // -------------------------------------------------------------------------

    private void showAboutPanel() {
        Label heading = sectionLabel("About");
        Label text = new Label("""
                Galactic Hub Management System
                
                A space station operations application for docking ships, managing crews
                and cargo, dispatching missions, and persisting data in PostgreSQL.
                
                Tech Stack: Java 25 · JavaFX 23 · JPA/Hibernate · PostgreSQL
                
                Developed for academic purposes.
                """);
        text.setWrapText(true);
        text.getStyleClass().add("about-text");

        VBox card = card("Application Info", text);
        VBox content = new VBox(16, heading, card);
        content.setPadding(new Insets(20, 24, 24, 24));
        setContent(content);
        setStatus("About");
    }

    // =========================================================================
    // UI helpers
    // =========================================================================

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<T, String> extractor, double pref) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(extractor.apply(data.getValue())));
        c.setPrefWidth(pref);
        return c;
    }

    private VBox card(String title, Node... children) {
        Label lbl = new Label(title);
        lbl.getStyleClass().add("card-title");

        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(16));
        box.getChildren().add(lbl);
        box.getChildren().addAll(children);
        return box;
    }

    private HBox formRow(String labelText, Node field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        lbl.setMinWidth(140);
        lbl.setMaxWidth(140);
        HBox row = new HBox(12, lbl, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private Button primaryBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-primary");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Button dangerBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-danger");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-heading");
        return l;
    }

    private Label infoLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("info-label");
        l.setWrapText(true);
        return l;
    }

    private Label infoNote(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("info-note");
        l.setWrapText(true);
        l.setPadding(new Insets(8, 12, 8, 12));
        return l;
    }

    private VBox emptyState(String message) {
        Label l = new Label(message);
        l.getStyleClass().add("empty-state");
        VBox box = new VBox(l);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        return box;
    }
}
