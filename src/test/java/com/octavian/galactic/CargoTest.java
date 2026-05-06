package com.octavian.galactic;

import com.octavian.galactic.exception.CargoCapacityExceededException;
import com.octavian.galactic.exception.InsufficientContainmentException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.HazardousCargo;
import com.octavian.galactic.model.spaceship.CargoShip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CargoTest {

    @Test
    @DisplayName("Should add two cargo items to a cargo ship")
    void testAddCargoToCargoShip() {
        // Arrange
        CargoShip cargoShip = new CargoShip.Builder("Alpha 1", Size.SMALL)
                .fuelLevel(88)
                .hullIntegrity(100)
                .maxCrewCapacity(20)
                .maxCargoWeight(25.75)
                .build();

        HazardousCargo hc = new HazardousCargo("Radioactive milk", 2.5, 8, "Lead-lined");
        HazardousCargo hazardousCargoWithDescription = new HazardousCargo("Antimatter jelly", 5, 0, "Regular",
                "A mysterious jelly extracted from unknown sources");
        HazardousCargo hazardousCargoWithDescription2 = new HazardousCargo("Schrodinger's matter", 0, 0, "Paradoxical",
                "Is it solid? Is it liquid? Is it gas?");

        // Act
        cargoShip.setFuelLevel(100);
        cargoShip.addCargoItem(hc, 2);
        hc = new HazardousCargo("Alien slurpie", 10, 10, "Lead-lined");
        cargoShip.addCargoItem(hc, 1);
        cargoShip.addCargoItem(hazardousCargoWithDescription, 1);
        cargoShip.addCargoItem(hazardousCargoWithDescription2, 1);

//        hazardousCargoWithDescription.printItemInfo();
//        hazardousCargoWithDescription2.printItemInfo();
//        hc.printItemInfo();
        cargoShip.printCargoManifest();

        // Assert
        assertEquals(4, cargoShip.getCargoManifest().size());
    }

    @Test
    @DisplayName("Should throw CargoCapacityExceededException when adding cargo over maxCargoWeight")
    void testAddCargoOverWeightLimit() {
        // Arrange
        CargoShip ship = new CargoShip.Builder("HeavyHauler", Size.SMALL)
                .fuelLevel(12)
                .hullIntegrity(100)
                .maxCrewCapacity(5)
                .maxCargoWeight(2.5)
                .build();

        HazardousCargo hc = new HazardousCargo("Lead balls", 1, 0, "Regular");

        // Act
        ship.addCargoItem(hc, 1); // 1 tonne — within limit

        // Assert: adding 7 tonnes (3.5 * 2) to a ship with 1.5 tonnes remaining must throw
        assertThrows(CargoCapacityExceededException.class,
                () -> ship.addCargoItem(new HazardousCargo("Killer bunnies", 3.5, 5, "Cryogenic"), 2),
                "Should throw CargoCapacityExceededException when load exceeds max cargo weight");

        // Original item should still be present; the failed load must not have been added
        assertTrue(ship.getCargoManifest().containsKey(hc));
        assertEquals(1, ship.getCargoManifest().size());
    }

    @Test
    @DisplayName("Should throw InsufficientContainmentException for high-radiation cargo with inadequate containment")
    void testAddHazardousCargoWithBadContainment() {
        // Arrange
        CargoShip ship = new CargoShip.Builder("Transport", Size.MEDIUM)
                .fuelLevel(100)
                .hullIntegrity(100)
                .maxCrewCapacity(5)
                .maxCargoWeight(500.0)
                .build();
        // Radiation level 8 (>= 7 threshold) with non-Lead-lined containment → rejected
        HazardousCargo dangerousCargo = new HazardousCargo("Unstable isotope", 10, 8, "Plastic wrap");

        // Assert
        assertThrows(InsufficientContainmentException.class,
                () -> ship.addCargoItem(dangerousCargo, 1),
                "High-radiation cargo with inadequate containment must throw InsufficientContainmentException");

        assertEquals(0, ship.getCargoManifest().size(), "Rejected cargo must not appear in the manifest");
    }
}