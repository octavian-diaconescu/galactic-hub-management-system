package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;

public class HazardScanController implements RefreshablePanel {

    @FXML private VBox warningsBox;
    @FXML private Button scanBtn;
    @FXML private TextArea results;

    @Override
    public void refresh() {
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
        warningsBox.getChildren().clear();
        scanBtn.setDisable(occupied.isEmpty());
        if (occupied.isEmpty()) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  No ships are currently docked."));
        }
        AppContext.setStatus("Hazard scan ready");
    }

    @FXML
    private void onScan() {
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
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
        AppContext.setStatus("Hazard scan complete");
    }
}
