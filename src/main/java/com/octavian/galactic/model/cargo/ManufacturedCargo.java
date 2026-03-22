package com.octavian.galactic.model.cargo;

public class ManufacturedCargo extends CargoItem{
    private final int fragilityIndex; // 1 through 10
    public enum FragilityTier {
        ROBUST,     // 1-3: No special handling
        DELICATE,   // 4-6: Handle with care
        CRITICAL    // 7-10: Shock-proof packaging and upright storage mandatory
    }

    public ManufacturedCargo(String name, double weight, int fragilityIndex) {
        super(name, weight);
        validateFragilityIndex(fragilityIndex);
        this.fragilityIndex = fragilityIndex;
    }

    public ManufacturedCargo(String name, double weight, String description, int fragilityIndex) {
        super(name, weight, description);
        validateFragilityIndex(fragilityIndex);
        this.fragilityIndex = fragilityIndex;
    }

    public FragilityTier getFragilityTier() {
        if (fragilityIndex <= 3) return FragilityTier.ROBUST;
        if (fragilityIndex <= 6) return FragilityTier.DELICATE;
        return FragilityTier.CRITICAL;
    }

    public String getHandlingInstructions() {
        return switch (getFragilityTier()) {
            case ROBUST   -> "No special requirements.";
            case DELICATE -> "Handle with care. Avoid stacking.";
            case CRITICAL -> "FRAGILE: Shock-proof packaging required. Store upright.";
        };
    }

    public boolean requiresPadding() {
        return fragilityIndex >= 5;
    }

    @Override
    public void printItemInfo() {
        super.printItemInfo();
        System.out.printf("----> Fragility: %d/10 | Tier: %s%n", fragilityIndex, getFragilityTier());
        System.out.printf("----> %s%n", getHandlingInstructions());
    }

    private static void validateFragilityIndex(int fragilityIndex){
        if(fragilityIndex < 0 || fragilityIndex > 10){
            throw new IllegalArgumentException("Fragility index must be between 1 and 10");
        }
    }
}
