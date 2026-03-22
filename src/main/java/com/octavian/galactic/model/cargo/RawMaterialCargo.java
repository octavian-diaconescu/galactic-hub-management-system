package com.octavian.galactic.model.cargo;

public class RawMaterialCargo extends CargoItem {
    private static final double HIGH_PURITY_THRESHOLD = 90.0;

    private final StateOfMatter stateOfMatter;
    private final double purityPercentage;

    public enum StateOfMatter {
        SOLID,
        LIQUID,
        GAS,
        PLASMA
    }

    public RawMaterialCargo(String name, double weight, StateOfMatter SoM, double purityPercentage) {
        super(name, weight);
        this.stateOfMatter = SoM;
        validatePurity(purityPercentage);
        this.purityPercentage = purityPercentage;
    }

    public RawMaterialCargo(String name, double weight, String description, StateOfMatter stateOfMatter, double purityPercentage) {
        super(name, weight, description);
        this.stateOfMatter = stateOfMatter;
        validatePurity(purityPercentage);
        this.purityPercentage = purityPercentage;
    }

    public StateOfMatter getStateOfMatter() {
        return stateOfMatter;
    }

    public double getPurityPercentage() {
        return purityPercentage;
    }

    public boolean isHighPurity() {
        return purityPercentage >= HIGH_PURITY_THRESHOLD;
    }

    public String getGrade() {
        if (purityPercentage >= 95.0) return "PREMIUM";
        if (purityPercentage >= HIGH_PURITY_THRESHOLD) return "HIGH";
        if (purityPercentage >= 70.0) return "STANDARD";
        return "LOW";
    }

    @Override
    public void printItemInfo() {
        super.printItemInfo();
        System.out.printf("----> State: %s | Purity: %.1f%% | Grade: %s%n",
                stateOfMatter, purityPercentage, getGrade());
    }

    private static void validatePurity(double purityPercentage) {
        if (purityPercentage > 100 || purityPercentage < 0) {
            throw new IllegalArgumentException("Percentage cannot be less than 0 or greater than 100");
        }
    }
}
