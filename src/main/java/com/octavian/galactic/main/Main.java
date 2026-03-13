package com.octavian.galactic.main;

import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.station.CrewMember;
import com.octavian.galactic.model.cargo.HazardousCargo;

public class Main {
    static void main() {
        CargoShip cargoShip = new CargoShip("Alpha 1", 88, 100, 20, 25.75);
        System.out.println(cargoShip);
        CrewMember crm = new CrewMember("Bob");
        cargoShip.addCrewMember(crm);
        cargoShip.setFuelLevel(100);
        System.out.printf("Ship [%s] has been refueled%n", cargoShip.getName());

        HazardousCargo hc = new HazardousCargo("Radioactive milk", 2.5);
        cargoShip.addCargoItem(hc, 2);
        hc = new HazardousCargo("Alien slurpie", 10);
        cargoShip.addCargoItem(hc, 1);

        System.out.printf("[INSPECTION] Here is the cargo manifest for the ship '%s' %n", cargoShip.getName());
        cargoShip.printCargoManifest();
    }
}
