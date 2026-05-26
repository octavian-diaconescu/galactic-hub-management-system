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

import java.util.List;

public class UndockFormController implements RefreshablePanel, MainWindowAware {

    @FXML private VBox warningsBox;
    @FXML private ComboBox<DockingBay> bayPicker;
    @FXML private Button undockBtn;
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
                return b == null ? "" : "Bay " + b.getBayNumber() + " — " + b.getSpaceShip().getName()
                        + "  [" + b.getBaySize() + "]";
            }
            @Override
            public DockingBay fromString(String s) { return null; }
        });
    }

    @Override
    public void refresh() {
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
        bayPicker.setItems(FXCollections.observableArrayList(occupied));
        feedback.setText("");
        feedback.getStyleClass().setAll("feedback-idle");
        warningsBox.getChildren().clear();
        undockBtn.setDisable(occupied.isEmpty());
        if (occupied.isEmpty()) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  No ships are currently docked."));
        }
        AppContext.setStatus("Undock form ready");
    }

    @FXML
    private void onUndock() {
        DockingBay bay = bayPicker.getValue();
        if (bay == null) {
            feedback.setText("Please select a bay.");
            feedback.getStyleClass().setAll("feedback-error");
            return;
        }
        try {
            String shipName = bay.getSpaceShip().getName();
            AppContext.getHub().unassignShipFromBay(bay.getSpaceShip().getId());
            AppContext.setStatus("Undocked " + shipName);
            mainWindow.refreshAll();
        } catch (Exception ex) {
            feedback.setText("✗  " + ex.getMessage());
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }
}
