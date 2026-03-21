package com.octavian.galactic.model.cargo;

public class AgriculturalCargo extends CargoItem{
    private final boolean requiresRefrigeration;
    private final int daysUntilSpoilage;

    public AgriculturalCargo(String name, double weight, int daysUntilSpoilage, boolean requiresRefrigeration) {
        super(name, weight);
        this.daysUntilSpoilage = daysUntilSpoilage;
        this.requiresRefrigeration = requiresRefrigeration;
    }

    public AgriculturalCargo(String name, double weight, String description, int daysUntilSpoilage, boolean requiresRefrigeration) {
        super(name, weight, description);
        this.daysUntilSpoilage = daysUntilSpoilage;
        this.requiresRefrigeration = requiresRefrigeration;
    }
}
