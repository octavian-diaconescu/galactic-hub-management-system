package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class FuelDepotController implements RefreshablePanel, MainWindowAware {

    @FXML private Label nameLabel;
    @FXML private Label levelLabel;
    @FXML private Label pctLabel;
    @FXML private ProgressBar fuelBar;
    @FXML private Button refuelBtn;
    @FXML private Label feedback;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @Override
    public void refresh() {
        FuelDepot depot = AppContext.getFuelDepotRepo().findAll().stream().findFirst()
                .orElse(AppContext.getFuelDepot());
        AppContext.setFuelDepot(depot);

        double pct = depot.getFuelCapacity() > 0
                ? depot.getFuelLevel() * 100.0 / depot.getFuelCapacity()
                : 0;

        nameLabel.setText("Depot:    " + depot.getName());
        levelLabel.setText(String.format("Level:    %,d / %,d units", depot.getFuelLevel(), depot.getFuelCapacity()));
        pctLabel.setText(String.format("Reserve:  %.1f%%", pct));
        pctLabel.getStyleClass().removeAll("text-danger", "text-warning", "text-success");
        pctLabel.getStyleClass().add(pct < 20 ? "text-danger" : pct < 50 ? "text-warning" : "text-success");
        fuelBar.setProgress(pct / 100.0);
        refuelBtn.setDisable(depot.getFuelLevel() >= depot.getFuelCapacity());
        feedback.setText("");
        feedback.getStyleClass().setAll("feedback-idle");

        AppContext.setStatus("Fuel depot: " + String.format("%.1f%%", pct) + " remaining");
    }

    @FXML
    private void onRefuel() {
        FuelDepot depot = AppContext.getFuelDepot();
        try {
            int needed = depot.getFuelCapacity() - depot.getFuelLevel();
            depot.refuel(needed);
            AppContext.getFuelDepotRepo().update(depot);
            feedback.setText("✓  Depot refueled to capacity: " + depot.getFuelCapacity() + " units.");
            feedback.getStyleClass().setAll("feedback-success");
            AppContext.setStatus("Fuel depot refueled");
            mainWindow.refreshAll();
        } catch (Exception ex) {
            feedback.setText("✗  " + ex.getMessage());
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }
}
