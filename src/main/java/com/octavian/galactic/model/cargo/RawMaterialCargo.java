package com.octavian.galactic.model.cargo;

public class RawMaterialCargo extends CargoItem{
    private final StateOfMatter stateOfMatter;
    private final double purityPercentage;

    public enum StateOfMatter{
        SOLID,
        LIQUID,
        GAS,
        PLASMA
    }

    public RawMaterialCargo(String name, double weight, StateOfMatter SoM, double purityPercentage) {
        super(name, weight);
        this.stateOfMatter = SoM;
        if(purityPercentage > 100 || purityPercentage < 0){
            throw new IllegalArgumentException("Percentage cannot be less than 0 or greater than 100");
        }
        this.purityPercentage = purityPercentage;
    }

    public RawMaterialCargo(String name, double weight, String description, StateOfMatter stateOfMatter, double purityPercentage) {
        super(name, weight, description);
        this.stateOfMatter = stateOfMatter;
        if(purityPercentage > 100 || purityPercentage < 0){
            throw new IllegalArgumentException("Percentage cannot be less than 0 or greater than 100");
        }
        this.purityPercentage = purityPercentage;
    }

}
