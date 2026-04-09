package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.model.Size;
import jakarta.persistence.*;

//TODO: implement game logic
// I'm getting closer and closer to turning this into a game, which is beyond the scope of the project's requirements :)

@Entity
@Table(name = "fighter_ship")
@DiscriminatorValue("FIGHTER")
public class FighterShip extends SpaceShip {
    public enum WeaponClass {
        LASER,   // Fast, energy-based
        TORPEDO, // Slow, high-damage
        RAILGUN  // Armor-piercing
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_weapon", nullable = false)
    private WeaponClass primaryWeapon;

    @Column(name = "shield_strength", nullable = false)
    private int shieldStrength;         // 0 to 100

    @Column(name = "ammunition_count", nullable = false)
    private int ammunitionCount;

   @Transient
    private boolean weaponsArmed;

   protected FighterShip(){}

    private FighterShip(Builder builder) {
        super(builder);
        this.primaryWeapon = builder.primaryWeapon;
        setShieldStrength(builder.shieldStrength);
        setAmmunitionCount(builder.ammunitionCount);
        this.weaponsArmed = builder.weaponsArmed;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private WeaponClass primaryWeapon = WeaponClass.LASER;
        private int shieldStrength = 100;
        private int ammunitionCount = 25;
        private boolean weaponsArmed = false;

        public Builder(String name, Size size) {
            super(name, size);
        }

        public Builder primaryWeapon(WeaponClass primaryWeapon){
            this.primaryWeapon = primaryWeapon;
            return this;
        }

        public Builder shieldStrength(int shieldStrength){
            this.shieldStrength = shieldStrength;
            return this;
        }

        public Builder ammunitionCount(int ammunitionCount){
            this.ammunitionCount = ammunitionCount;
            return this;
        }

        public Builder weaponsArmed(boolean weaponsArmed){
            this.weaponsArmed = weaponsArmed;
            return this;
        }

        @Override
        public Builder self() {
            return this;
        }

        @Override
        public FighterShip build() {
            return new FighterShip(this);
        }
    }
    public void armWeapons() {
        if (weaponsArmed) {
            System.out.printf("[COMBAT] '%s' weapons' are already armed.%n", this.getName());
            return;
        }
        System.out.printf("[COMBAT] '%s' weapons armed. Primary: %s%n", this.getName(), primaryWeapon);
        weaponsArmed = true;
    }

    public void unarmWeapons() {
        weaponsArmed = false;
        System.out.printf("[COMBAT] '%s' has been unarmed.%n", this.getName());
    }

    public void fire() {
        if (!weaponsArmed) {
            System.out.printf("[COMBAT] Cannot fire -- '%s' weapons are not armed.%n", this.getName());
            return;
        }
        if (fuelTankIsEmpty()) {
            System.out.printf("[COMBAT] '%s' has no fuel. Firing systems offline.%n", this.getName());
            return;
        }
        if (primaryWeapon != WeaponClass.LASER && ammunitionCount <= 0) {
            System.out.printf("[COMBAT] '%s' is out of ammunition.%n", this.getName());
            return;
        }

        if (primaryWeapon != WeaponClass.LASER) {
            ammunitionCount--;
        }
    }

    public void absorbDamage(int damageAmount) {
        if (damageAmount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }

        int damageToHull = Math.max(0, damageAmount - shieldStrength);
        shieldStrength = Math.max(0, shieldStrength - damageAmount);

        System.out.printf("[COMBAT] '%s' took %d damage. Shield: %d | Hull: %d%n",
                getName(), damageAmount, shieldStrength, getHullIntegrity());

        if (damageToHull > 0) {
            int newHull = Math.max(0, getHullIntegrity() - damageToHull);
            setHullIntegrity(newHull);
        }
    }

    // Combat readiness is a percentage returned as a double in the range 0.0 - 1.0
    public double getCombatReadiness() {
        double fuelFactor = (double) getFuelLevel() / 100;
        double hullFactor = (double) getHullIntegrity() / 100;
        double shieldFactor = (double) shieldStrength / 100;
        double ammoFactor = primaryWeapon == WeaponClass.LASER ? 1.0 : Math.min(1.0, (double) ammunitionCount / 20);

        return (fuelFactor + hullFactor + shieldFactor + ammoFactor) / 4;
    }

    public void printCombatStatus() {
        System.out.printf("[COMBAT-STATUS] Fighter '%s' | Weapon: %s | Shield: %d | Ammo: %s | Armed: %s | Readiness: %.0f%% %n",
                this.getName(), primaryWeapon, shieldStrength,
                primaryWeapon == WeaponClass.LASER ? "inf" : String.valueOf(ammunitionCount),
                weaponsArmed ? "YES" : "NO",
                getCombatReadiness() * 100);
    }

    public WeaponClass getPrimaryWeapon() {
        return primaryWeapon;
    }

    public boolean isWeaponsArmed() {
        return weaponsArmed;
    }

    public int getAmmunitionCount() {
        return ammunitionCount;
    }

    public int getShieldStrength() {
        return shieldStrength;
    }

    public void setShieldStrength(int shieldStrength) {
        if (shieldStrength < 0 || shieldStrength > 100)
            throw new IllegalArgumentException("Shield strength must be between 0 and 100");
        this.shieldStrength = shieldStrength;
    }

    public void setAmmunitionCount(int ammunitionCount) {
        if (ammunitionCount < 0)
            throw new IllegalArgumentException("Ammunition count cannot be negative");
        this.ammunitionCount = ammunitionCount;
    }

}
