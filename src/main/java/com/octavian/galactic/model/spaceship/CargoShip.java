package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.exception.CargoCapacityExceededException;
import com.octavian.galactic.exception.InsufficientContainmentException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Heavy hauler space vehicle
public class CargoShip extends SpaceShip {
    private final double maxCargoWeight;
    private final Map<CargoItem, Integer> cargoManifest = new LinkedHashMap<>();


    public CargoShip(String name, Size size, int fuelLevel, int hullIntegrity, int maxCrewCapacity, double maxCargoCapacity) {
        super(name, fuelLevel, hullIntegrity, maxCrewCapacity, size);
        if (maxCargoCapacity < 0) {
            throw new IllegalArgumentException("Max cargo weight cannot be less than 0!");
        }
        this.maxCargoWeight = maxCargoCapacity;
    }

    public void addCargoItem(CargoItem item, int quantity) {
        if (item == null || quantity <= 0) {
            System.out.println("[LOGISTICS] Cannot insert a null item or with a quantity of 0!");
            return;
        }
        double currentWeight = cargoManifest
                .entrySet()
                .stream()
                .mapToDouble(e -> e.getKey().getWeight() * e.getValue())
                .sum();

        if (currentWeight + item.getWeight() * quantity > maxCargoWeight) {
            double overBy = (currentWeight + item.getWeight() * quantity) - maxCargoWeight;
            throw new CargoCapacityExceededException(this.getName(), item.getName(), quantity, overBy);
        }
        if(item instanceof HazardousCargo){
            System.out.println(((HazardousCargo) item).getHandlingWarning() + "-->" + item.getName());

            if(!((HazardousCargo) item).isContainmentAdequate()){
                throw new InsufficientContainmentException(item.getName(), ((HazardousCargo) item).getContainmentType());
            }
        }
        cargoManifest.put(item, cargoManifest.getOrDefault(item, 0) + quantity);
        System.out.printf("[LOGISTICS] Loaded %s with [%d] x '%s' %n", this.getName(), quantity, item.getName());
    }

    public Map<CargoItem, Integer> getCargoManifest(){
        return Collections.unmodifiableMap(cargoManifest);
    }

    public void printCargoManifest() {
        System.out.printf("[INSPECTION] Here is the cargo manifest for the ship '%s' %n", this.getName());
        System.out.println("[LOGISTICS] Cargo contents: " + cargoManifest);
    }
}
