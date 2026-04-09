package com.octavian.galactic.model.cargo;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

//TODO: instantiate class
@Entity
@Table(name = "agricultural_cargo")
@DiscriminatorValue("AGRICULTURAL")
public class AgriculturalCargo extends CargoItem{
    @Column(name = "requires_refrigeration", nullable = false)
    private boolean requiresRefrigeration;

    @Column(name = "until_spoilage",  nullable = false)
    private int daysUntilSpoilage;

    protected AgriculturalCargo() {}

    public AgriculturalCargo(String name, double weight, int daysUntilSpoilage, boolean requiresRefrigeration) {
        super(name, weight);
        validateSpoilage(daysUntilSpoilage);
        this.daysUntilSpoilage = daysUntilSpoilage;
        this.requiresRefrigeration = requiresRefrigeration;
    }

    public AgriculturalCargo(String name, double weight, String description, int daysUntilSpoilage, boolean requiresRefrigeration) {
        super(name, weight, description);
        validateSpoilage(daysUntilSpoilage);
        this.daysUntilSpoilage = daysUntilSpoilage;
        this.requiresRefrigeration = requiresRefrigeration;
    }

    public boolean willSpoilInTransit(int transitDays) {
        if (transitDays < 0) throw new IllegalArgumentException("Transit days cannot be negative");
        return transitDays >= daysUntilSpoilage;
    }

    public boolean isPerishable() {
        return daysUntilSpoilage <= 7;
    }

    public String getStorageAdvisory() {
        if (requiresRefrigeration && isPerishable()) {
            return "URGENT: Refrigerated bay required. Deliver within " + daysUntilSpoilage + " days.";
        } else if (requiresRefrigeration) {
            return "Refrigerated storage required. Spoils in " + daysUntilSpoilage + " days.";
        } else if (isPerishable()) {
            return "Perishable — prioritise delivery. Spoils in " + daysUntilSpoilage + " days.";
        }
        return "Standard storage. Spoils in " + daysUntilSpoilage + " days.";
    }

    @Override
    public void printItemInfo() {
        super.printItemInfo();
        System.out.printf("----> Spoils in: %d days | Refrigeration required: %s%n",
                daysUntilSpoilage, requiresRefrigeration ? "YES" : "NO");
        System.out.printf("----> %s%n", getStorageAdvisory());
    }

    private static void validateSpoilage(int days) {
        if (days < 0)
            throw new IllegalArgumentException("Days until spoilage cannot be negative");
    }
}
