package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

import java.util.Map;

public class DashboardController implements RefreshablePanel {

    @FXML private HBox statsRow;
    @FXML private Label fuelLabel;
    @FXML private ProgressBar fuelBar;
    @FXML private Label fuelDetail;

    @FXML
    private void initialize() {
        refresh();
    }

    @Override
    public void refresh() {
        Map<Integer, DockingBay> bays = AppContext.getHub().getDockingBays();
        int total = bays.size();
        long occupied = bays.values().stream().filter(DockingBay::isOccupied).count();
        long free = total - occupied;
        int shipCount = AppContext.getShipRepo().findAll().size();
        FuelDepot depot = AppContext.getFuelDepot();
        double fuelPct = depot.getFuelCapacity() > 0
                ? depot.getFuelLevel() * 100.0 / depot.getFuelCapacity()
                : 0;

        statsRow.getChildren().setAll(
                UiComponents.statCard("Total Bays", String.valueOf(total), "stat-neutral"),
                UiComponents.statCard("Occupied", String.valueOf(occupied), "stat-warning"),
                UiComponents.statCard("Available", String.valueOf(free), "stat-success"),
                UiComponents.statCard("Registered Ships", String.valueOf(shipCount), "stat-neutral"),
                UiComponents.statCard("Fuel Level", String.format("%.0f%%", fuelPct),
                        fuelPct < 20 ? "stat-danger" : fuelPct < 50 ? "stat-warning" : "stat-success")
        );

        fuelLabel.setText("Fuel Depot: " + depot.getName());
        fuelBar.setProgress(fuelPct / 100.0);
        fuelDetail.setText(String.format("%,d / %,d units  (%.1f%%)",
                depot.getFuelLevel(), depot.getFuelCapacity(), fuelPct));

        AppContext.setStatus("Dashboard loaded");
    }
}
