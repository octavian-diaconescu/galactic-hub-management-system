package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.exception.CargoCapacityExceededException;
import com.octavian.galactic.exception.InsufficientContainmentException;
import com.octavian.galactic.model.Size;
import com.octavian.galactic.model.cargo.CargoItem;
import com.octavian.galactic.model.cargo.HazardousCargo;
import jakarta.persistence.*;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "cargo_ship")
@DiscriminatorValue("CARGO")
// Heavy hauler space vehicle
public class CargoShip extends SpaceShip {
    @Column(name = "max_cargo_weight", nullable = false)
    private double maxCargoWeight;

    @OneToMany(mappedBy = "cargoShip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargoManifestEntry> cargoManifestLine = new ArrayList<>();

    protected CargoShip() {}

    private CargoShip(Builder builder) {
        super(builder);
        if (builder.maxCargoWeight < 0) {
            throw new IllegalArgumentException("Max cargo weight cannot be less than 0!");
        }
        this.maxCargoWeight = builder.maxCargoWeight;
    }

    public static class Builder extends AbstractBuilder<Builder>{
        private double maxCargoWeight = 250.0;

        public Builder(String name, Size size){
            super(name, size);
        }

        public Builder maxCargoWeight(double maxCargoWeight){
            this.maxCargoWeight = maxCargoWeight;
            return this;
        }

        @Override
        public Builder self(){
            return this;
        }

        @Override
        public CargoShip build() {
            return new CargoShip(this);
        }


    }
    public void addCargoItem(CargoItem item, int quantity) {
        if (item == null || quantity <= 0) {
            System.out.println("[LOGISTICS] Cannot insert a null item or with a quantity of 0!");
            return;
        }
        double currentWeight = cargoManifestLine.stream()
                .mapToDouble(e -> e.getCargoItem().getWeight() * e.getQuantity())
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
        cargoManifestLine.stream()
                .filter(e -> e.getCargoItem().equals(item))
                .findFirst()
                .ifPresentOrElse(
                        e -> e.addQuantity(quantity),
                        () -> cargoManifestLine.add(new CargoManifestEntry(this, item, quantity)));
        System.out.printf("[LOGISTICS] Loaded %s with [%d] x '%s' %n", this.getName(), quantity, item.getName());
    }

    public Map<CargoItem, Integer> getCargoManifest(){
        Map<CargoItem, Integer> temporaryMap;

        temporaryMap = cargoManifestLine.stream()
                .collect(Collectors.toMap(
                        CargoManifestEntry::getCargoItem,
                        CargoManifestEntry::getQuantity,
                        Integer::sum,
                        LinkedHashMap::new
                ));

        return Collections.unmodifiableMap(new LinkedHashMap<>(temporaryMap));
    }

    public void printCargoManifest() {
        System.out.printf("[INSPECTION] Here is the cargo manifest for the ship '%s' %n", this.getName());
        System.out.println("[LOGISTICS] Cargo contents: " + getCargoManifest());
    }
}
