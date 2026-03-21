package com.octavian.galactic.model.cargo;

public class ManufacturedCargo extends CargoItem{
    private final int fragilityIndex; // 1 through 10

    public ManufacturedCargo(String name, double weight, int fragilityIndex) {
        super(name, weight);
        if(fragilityIndex < 0 || fragilityIndex > 10){
            throw new IllegalArgumentException("Fragility index must be between 1 and 10");
        }
        this.fragilityIndex = fragilityIndex;
    }

    public ManufacturedCargo(String name, double weight, String description, int fragilityIndex) {
        super(name, weight, description);
        if(fragilityIndex < 0 || fragilityIndex > 10){
            throw new IllegalArgumentException("Fragility index must be between 1 and 10");
        }
        this.fragilityIndex = fragilityIndex;
    }
}
