package Rookie;

import aic2019.*;

public class Move {
    private Injection in;

    public Move(Injection in){
        this.in = in;
    }

    public Location getTarget() {
        if (in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER) + in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER) < 10 && in.staticVariables.round < 100) {
            return in.staticVariables.allyBase;
        }

        TownInfo neutralTown = null;
        int neutralDistance = 100000;
        TownInfo enemyTown = null;
        int enemyDistance = 10000;

        for (TownInfo town : in.staticVariables.enemytowns) {
            Location townLoc = town.getLocation();
            int currentDistance = in.staticVariables.myLocation.distanceSquared(townLoc);
            if (town.getOwner() == in.staticVariables.opponent) {
                if (currentDistance < enemyDistance) {
                    enemyDistance = currentDistance;
                    enemyTown = town;
                }
            } else {
                if (currentDistance < neutralDistance) {
                    neutralDistance = currentDistance;
                    neutralTown = town;
                }
            }
        }

        if (neutralTown != null && enemyTown != null) {
            if (neutralDistance > enemyDistance) return enemyTown.getLocation();
            return neutralTown.getLocation();
        } else if (neutralTown != null) {
            return neutralTown.getLocation();
        } else if (enemyTown != null) {
            return enemyTown.getLocation();
        } else {
            return in.staticVariables.enemyBase;
        }
    }
}
