package com.octavian.galactic.ui;

import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.repository.CargoRepository;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.FuelDepotRepository;
import com.octavian.galactic.repository.ShipRepository;
import com.octavian.galactic.service.HubService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class GalacticApp extends Application {

    private EntityManagerFactory emf;
    private Throwable initError;

    @Override
    public void init() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .filename(".env.persistence")
                    .load();

            Map<String, Object> overrides = new HashMap<>();
            overrides.put("jakarta.persistence.jdbc.user", required(dotenv, "GALACTIC_DB_USER"));
            overrides.put("jakarta.persistence.jdbc.password", required(dotenv, "GALACTIC_DB_PASSWORD"));

            emf = Persistence.createEntityManagerFactory("com.octavian.galactic", overrides);

            DockingBayRepository dockingRepo = new DockingBayRepository(emf);
            ShipRepository shipRepo = new ShipRepository(emf);
            CargoRepository cargoRepo = new CargoRepository(emf);
            FuelDepotRepository fuelDepotRepo = new FuelDepotRepository(emf);

            FuelDepot fuelDepot = fuelDepotRepo.findAll().stream().findFirst()
                    .orElseGet(() -> {
                        FuelDepot seeded = new FuelDepot("Omega F-Depot", 10000, 8000);
                        fuelDepotRepo.save(seeded);
                        return seeded;
                    });

            HubService hub = new HubService("Omega Station", fuelDepot, shipRepo, dockingRepo, fuelDepotRepo);
            AppContext.init(hub, shipRepo, dockingRepo, cargoRepo, fuelDepotRepo, fuelDepot);

        } catch (Throwable t) {
            initError = t;
        }
    }

    @Override
    public void start(Stage stage) {
        if (initError != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Failed to connect to the database");
            alert.setContentText(initError.getMessage() + "\n\nMake sure Docker is running and .env.persistence is configured.");
            alert.showAndWait();
            Platform.exit();
            return;
        }
        new MainWindow(stage).show();
    }

    @Override
    public void stop() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private static String required(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing or empty .env.persistence entry: " + key);
        }
        return value;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
