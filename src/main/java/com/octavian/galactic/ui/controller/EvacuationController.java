package com.octavian.galactic.ui.controller;

import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class EvacuationController implements RefreshablePanel, MainWindowAware {

    @FXML private Label feedback;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @Override
    public void refresh() {
        feedback.setText("");
        feedback.getStyleClass().setAll("feedback-idle");
        AppContext.setStatus("Emergency evacuation panel ready");
    }

    @FXML
    private void onConfirm() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Emergency Evacuation");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will undock ALL ships immediately. Proceed?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    AppContext.getHub().emergencyEvacuation();
                    feedback.setText("✓  Emergency evacuation complete. All ships undocked.");
                    feedback.getStyleClass().setAll("feedback-success");
                    AppContext.setStatus("Emergency evacuation triggered");
                    mainWindow.refreshBayTable();
                } catch (Exception ex) {
                    feedback.setText("✗  " + ex.getMessage());
                    feedback.getStyleClass().setAll("feedback-error");
                    AppContext.setStatus("✗  " + ex.getMessage());
                }
            }
        });
    }
}
