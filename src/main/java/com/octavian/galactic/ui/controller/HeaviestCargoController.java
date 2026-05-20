package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.RefreshablePanel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.Optional;

public class HeaviestCargoController implements RefreshablePanel {

    @FXML private RadioButton dockedScope;
    @FXML private RadioButton allTimeScope;
    @FXML private Label result;

    @FXML
    private void initialize() {
        ToggleGroup scope = new ToggleGroup();
        dockedScope.setToggleGroup(scope);
        allTimeScope.setToggleGroup(scope);
        dockedScope.setSelected(true);
    }

    @Override
    public void refresh() {
        result.setText("");
        result.getStyleClass().setAll("feedback-idle");
        AppContext.setStatus("Heaviest cargo search ready");
    }

    @FXML
    private void onFind() {
        String filter = dockedScope.isSelected() ? "docked" : "all time";
        Optional<CargoShip> found = AppContext.getHub().findHeaviestCargoShip(filter);
        if (found.isEmpty()) {
            result.setText("No cargo ships found in the selected scope.");
            result.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("No cargo ships found");
        } else {
            CargoShip ship = found.get();
            double weight = ship.getCargoManifest().entrySet().stream()
                    .mapToDouble(en -> en.getKey().getWeight() * en.getValue()).sum();
            result.setText(String.format(" Heaviest: %s\n   Cargo weight: %.2f Tonnes\n   Type: %s | Size: %s | Fuel: %d%%",
                    ship.getName(), weight, ship.getClass().getSimpleName(), ship.getShipSize(), ship.getFuelLevel()));
            result.getStyleClass().setAll("feedback-success");
            AppContext.setStatus("Heaviest: " + ship.getName() + " (" + String.format("%.1f", weight) + " Tonnes)");
        }
    }
}
