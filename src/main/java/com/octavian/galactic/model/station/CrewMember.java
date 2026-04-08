package com.octavian.galactic.model.station;

import com.octavian.galactic.model.SpaceEntity;
import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "crew_member")
public class CrewMember extends SpaceEntity implements Comparable<CrewMember> {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Species species;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Rank rank;


    public enum Rank {
        COMMANDER,    // Top authority
        OFFICER,      // Standard tactical/bridge staff
        ENGINEER,     // Technical and maintenance staff
        SPECIALIST,   // Science or medical experts
        CIVILIAN,     // Non-military residents
        GUEST         // Temporary visitors
    }

    public enum Species{
        HUMAN,
        GEK,
        KORVAX,
        VYKEEN,
        ATLAS
    }
    protected CrewMember(){}
    public CrewMember(String name, Rank rank, Species species) {
        super(name);
        this.rank = Objects.requireNonNull(rank, "Rank cannot be null");
        this.species = species;
    }

    public Species getSpecies() {
        return species;
    }

    @Override
    public int compareTo(CrewMember other) {
        // Compare by Rank first
        int rankComparison = this.rank.compareTo(other.rank);
        if (rankComparison != 0) {
            return rankComparison;
        }

        // If ranks are the same, sort alphabetically by name
        int nameComparison = this.getName().compareTo(other.getName());
        if (nameComparison != 0) {
            return nameComparison;
        }

        // Compare UUIDs. This ensures the TreeSet doesn't treat different people with the same name/rank as duplicates.
        return this.getId().compareTo(other.getId());
    }

    @Override
    public String toString() {
        return this.getName() + '(' + rank + ')';
    }
}
