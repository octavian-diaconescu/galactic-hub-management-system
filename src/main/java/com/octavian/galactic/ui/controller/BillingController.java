package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.service.DockingFeeBreakdown;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;

public class BillingController implements RefreshablePanel, MainWindowAware {

    @FXML private VBox warningsBox;
    @FXML private Button runBtn;
    @FXML private TextArea log;
    @FXML private Label totalLabel;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @Override
    public void refresh() {
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
        warningsBox.getChildren().clear();
        runBtn.setDisable(occupied.isEmpty());
        if (occupied.isEmpty()) {
            warningsBox.getChildren().add(UiComponents.infoNote("⚠  No ships are currently docked."));
        }
        AppContext.setStatus("Billing panel ready");
    }

    @FXML
    private void onRunBilling() {
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
            AppContext.setStatus(String.format("Billing complete — %.2f credits collected", total));
            mainWindow.refreshBayTable();
        } catch (Exception ex) {
            log.setText("✗  Error during billing: " + ex.getMessage());
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }
}
