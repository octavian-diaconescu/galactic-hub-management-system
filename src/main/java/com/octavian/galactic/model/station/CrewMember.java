package com.octavian.galactic.model.station;

import com.octavian.galactic.model.SpaceEntity;

public class CrewMember extends SpaceEntity implements Comparable<CrewMember> {

    //TODO
    public enum Rank{
        COMMANDER,
        OFFICER,
        CIVILIAN
    };

    //TODO
    @Override
    public int compareTo(CrewMember other) {

        return 0;
    }

    public CrewMember(String name){
        super(name);
    }
}
