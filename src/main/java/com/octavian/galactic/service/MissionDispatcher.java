package com.octavian.galactic.service;

import com.octavian.galactic.model.mission.Mission;
import com.octavian.galactic.model.mission.MissionResult;
import com.octavian.galactic.model.mission.SpaceEvent;
import com.octavian.galactic.model.spaceship.*;

import java.util.Random;

public class MissionDispatcher {
    private static final Random random = new Random();

    // Event probability weights [CLEAR, PIRATE, NEBULA, DEBRIS, DERELICT]
    private static final int[] EVENT_WEIGHTS = {40, 20, 15, 15, 10};

    private MissionDispatcher() {
    }

    public static MissionResult dispatch(SpaceShip ship, Mission mission) {
        // Inside this validation function we also check if the ship has enough fuel to be dispatched
        // This accounts only for a one-way trip
        validateDispatch(ship, mission);

        int fuelCost = ship.calculateFuelCost(mission.distance());
        SpaceEvent event = rollEvent();

        MissionResult result = switch (mission.type()) {
            case PATROL -> handlePatrol(ship, mission, event, fuelCost);
            case EXPLORE -> handleExplore(ship, mission, event, fuelCost);
            case HAUL -> handleHaul(ship, mission, event, fuelCost);
        };

        applyResult(ship, result);
        result.printSummary();
        return result;
    }

    private static MissionResult handlePatrol(SpaceShip ship, Mission mission, SpaceEvent event, int fuelCost) {
        MissionResult.Builder result = new MissionResult.Builder(mission, event)
                .fuelConsumed(fuelCost);

        if (ship instanceof FighterShip fighter) {
            return switch (event) {
                case PIRATE_ATTACK -> {
                    fighter.armWeapons();
                    double readiness = fighter.getCombatReadiness();
                    fighter.fire();
                    boolean success = readiness >= 0.5;
                    int damageTaken = success ? 10 : 30;
                    fighter.absorbDamage(damageTaken);
                    yield result
                            .success(success)
                            .hullDamageTaken(damageTaken)
                            .creditsEarned(success ? mission.baseReward() * 1.5 : 0)
                            .narrative(success
                                    ? String.format("'%s' engaged and repelled pirates. Readiness: %.0f%%", ship.getName(), readiness * 100)
                                    : String.format("'%s' was overwhelmed. Readiness too low (%.0f%%)", ship.getName(), readiness * 100))
                            .build();
                }
                case DERELICT_SHIP -> result
                .success(true)
                .creditsEarned(mission.baseReward() * 0.5)
                .narrative("Derelict located during patrol. Salvage rights claimed.")
                .build();
                default -> result
                        .success(true)
                        .creditsEarned(mission.baseReward())
                        .narrative(String.format("'%s' completed patrol. Event: %s", ship.getName(), event))
                        .build();
            };
        }

        // Non-fighter on a patrol mission ==> penalized
        return result
                .success(false)
                .hullDamageTaken(event == SpaceEvent.PIRATE_ATTACK ? 25 : 5)
                .creditsEarned(0)
                .narrative(String.format("'%s' is not suited for patrol. Took damage.", ship.getName()))
                .build();
    }

