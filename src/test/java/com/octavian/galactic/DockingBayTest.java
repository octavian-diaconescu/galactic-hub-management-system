package com.octavian.galactic;

import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.*;
import com.octavian.galactic.model.spaceship.CargoShip;
import com.octavian.galactic.model.station.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DockingBayTest {

    @Test
    @DisplayName("Should create a docking bay, and add a ship. The ship should have crew and cargo ")
    void addShipToDockingBaySuccess() {
        // Arrange
        DockingBay derelictFreighter = new DockingBay("Mariana Trench", Size.MEDIUM, false);
        CargoShip titanHauler = new CargoShip("Titan Hauler", Size.MEDIUM, 85, 90, 5, 100.88);

        List<CrewMember> crewManifest = List.of(
                new CrewMember("Michael Bay", CrewMember.Rank.COMMANDER),
                new CrewMember("Leonardo DiCaprio", CrewMember.Rank.GUEST),
                new CrewMember("Matthew McConaughey", CrewMember.Rank.ENGINEER)
        );
        Map<CargoItem, Integer> cargoManifest = new LinkedHashMap<>(Map.of(
                new HazardousCargo("Antimatter jelly", 5, 0, "Regular", "A mysterious jelly extracted from unknown sources"), 1,
                new HazardousCargo("Schrodinger's matter", 0, 0, "Paradoxical", "Is it solid? Is it liquid? Is it gas?"), 3,
                new HazardousCargo("Killer bunnies", 3.5, 5, "Cryogenic"), 1
        ));

        // Act
        crewManifest.forEach(titanHauler::addCrewMember);
        cargoManifest.forEach(titanHauler::addCargoItem);

        //Assert
        assertEquals(3, titanHauler.getCrewMembers().size());
        assertEquals(3, titanHauler.getCargoManifest().size());
        assertTrue(titanHauler.getCrewMembers().containsAll(crewManifest));
        for (CargoItem ci : cargoManifest.keySet()) {
            assertTrue(titanHauler.getCargoManifest().containsKey(ci));
        }
        System.out.println(titanHauler.getCargoManifest());
        System.out.println(titanHauler.getCrewMembers());
    }
}
