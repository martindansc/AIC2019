package LilRookie;

import aic2019.*;

public class Soldier {
    private StaticVariables variables;

    public Soldier(StaticVariables variables) {
        this.variables = variables;
    }

    public Location getSoldierTarget() {
        TownInfo closestTown = null;
        int distance = 10000;
        for (TownInfo town : variables.enemytowns) {
            Location townLoc = town.getLocation();
            int currentDistance = variables.myLocation.distanceSquared(townLoc);
            if (currentDistance < distance) {
                distance = currentDistance;
                closestTown = town;
            }
        }
        if (closestTown != null) {
            return closestTown.getLocation();
        } else {
            return variables.myLocation;
        }
    }
}
