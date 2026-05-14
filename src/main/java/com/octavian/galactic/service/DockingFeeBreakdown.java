package com.octavian.galactic.service;

/**
 * Line-item result of {@link HubService#billDockedShipWithBreakdown} for UI / reports.
 */
public record DockingFeeBreakdown(
        String shipName,
        String shipType,
        double baseFee,
        double serviceMultiplier,
        int fuelGapUnits,
        int repairGapUnits,
        double fuelUnitRate,
        double repairUnitRate,
        double fuelLineCredits,
        double repairLineCredits,
        double resourceCost,
        int fuelUnitsDispensed,
        int repairUnitsRestored,
        double totalCredits
) {
    public String formatInvoice() {
        String mult = serviceMultiplier == (long) serviceMultiplier
                ? String.valueOf((long) serviceMultiplier)
                : String.valueOf(serviceMultiplier);
        StringBuilder sb = new StringBuilder();
        sb.append("---- ").append(shipName).append(" (").append(shipType).append(") ----\n");
        sb.append(String.format("  Base fee:               %,.2f cr%n", baseFee));
        sb.append(String.format("  Service multiplier:     x%s%n", mult));
        sb.append(String.format("  Fuel gap (for pricing): %d units @ %.1f cr/unit → %,.2f cr%n",
                fuelGapUnits, fuelUnitRate, fuelLineCredits));
        sb.append(String.format("  Repair gap:             %d units @ %.1f cr/unit → %,.2f cr%n",
                repairGapUnits, repairUnitRate, repairLineCredits));
        sb.append(String.format("  Resource subtotal:      %,.2f cr%n", resourceCost));
        sb.append(String.format("  Formula:                %,.2f + (%,.2f × %s) = %,.2f cr%n",
                baseFee, resourceCost, mult, totalCredits));
        sb.append(String.format("  Fuel actually dispensed: %d units%n", fuelUnitsDispensed));
        sb.append("  Hull restored:           ").append(repairUnitsRestored).append(" points to 100%\n");
        sb.append(String.format("  ** Total charged:       %,.2f cr%n", totalCredits));
        return sb.toString();
    }
}
