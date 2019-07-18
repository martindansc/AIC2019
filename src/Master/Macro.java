package Master;

import aic2019.Location;
import aic2019.TownInfo;

public class Macro {
    private Injection in;

    public Macro(Injection in){
        this.in = in;
    }

    public Location getTarget() {
        int enemyTowns = 0;
        for (TownInfo town: in.staticVariables.allenemytowns) {
            if (town.getOwner() == in.staticVariables.opponent) {
                enemyTowns++;
            }
        }
        if (in.staticVariables.myTowns.length > enemyTowns && in.staticVariables.allies.getVictoryPoints() >= in.staticVariables.opponent.getVictoryPoints()) {
            TownInfo defendTown = null;
            int defendDistance = Integer.MAX_VALUE;
            for (TownInfo town: in.staticVariables.myTowns) {
                int distance = in.staticVariables.myLocation.distanceSquared(town.getLocation());
                if (distance < defendDistance) {
                    defendTown = town;
                    defendDistance = distance;
                }
            }
            if (defendTown != null) {
                return defendTown.getLocation();
            }
            return in.staticVariables.allyBase;
        }

        TownInfo allyTown = null;
        int allyDistance = Integer.MAX_VALUE;

        for (TownInfo town : in.staticVariables.myTowns) {
            int maxLoyalty = town.getMaxLoyalty();
            int loyalty = town.getLoyalty();
            if (loyalty * 5 < maxLoyalty) {
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

        return setTarget();
    }

    public Location setTarget() {
        int bestScore = 10000;
        Location bestTarget = null;
        int closestStolen = 10000;
        Location stolen = null;
        for (TownInfo town: in.staticVariables.allenemytowns) {
            Location loc = town.getLocation();
            if (in.memoryManager.isTownExplored(loc)) {
                int currentScore = in.memoryManager.getTownScore(loc);
                if (currentScore < bestScore) {
                    bestScore = currentScore;
                    bestTarget = loc;
                }
            }

            if (in.memoryManager.getTownStatus(loc) == in.constants.STOLEN_TOWN) {
                int distance = loc.distanceSquared(in.staticVariables.allyBase);
                if (closestStolen < distance) {
                    closestStolen = distance;
                    stolen = loc;
                }
            }
        }

        if (stolen != null) {
            return stolen;
        }

        if (bestTarget != null) {
            int knights = in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER);
            int soldiers = in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER);
            int archers = in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER);
            int mages = in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER);
            if (knights + soldiers + archers + mages > 1.5 * bestScore) {
                return bestTarget;
            }
        }

        return in.staticVariables.allyBase;
    }
}
