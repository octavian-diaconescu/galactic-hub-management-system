package com.octavian.galactic.model.cargo;

import com.octavian.galactic.model.SpaceEntity;

// A CargoItem derived object describes a type of cargo a cargo ship can hold
public abstract class CargoItem extends SpaceEntity {
    private final double weight;
    private final String description;

    public CargoItem(String name, double weight){
        super(name);
        if(weight < 0){
            throw new IllegalArgumentException("Cargo weight cannot be less than 0");
        }
        this.weight = weight;
        description = "N/A";
    }

    public CargoItem(String name, double weight, String description){
        super(name);
        if(weight < 0){
            throw new IllegalArgumentException("Cargo weight cannot be less than 0");
        }
        this.weight = weight;
        this.description = description;
    }


    public double getWeight() {
        return weight;
    }


    public void printItemInfo(){
        System.out.printf("*%s:%.2f(Tonnes)%n", this.getName(), weight);
        System.out.printf("----->(%s)%n", description);
    }

    @Override
    public String toString() {
        return  '\'' + this.getName() + '\'';
    }

//    public String getDescription(){
//        return description;
//    }

    // Do I want somebody else to modify the description of an item after creation? I'm not sure
    // Maybe some crew member with authority
//    public void setDescription(String description) {
//        this.description = description;
//    }
}
