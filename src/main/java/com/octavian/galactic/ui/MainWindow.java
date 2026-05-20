package com.octavian.galactic.ui;

import com.octavian.galactic.ui.controller.MainWindowController;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainWindow {

    private final Stage stage;
    private final MainWindowController controller;

    public MainWindow(Stage stage) {
        this.stage = stage;
        ViewLoader.LoadedView<MainWindowController> loaded = ViewLoader.load("main-window.fxml");
        this.controller = loaded.controller();
    }

    public void show() {
        Scene scene = new Scene(controller.getRoot(), 1280, 820);
        stage.setScene(scene);
        stage.setTitle("Omega Station — Galactic Hub Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
        controller.showInitialContent();
    }
}
