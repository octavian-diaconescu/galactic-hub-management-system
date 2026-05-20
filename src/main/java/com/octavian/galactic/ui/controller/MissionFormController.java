package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.mission.Mission;
import com.octavian.galactic.model.mission.MissionResult;
import com.octavian.galactic.model.mission.MissionType;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.service.MissionDispatcher;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

public class MissionFormController implements RefreshablePanel, MainWindowAware {

    @FXML private VBox warningsBox;
    @FXML private ComboBox<SpaceShip> shipPicker;
    @FXML private ChoiceBox<MissionType> typePicker;
    @FXML private TextField distanceField;
    @FXML private Button dispatchBtn;
    @FXML private Label missionError;
    @FXML private TextArea missionReport;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @FXML
    private void initialize() {
        typePicker.setItems(FXCollections.observableArrayList(MissionType.values()));
        shipPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(SpaceShip s) {
                return s == null ? "" : s.getName() + "  [" + s.getClass().getSimpleName()
                        + " | Fuel: " + s.getFuelLevel() + "%]";
            }
            @Override
            public SpaceShip fromString(String s) { return null; }
        });
    }

    @Override
    public void refresh() {
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
        List<SpaceShip> docked = occupied.stream().map(DockingBay::getSpaceShip).toList();
        shipPicker.setItems(FXCollections.observableArrayList(docked));
        warningsBox.getChildren().clear();
        dispatchBtn.setDisable(docked.isEmpty());
        if (docked.isEmpty()) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  No ships are currently docked."));
        }

        if (MainWindowController.pendingMissionCliReport != null) {
            missionReport.setText(MainWindowController.pendingMissionCliReport);
            MainWindowController.pendingMissionCliReport = null;
        }

        AppContext.setStatus("Mission Dispatch form ready");
    }

    @FXML
    private void onDispatch() {
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
            if (distance <= 0) {
                throw new NumberFormatException("Distance must be positive");
            }

            SpaceShip resolved = ship;
            if (type == MissionType.HAUL) {
                resolved = AppContext.getShipRepo().findByIdWithCargo(ship.getId()).orElse(ship);
            }

            Mission mission = new Mission(
                    "Sector " + (int) (Math.random() * 100) + " Operation", type, distance, 1500.0);
            MissionResult result = MissionDispatcher.dispatch(resolved, mission);
            AppContext.getShipRepo().update(resolved);
            AppContext.getHub().unassignShipFromBay(resolved.getId());

            MainWindowController.pendingMissionCliReport = result.formatCliSummary();
            missionError.getStyleClass().setAll("feedback-idle");
            AppContext.setStatus((result.isSuccess() ? "✓ " : "✗ ") + ship.getName() + " — " + result.getNarrative());
            mainWindow.refreshAll();

        } catch (NumberFormatException nfe) {
            missionError.setText("✗  Distance must be a positive integer.");
            missionError.getStyleClass().setAll("feedback-error");
        } catch (Exception ex) {
            missionError.setText("✗  " + ex.getMessage());
            missionError.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }
}
