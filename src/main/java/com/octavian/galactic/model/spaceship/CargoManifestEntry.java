package com.octavian.galactic.model.spaceship;

import com.octavian.galactic.model.cargo.CargoItem;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "cargo_manifest_entry", uniqueConstraints = @UniqueConstraint(columnNames = {"cargo_ship_id", "cargo_item_id"}))
public class CargoManifestEntry {
    @Id
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_ship_id", nullable = false)
    private CargoShip cargoShip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_item_id", nullable = false)
    private CargoItem cargoItem;

    @Column(nullable = false)
    private int quantity;

    protected CargoManifestEntry() {
    }

    public CargoManifestEntry(CargoShip cargoShip, CargoItem cargoItem, int quantity) {
        this.cargoShip = cargoShip;
        this.cargoItem = cargoItem;
        this.quantity = quantity;
        id = UUID.randomUUID();
    }

    public CargoItem getCargoItem() {
        return cargoItem;
    }

    public int getQuantity() {
        return quantity;
    }

    void addQuantity(int delta) {
        if (delta <= 0) {
            throw new IllegalArgumentException("Quantity delta must be positive");
        }
        quantity += delta;
    }
}