    private static MissionResult handleExplore(SpaceShip ship, Mission mission, SpaceEvent event, int fuelCost) {
        MissionResult.Builder result = new MissionResult.Builder(mission, event)
                .fuelConsumed(fuelCost);

        if (ship instanceof ScoutShip scout) {
            return switch (event) {
                case NEBULA_INTERFERENCE -> {
                    boolean mitigated = scout.getSensorRange() >= 200;
                    yield result
                            .success(mitigated)
                            .hullDamageTaken(mitigated ? 0 : 15)
                            .creditsEarned(mitigated ? mission.baseReward() : mission.baseReward() * 0.3)
                            .narrative(mitigated
                                    ? String.format("'%s' used superior sensors to navigate the nebula.", ship.getName())
                                    : String.format("'%s' lost in nebula interference. Low sensor range.", ship.getName()))
                            .build();
                }
                case DERELICT_SHIP -> {
                    double bonus = scout.getSensorRange() >= 300 ? 2.0 : 1.2;
                    yield result
                            .success(true)
                            .creditsEarned(mission.baseReward() * bonus)
                            .narrative(String.format("'%s' located a derelict. Sensor range determined salvage value.", ship.getName()))
                            .build();
                }
                default -> result
                        .success(true)
                        .creditsEarned(mission.baseReward())
                        .narrative(String.format("'%s' completed exploration. Event: %s", ship.getName(), event))
                        .build();
            };
        }

        // Non-scout on an explore mission ==> penalized
        return result
                .success(false)
                .hullDamageTaken(10)
                .creditsEarned(mission.baseReward() * 0.2)
                .narrative(String.format("'%s' is not suited for exploration.", ship.getName()))
                .build();
    }

    private static MissionResult handleHaul(SpaceShip ship, Mission mission, SpaceEvent event, int fuelCost) {
        MissionResult.Builder result = new MissionResult.Builder(mission, event)
                .fuelConsumed(fuelCost);

        if (ship instanceof CargoShip cargoShip) {
            double cargoWeight = cargoShip.getCargoManifest().entrySet().stream()
                    .mapToDouble(e -> e.getKey().getWeight() * e.getValue())
                    .sum();

            return switch (event) {
                case DEBRIS_FIELD -> {
                    boolean heavyLoad = cargoWeight >= 50;
                    int damage = heavyLoad ? 25 : 10;
                    yield result
                            .success(true)
                            .hullDamageTaken(damage)
                            .creditsEarned(mission.baseReward())
                            .narrative(String.format("'%s' navigated debris. Heavy load made maneuvering %s.",
                                    ship.getName(), heavyLoad ? "difficult" : "manageable"))
                            .build();
                }
                case PIRATE_ATTACK -> result
                        .success(false)
                        .hullDamageTaken(30)
                        .creditsEarned(0)
                        .narrative(String.format("'%s' was raided. No escort assigned.", ship.getName()))
                        .build();
                default -> result
                        .success(true)
                        .creditsEarned(mission.baseReward() + cargoWeight * 0.1)
                        .narrative(String.format("'%s' delivered %.1f tonnes. Event: %s",
                                ship.getName(), cargoWeight, event))
                        .build();
            };
        }

        // Non-cargo ship on a haul mission ==> penalized
        return result
                .success(false)
                .creditsEarned(0)
                .narrative(String.format("'%s' has no cargo capacity.", ship.getName()))
                .build();
    }

    private static void applyResult(SpaceShip ship, MissionResult result) {
        ship.travel(result.getMission().distance());

        int newHull = Math.max(0, ship.getHullIntegrity() - result.getHullDamageTaken());
        ship.setHullIntegrity(newHull);
    }

    private static SpaceEvent rollEvent() {
        int roll = random.nextInt(100);
        int accumulator = 0;
        SpaceEvent[] events = SpaceEvent.values();

        for (int i = 0; i < events.length; i++) {
            accumulator += EVENT_WEIGHTS[i];
            if (roll < accumulator) return events[i];
        }

        return SpaceEvent.CLEAR;
    }

    private static void validateDispatch(SpaceShip ship, Mission mission) {
        if (ship == null)
            throw new IllegalArgumentException("Ship must not be null");
        if (mission == null)
            throw new IllegalArgumentException("Mission must not be null");

        int fuelCost = ship.calculateFuelCost(mission.distance());

        if (ship.getFuelLevel() < fuelCost) {
            throw new IllegalStateException(String.format(
                    "'%s' has insufficient fuel for this mission. Needs %d, has %d",
                    ship.getName(), fuelCost, ship.getFuelLevel()));
        }
    }
}
