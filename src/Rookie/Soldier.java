package Rookie;

import aic2019.Location;
import aic2019.TownInfo;

public class Soldier {
    private Injection in;

    public Soldier(Injection in) {
        this.in = in;
    }

    public Location getSoldierTarget() {
        TownInfo closestTown = null;
        int distance = 10000;
        for (TownInfo town : in.staticVariables.enemytowns) {
            Location townLoc = town.getLocation();
            int currentDistance = in.staticVariables.myLocation.distanceSquared(townLoc);
            if (currentDistance < distance) {
                distance = currentDistance;
                closestTown = town;
            }
        }

        if (closestTown != null) {
            return closestTown.getLocation();
        } else {
            return in.staticVariables.enemyBase;
        }
    }

}
