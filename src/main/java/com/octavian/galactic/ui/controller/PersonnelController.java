package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.Set;

public class PersonnelController implements RefreshablePanel {

    @FXML private VBox contentBox;

    @Override
    public void refresh() {
        Set<CrewMember> personnel = AppContext.getHub().generatePersonnelReport();
        contentBox.getChildren().clear();

        if (personnel.isEmpty()) {
            contentBox.getChildren().add(UiComponents.emptyState("No crew members registered."));
            AppContext.setStatus("No personnel found");
            return;
        }

        TableView<CrewMember> table = new TableView<>(FXCollections.observableArrayList(personnel));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("data-table");
        table.setPrefHeight(380);

        table.getColumns().addAll(
                col("Name", SpaceEntity::getName, 160),
                col("Rank", c -> c.toString().replaceAll(".*\\((.*)\\).*", "$1"), 100),
                col("Species", c -> c.getSpecies().toString(), 100)
        );

        Label total = new Label("Total personnel: " + personnel.size());
        total.getStyleClass().addAll("info-label", "secondary-text");

        contentBox.getChildren().addAll(table, total);
        AppContext.setStatus("Personnel: " + personnel.size() + " crew members");
    }

    private static TableColumn<CrewMember, String> col(String title,
                                                         java.util.function.Function<CrewMember, String> extractor,
                                                         double pref) {
        TableColumn<CrewMember, String> c = new TableColumn<>(title);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(extractor.apply(data.getValue())));
        c.setPrefWidth(pref);
        return c;
    }
}
