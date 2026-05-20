package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RemoveDockingBayController implements RefreshablePanel, MainWindowAware {

    @FXML private VBox warningsBox;
    @FXML private ComboBox<DockingBay> bayPicker;
    @FXML private Label occupancyHint;
    @FXML private Button removeBtn;
    @FXML private Label feedback;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @FXML
    private void initialize() {
        bayPicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(DockingBay b) {
                if (b == null) {
                    return "";
                }
                String status = b.isOccupied() && b.getSpaceShip() != null
                        ? "OCCUPIED — " + b.getSpaceShip().getName()
                        : "EMPTY";
                return "Bay " + b.getBayNumber() + " — " + b.getName() + "  [" + b.getBaySize() + ", " + status + "]";
            }

            @Override
            public DockingBay fromString(String s) {
                return null;
            }
        });
        bayPicker.valueProperty().addListener((o, a, n) -> updateOccupancyHint());
    }

    @Override
    public void refresh() {
        List<DockingBay> bays = new ArrayList<>(AppContext.getDockingRepo().findAll());
        bays.sort(Comparator.comparingInt(DockingBay::getBayNumber));
        bayPicker.setItems(FXCollections.observableArrayList(bays));
        feedback.setText("");
        feedback.getStyleClass().setAll("feedback-idle");
        warningsBox.getChildren().clear();

        if (bays.isEmpty()) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  No docking bays exist."));
            removeBtn.setDisable(true);
            occupancyHint.setText("");
        } else {
            removeBtn.setDisable(false);
            updateOccupancyHint();
        }
        AppContext.setStatus("Remove docking bay");
    }

    @FXML
    private void onRemove() {
        DockingBay bay = bayPicker.getValue();
        if (bay == null) {
            feedback.setText("Please select a bay to remove.");
            feedback.getStyleClass().setAll("feedback-error");
            return;
        }
        if (bay.isOccupied()) {
            feedback.setText("✗  Cannot remove an occupied bay. Undock the ship first.");
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("Undock ship before removing bay");
            return;
        }
        try {
            AppContext.getHub().removeDockingBay(bay.getId());
            feedback.setText("✓  Removed bay «" + bay.getName() + "» (bay " + bay.getBayNumber() + ").");
            feedback.getStyleClass().setAll("feedback-success");
            AppContext.setStatus("Removed docking bay: " + bay.getName());
            mainWindow.refreshAll();
        } catch (Exception ex) {
            feedback.setText("✗  " + ex.getMessage());
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }

    private void updateOccupancyHint() {
        DockingBay bay = bayPicker.getValue();
        if (bay == null) {
            occupancyHint.setText("");
            removeBtn.setDisable(bayPicker.getItems().isEmpty());
            return;
        }
        if (bay.isOccupied() && bay.getSpaceShip() != null) {
            occupancyHint.setText(String.format(
                    "«%s» is docked here. Use Ships → Undock a Ship before removing this bay.",
                    bay.getSpaceShip().getName()));
            occupancyHint.getStyleClass().setAll("feedback-error");
            removeBtn.setDisable(true);
        } else {
            occupancyHint.setText("This bay is empty — safe to remove.");
            occupancyHint.getStyleClass().setAll("secondary-text");
            removeBtn.setDisable(false);
        }
    }
}
