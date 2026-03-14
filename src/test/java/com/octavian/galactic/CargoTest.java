package com.octavian.galactic;

import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CargoTest {

    @Test
    @DisplayName("Should add two cargo to a cargo ship")
    void testAddCargoToCargoShip(){
        // Arrange
        CargoShip cargoShip = new CargoShip("Alpha 1", 88, 100, 20, 25.75);
        HazardousCargo hc = new HazardousCargo("Radioactive milk", 2.5);

        System.out.println(cargoShip);

        // Act
        cargoShip.setFuelLevel(100);

        System.out.printf("Ship [%s] has been refueled%n", cargoShip.getName());

        cargoShip.addCargoItem(hc, 2);
        hc = new HazardousCargo("Alien slurpie", 10);
        cargoShip.addCargoItem(hc, 1);

        System.out.printf("[INSPECTION] Here is the cargo manifest for the ship '%s' %n", cargoShip.getName());
        cargoShip.printCargoManifest();
    }
    @Test
    @DisplayName("Shouldn't add cargo over maxCargoCapacity")
    void testFailAddCargoToCargoShip(){
        // Arrange
        CargoShip ship = new CargoShip("HeavyHauler", 12, 100, 5, 2.5);
        HazardousCargo hc = new HazardousCargo("Lead balls" , 1);

        // Act
        ship.addCargoItem(hc, 1);
        ship.addCargoItem(new HazardousCargo("Killer bunnies", 3.5), 2);

        // Assert
        assertTrue(ship.getCargoManifest().containsKey(hc));
        assertEquals(1, ship.getCargoManifest().size());
    }
}
