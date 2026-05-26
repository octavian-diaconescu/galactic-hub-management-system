package com.octavian.galactic.ui;

import com.octavian.galactic.model.station.FuelDepot;
import com.octavian.galactic.repository.CargoRepository;
import com.octavian.galactic.repository.DockingBayRepository;
import com.octavian.galactic.repository.FuelDepotRepository;
import com.octavian.galactic.repository.ShipRepository;
import com.octavian.galactic.service.HubService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class AppContext {

    private static HubService hub;
    private static ShipRepository shipRepo;
    private static DockingBayRepository dockingRepo;
    private static CargoRepository cargoRepo;
    private static FuelDepotRepository fuelDepotRepo;
    private static FuelDepot fuelDepot;

    public static final StringProperty statusProperty = new SimpleStringProperty("Ready");

    private AppContext() {}

    public static void init(HubService hub, ShipRepository shipRepo, DockingBayRepository dockingRepo,
                            CargoRepository cargoRepo, FuelDepotRepository fuelDepotRepo, FuelDepot fuelDepot) {
        AppContext.hub = hub;
        AppContext.shipRepo = shipRepo;
        AppContext.dockingRepo = dockingRepo;
        AppContext.cargoRepo = cargoRepo;
        AppContext.fuelDepotRepo = fuelDepotRepo;
        AppContext.fuelDepot = fuelDepot;
    }

    public static HubService getHub()                     { return hub; }
    public static ShipRepository getShipRepo()            { return shipRepo; }
    public static DockingBayRepository getDockingRepo()   { return dockingRepo; }
    public static CargoRepository getCargoRepo()          { return cargoRepo; }
    public static FuelDepotRepository getFuelDepotRepo()  { return fuelDepotRepo; }
    public static FuelDepot getFuelDepot()                { return fuelDepot; }
    public static void setFuelDepot(FuelDepot fd)         { fuelDepot = fd; }

    public static void setStatus(String message) {
        statusProperty.set(message);
    }
}
