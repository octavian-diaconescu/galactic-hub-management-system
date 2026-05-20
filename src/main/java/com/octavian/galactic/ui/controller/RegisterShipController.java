package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.spaceship.SpaceShipFactory;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class RegisterShipController implements RefreshablePanel, MainWindowAware {

    @FXML private Label feedback;
    @FXML private Label detailsPlaceholder;
    @FXML private VBox shipDetailsBox;
    @FXML private Label shipNameLabel;
    @FXML private Label shipTypeLabel;
    @FXML private Label shipSizeLabel;
    @FXML private Label shipFuelLabel;
    @FXML private ProgressBar shipFuelBar;
    @FXML private Label shipHullLabel;
    @FXML private ProgressBar shipHullBar;

    private MainWindowController mainWindow;
    private SpaceShip lastRegisteredShip;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @Override
    public void refresh() {
        if (lastRegisteredShip != null) {
            showShipDetails(lastRegisteredShip);
        }
        AppContext.setStatus("Register factory ship");
    }

    @FXML
    private void onGenerate() {
        try {
            SpaceShip ship = SpaceShipFactory.createRandomArrival();
            AppContext.getHub().registerShip(ship);
            lastRegisteredShip = ship;
            showShipDetails(ship);
            feedback.setText("✓  Ship saved to the database. It appears in «Dock a Ship» when undocked.");
            feedback.getStyleClass().setAll("feedback-success");
            AppContext.setStatus("Registered: " + ship.getName());
            mainWindow.refreshAll();
        } catch (Exception ex) {
            feedback.setText("✗  " + ex.getMessage());
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }

    private void showShipDetails(SpaceShip ship) {
        detailsPlaceholder.setVisible(false);
        detailsPlaceholder.setManaged(false);
        shipDetailsBox.setVisible(true);
        shipDetailsBox.setManaged(true);

        shipNameLabel.setText(ship.getName());
        shipTypeLabel.setText(formatShipType(ship));
        shipSizeLabel.setText(ship.getShipSize().toString());

        int fuel = ship.getFuelLevel();
        shipFuelLabel.setText(fuel + "%");
        shipFuelBar.setProgress(fuel / 100.0);

        int hull = ship.getHullIntegrity();
        shipHullLabel.setText(hull + "%");
        shipHullBar.setProgress(hull / 100.0);
    }

    private static String formatShipType(SpaceShip ship) {
        String simple = ship.getClass().getSimpleName();
        if (simple.endsWith("Ship")) {
            simple = simple.substring(0, simple.length() - 4);
        }
        return simple;
    }
}
