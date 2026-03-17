package com.octavian.galactic;

import com.octavian.galactic.model.Size;
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
        CargoShip cargoShip = new CargoShip("Alpha 1", Size.SMALL, 88, 100, 20, 25.75);
        HazardousCargo hc = new HazardousCargo("Radioactive milk", 2.5, 8, "Lead-lined");
        HazardousCargo hazardousCargoWithDescription = new HazardousCargo("Antimatter jelly", 5, 0, "Regular", "A mysterious jelly extracted from unknown sources");
        HazardousCargo hazardousCargoWithDescription2 = new HazardousCargo("Schrodinger's matter", 0, 0, "Paradoxical", "Is it solid? Is it liquid? Is it gas?");

        System.out.println(cargoShip);

        // Act
        cargoShip.setFuelLevel(100);

        System.out.printf("Ship [%s] has been refueled%n", cargoShip.getName());

        cargoShip.addCargoItem(hc, 2);
        hc = new HazardousCargo("Alien slurpie", 10, 10, "Lead-lined");
        cargoShip.addCargoItem(hc, 1);
        cargoShip.addCargoItem(hazardousCargoWithDescription, 1);
        cargoShip.addCargoItem(hazardousCargoWithDescription2, 1);

        hazardousCargoWithDescription.printItemInfo();
        hazardousCargoWithDescription2.printItemInfo();

        System.out.printf("[INSPECTION] Here is the cargo manifest for the ship '%s' %n", cargoShip.getName());
        cargoShip.printCargoManifest();
    }
    @Test
    @DisplayName("Shouldn't add cargo over maxCargoCapacity")
    void testFailAddCargoToCargoShip(){
        // Arrange
        CargoShip ship = new CargoShip("HeavyHauler", Size.SMALL, 12, 100, 5, 2.5);
        HazardousCargo hc = new HazardousCargo("Lead balls" , 1, 0, "Regular");

        // Act
        ship.addCargoItem(hc, 1);
        ship.addCargoItem(new HazardousCargo("Killer bunnies", 3.5, 5, "Cryogenic"), 2);

        // Assert
        assertTrue(ship.getCargoManifest().containsKey(hc));
        assertEquals(1, ship.getCargoManifest().size());
    }
}
