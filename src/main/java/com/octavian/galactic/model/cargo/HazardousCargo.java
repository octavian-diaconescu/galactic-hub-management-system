package com.octavian.galactic.model.cargo;

public class HazardousCargo extends CargoItem{
    private int radiationLevel;
    private String containmentType; // e.g: Lead-lined, Cryogenic

    public HazardousCargo(String name, double weight, int radiationLevel, String containmentType){
        super(name, weight);
        setRadiationLevel(radiationLevel);
        this.containmentType = containmentType;
    }

    public  HazardousCargo(String name, double weight, int radiationLevel, String containmentType, String description){
        super(name, weight, description);
        setRadiationLevel(radiationLevel);
        this.containmentType = containmentType;

    }

    void setRadiationLevel(int radiationLevel){
        if(radiationLevel < 0){
            throw new IllegalArgumentException("Radiation level cannot be less than 0");
        }
        this.radiationLevel = radiationLevel;
    }

}
