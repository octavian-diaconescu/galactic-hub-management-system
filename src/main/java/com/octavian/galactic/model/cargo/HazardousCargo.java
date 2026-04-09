package com.octavian.galactic.model.cargo;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hazardous_cargo")
@DiscriminatorValue("HAZARD")
public class HazardousCargo extends CargoItem {
    private static final int DANGEROUS_RADIATION_THRESHOLD = 7;

    @Column(name = "radiation_level", nullable = false)
    private int radiationLevel;

    @Column(name = "containment_type", nullable = false)
    private String containmentType; // e.g: Lead-lined, Cryogenic. Can be anything the user decides

    protected HazardousCargo(){}

    public HazardousCargo(String name, double weight, int radiationLevel, String containmentType) {
        super(name, weight);
        setRadiationLevel(radiationLevel);
        this.containmentType = containmentType;
    }

    public HazardousCargo(String name, double weight, int radiationLevel, String containmentType, String description) {
        super(name, weight, description);
        setRadiationLevel(radiationLevel);
        this.containmentType = containmentType;

    }

    public String getContainmentType() {
        return containmentType;
    }

    public void setRadiationLevel(int radiationLevel) {
        if (radiationLevel < 0) {
            throw new IllegalArgumentException("Radiation level cannot be less than 0");
        }
        this.radiationLevel = radiationLevel;
    }

    @Override
    public void printItemInfo() {
        super.printItemInfo();
        System.out.printf("----> Radiation Level: %d/10 | Containment: %s%n", radiationLevel, containmentType);
        System.out.printf("----> %s%n", getHandlingWarning());
    }

    @Override
    public String toString() {
        return super.toString() +
                "(radLvl: " + radiationLevel + ") ";
    }

    public boolean isDangerous() {
        return radiationLevel >= DANGEROUS_RADIATION_THRESHOLD;
    }

    public boolean isContainmentAdequate() {
        return !isDangerous() || containmentType.equalsIgnoreCase("Lead-lined");
    }

    public String getHandlingWarning() {
        if (isDangerous() && !isContainmentAdequate()) {
            return "CRITICAL: High radiation with inadequate containment — DO NOT LOAD";
        } else if (isDangerous()) {
            return "WARNING: High radiation cargo — authorised personnel only";
        } else {
            return "LOW RISK: Standard hazardous handling protocols apply";
        }
    }

}
