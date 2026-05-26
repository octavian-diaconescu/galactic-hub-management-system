package com.octavian.galactic.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class UiComponents {

    private UiComponents() {}

    public static VBox statCard(String label, String value, String styleClass) {
        Label val = new Label(value);
        val.getStyleClass().addAll("stat-value", styleClass);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        VBox box = new VBox(2, val, lbl);
        box.getStyleClass().add("stat-card");
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setMinWidth(110);
        return box;
    }

    public static VBox emptyState(String message) {
        Label l = new Label(message);
        l.getStyleClass().add("empty-state");
        VBox box = new VBox(l);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        return box;
    }

    public static Label infoNote(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("info-note");
        l.setWrapText(true);
        l.setPadding(new Insets(8, 12, 8, 12));
        return l;
    }

    public static HBox formRow(String labelText, Node field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        lbl.setMinWidth(140);
        lbl.setMaxWidth(140);
        HBox row = new HBox(12, lbl, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }
}
