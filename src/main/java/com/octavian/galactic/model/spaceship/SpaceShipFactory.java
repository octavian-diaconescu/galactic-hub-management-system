package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.model.Size;

import java.util.Random;

public class SpaceShipFactory {
    private static final Random random = new Random();
    private static final String[] SHIP_NAMES = {"Altair 8", "Betty Boop", "Moonrakers", "Orion III", "Pathfinder", "Daedalus", "Excalibur", "Excursion", "Lancet", "Messiah", "Odyssey", "Pleiades", "Atlantis II", "Aries", "Friede", "Mayflower One", "Aether", "Bebop", "Anastasia", "Bilkis", "Cepheus", "Europa One", "Excelsior", "Hermes", "Hunter IV", "Icarus I", "Icarus II", "Mars I", "Mars II", "Rocinante", "Alexander 78v", "Amaterasu", "Arcadia", "Argonaut", "Ark", "Aurora", "Athena", "Avalon", "Axalon", "Axiom", "Bellerophon", "Conquistador", "Copernicus", "Hydra", "Hypatia", "IMS Draconis", "IMS Malta", "IMS Thermopylae", "ISA Excalibur" }; // Names taken from Wikipedia

    private SpaceShipFactory() {}

    public static SpaceShip createRandomArrival(){
        String name = SHIP_NAMES[random.nextInt(SHIP_NAMES.length)];
        int type = random.nextInt(3);

       return switch (type){
           case 0 -> new CargoShip.Builder(name, randomSize())
                   .fuelLevel(random.nextInt(40) + 20)
                   .hullIntegrity(random.nextInt(50) + 30)
                   .maxCargoWeight(random.nextInt(9000) + 1000)
                   .build();
           case 1 -> new ScoutShip.Builder(name, Size.SMALL)
                   .fuelLevel(random.nextInt(30) + 10)
                   .hullIntegrity(random.nextInt(40) + 40)
                   .sensorRange(random.nextInt(400) + 100)
                   .stealthState(random.nextBoolean())
                   .build();
           case 2 -> new FighterShip.Builder(name, randomSize())
                   .fuelLevel(random.nextInt(50) + 10)
                   .hullIntegrity(random.nextInt(60) + 20)
                   .primaryWeapon(randomWeapon())
                   .ammunitionCount(random.nextInt(20) + 10)
                   .shieldStrength(random.nextInt(50) + 10)
                   .weaponsArmed(random.nextBoolean())
                   .build();
           default -> throw new IllegalStateException("Unexpected type: " + type);
       };
    }

    private static Size randomSize() {
        Size[] sizes = Size.values();
        return sizes[random.nextInt(sizes.length)];
    }

    private static FighterShip.WeaponClass randomWeapon(){
        FighterShip.WeaponClass[] weaponClasses = FighterShip.WeaponClass.values();
        return weaponClasses[random.nextInt(weaponClasses.length)];
    }
}
