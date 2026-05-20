package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.MainWindowAware;
import com.octavian.galactic.ui.RefreshablePanel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddDockingBayController implements RefreshablePanel, MainWindowAware {

    @FXML private TextField nameField;
    @FXML private ComboBox<Size> sizePicker;
    @FXML private Label feedback;

    private MainWindowController mainWindow;

    @Override
    public void setMainWindow(Object mainWindowController) {
        this.mainWindow = (MainWindowController) mainWindowController;
    }

    @FXML
    private void initialize() {
        sizePicker.setItems(FXCollections.observableArrayList(Size.values()));
        sizePicker.getSelectionModel().selectFirst();
    }

    @Override
    public void refresh() {
        AppContext.setStatus("Add docking bay");
    }

    @FXML
    private void onCreate() {
        String name = nameField.getText() == null ? "" : nameField.getText().strip();
        Size size = sizePicker.getValue();
        if (name.isEmpty()) {
            feedback.setText("Please enter a bay name.");
            feedback.getStyleClass().setAll("feedback-error");
            return;
        }
        if (size == null) {
            feedback.setText("Please select a bay size.");
            feedback.getStyleClass().setAll("feedback-error");
            return;
        }
        try {
            DockingBay bay = new DockingBay(name, size, false);
            AppContext.getHub().buildDockingBay(bay);
            feedback.setText("✓  Created bay «" + name + "» (" + size + "). It is now available for docking.");
            feedback.getStyleClass().setAll("feedback-success");
            AppContext.setStatus("New docking bay: " + name);
            nameField.clear();
            mainWindow.refreshAll();
        } catch (Exception ex) {
            feedback.setText("✗  " + ex.getMessage());
            feedback.getStyleClass().setAll("feedback-error");
            AppContext.setStatus("✗  " + ex.getMessage());
        }
    }
}
