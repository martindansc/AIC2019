package Skilled;

import aic2019.Location;
import aic2019.TownInfo;

public class Move {
    private Injection in;

    public Move(Injection in){
        this.in = in;
    }

    public Location getTarget() {
        if (in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER) + in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER) + in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER) + in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER) < 10 && in.staticVariables.round < 100) {
            return in.staticVariables.allyBase;
        }

        TownInfo allyTown = null;
        int allyDistance = Integer.MAX_VALUE;
        TownInfo neutralTown = null;
        int neutralDistance = Integer.MAX_VALUE;
        TownInfo enemyTown = null;
        int enemyDistance = Integer.MAX_VALUE;

        for (TownInfo town : in.staticVariables.myTowns) {
            int maxLoyalty = town.getMaxLoyalty();
            int loyalty = town.getLoyalty();
            if ((loyalty/maxLoyalty) < 0.2) {
                int distance = in.staticVariables.myLocation.distanceSquared(town.getLocation());
                if (distance < allyDistance) {
                    allyTown = town;
                    allyDistance = distance;
                }
            }
        }

        if (allyTown != null) {
            return allyTown.getLocation();
        }

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
