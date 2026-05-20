package com.octavian.galactic.ui.controller;

import com.octavian.galactic.model.SpaceEntity;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.spaceship.SpaceShip;
import com.octavian.galactic.model.station.DockingBay;
import com.octavian.galactic.ui.AppContext;
import com.octavian.galactic.ui.RefreshablePanel;
import com.octavian.galactic.ui.UiComponents;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;

public class DockedShipsController implements RefreshablePanel {

    @FXML private VBox contentBox;

    @Override
    public void refresh() {
        List<DockingBay> occupied = AppContext.getHub().getBaysByStatus(true);
        contentBox.getChildren().clear();

        if (occupied.isEmpty()) {
            contentBox.getChildren().add(UiComponents.emptyState("No ships are currently docked."));
            AppContext.setStatus("No docked ships");
            return;
        }

        List<SpaceShip> ships = occupied.stream().map(DockingBay::getSpaceShip).toList();
        TableView<SpaceShip> table = new TableView<>(FXCollections.observableArrayList(ships));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("data-table");
        table.setPrefHeight(350);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        table.getColumns().addAll(
                col("Name", SpaceEntity::getName, 130),
                col("Type", s -> s.getClass().getSimpleName(), 90),
                col("Size", s -> s.getShipSize().toString(), 70),
                col("Fuel", s -> s.getFuelLevel() + "%", 60),
                col("Hull", s -> s.getHullIntegrity() + "%", 60),
                col("Crew", s -> String.valueOf(s.getCrewMembers().size()), 50),
                col("Cargo", s -> {
                    if (s instanceof CargoShip cs) {
                        SpaceShip loaded = AppContext.getShipRepo().findByIdWithCargo(cs.getId()).orElse(cs);
                        if (loaded instanceof CargoShip csLoaded) {
                            double w = csLoaded.getCargoManifest().entrySet().stream()
                                    .mapToDouble(e -> e.getKey().getWeight() * e.getValue()).sum();
                            return String.format("%.1f Tonnes", w);
                        }
                    }
                    return "N/A";
                }, 80)
        );

        contentBox.getChildren().add(table);
        AppContext.setStatus("Showing " + ships.size() + " docked ship(s)");
    }

    private static TableColumn<SpaceShip, String> col(String title,
                                                      java.util.function.Function<SpaceShip, String> extractor,
                                                      double pref) {
        TableColumn<SpaceShip, String> c = new TableColumn<>(title);
        c.setCellValueFactory(data -> new ReadOnlyStringWrapper(extractor.apply(data.getValue())));
        c.setPrefWidth(pref);
        return c;
    }
}
