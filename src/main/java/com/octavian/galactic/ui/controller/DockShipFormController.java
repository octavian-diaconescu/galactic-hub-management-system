package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.spaceship.SpaceShip;
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

public class DockShipFormController implements RefreshablePanel, MainWindowAware {

    @FXML private VBox warningsBox;
    @FXML private ComboBox<SpaceShip> shipPicker;
    @FXML private ComboBox<DockingBay> bayPicker;
    @FXML private Label dockSizeHint;
    @FXML private Button dockBtn;
    @FXML private Label feedback;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @FXML
    private void initialize() {
        shipPicker.setConverter(shipConverter());
        bayPicker.setConverter(bayConverter());
        shipPicker.valueProperty().addListener((o, a, n) -> updateDockSizeHint());
        bayPicker.valueProperty().addListener((o, a, n) -> updateDockSizeHint());
    }

    @Override
    public void refresh() {
        List<SpaceShip> undocked = AppContext.getShipRepo().findUndockedShips();
        List<DockingBay> emptyBays = AppContext.getHub().getBaysByStatus(false);

        shipPicker.setItems(FXCollections.observableArrayList(undocked));
        bayPicker.setItems(FXCollections.observableArrayList(emptyBays));
        feedback.setText("");
        feedback.getStyleClass().setAll("feedback-idle");
        warningsBox.getChildren().clear();

        boolean noShips = undocked.isEmpty();
        boolean noBays = emptyBays.isEmpty();
        dockBtn.setDisable(noShips || noBays);

        if (noShips) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  No undocked ships are currently waiting."));
        }
        if (noBays) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  All docking bays are currently occupied."));
        }
        if (!noShips && !noBays) {
            updateDockSizeHint();
        } else {
            dockSizeHint.setText("");
        }

        AppContext.setStatus("Dock Ship form ready");
    }

    @FXML
    private void onDock() {
        SpaceShip ship = shipPicker.getValue();
        DockingBay bay = bayPicker.getValue();
        if (ship == null || bay == null) {
            feedback.setText("Please select both a ship and a bay.");
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("Validation failed");
            return;
        }
        if (ship.getShipSize().compareTo(bay.getBaySize()) > 0) {
            feedback.setText(String.format(
                    "Docking refused: a %s ship cannot use a %s bay. Select a bay with size at least %s.",
                    ship.getShipSize(), bay.getBaySize(), ship.getShipSize()));
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("Ship too large for selected bay");
            return;
        }
        try {
            AppContext.getHub().assignShipToBay(ship.getId(), bay.getBayNumber());
            AppContext.setStatus("Docked " + ship.getName() + " → " + bay.getName());
            mainWindow.refreshAll();
        } catch (Exception ex) {
            feedback.setText("✗  " + ex.getMessage());
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }

    private void updateDockSizeHint() {
        SpaceShip s = shipPicker.getValue();
        DockingBay b = bayPicker.getValue();
        if (s == null || b == null) {
            dockSizeHint.setText("");
            dockBtn.setDisable(shipPicker.getItems().isEmpty() || bayPicker.getItems().isEmpty());
            return;
        }
        if (s.getShipSize().compareTo(b.getBaySize()) > 0) {
            dockSizeHint.setText(String.format(
                    "Cannot dock: ship «%s» is size %s but bay «%s» is only size %s. Choose a bay of size %s or larger.",
                    s.getName(), s.getShipSize(), b.getName(), b.getBaySize(), s.getShipSize()));
            dockSizeHint.getStyleClass().setAll("feedback-error");
            dockBtn.setDisable(true);
        } else {
            dockSizeHint.setText(String.format("Ship size %s fits this %s bay — ready to dock.",
                    s.getShipSize(), b.getBaySize()));
            dockSizeHint.getStyleClass().setAll("secondary-text");
            dockBtn.setDisable(false);
        }
    }

    private static StringConverter<SpaceShip> shipConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SpaceShip s) {
                return s == null ? "" : s.getName() + "  [" + s.getClass().getSimpleName() + " / " + s.getShipSize() + "]";
            }
            @Override
            public SpaceShip fromString(String s) { return null; }
        };
    }

    private static StringConverter<DockingBay> bayConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(DockingBay b) {
                return b == null ? "" : "Bay " + b.getBayNumber() + " — " + b.getName() + "  [" + b.getBaySize() + "]";
            }
            @Override
            public DockingBay fromString(String s) { return null; }
        };
    }
}
