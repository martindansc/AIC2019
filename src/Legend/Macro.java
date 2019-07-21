package Legend;

import aic2019.Location;
import aic2019.TownInfo;

public class Macro {
    private Injection in;
    private Location previousStolen = null;

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
        if ((in.staticVariables.myTowns.length > enemyTowns || in.staticVariables.myTowns.length >= in.staticVariables.allenemytowns.length) &&
                in.staticVariables.allies.getVictoryPoints() >= in.staticVariables.opponent.getVictoryPoints()) {
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
                previousStolen = null;
                return defendTown.getLocation();
            }
            previousStolen = null;
            return in.staticVariables.allyBase;
        }

        TownInfo allyTown = null;
        int allyDistance = Integer.MAX_VALUE;

        for (TownInfo town : in.staticVariables.myTowns) {
            if (previousStolen!= null && previousStolen.isEqual(town.getLocation())) previousStolen = null;
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
            previousStolen = null;
            return allyTown.getLocation();
        }

        return setTarget();
    }

    public Location setTarget() {
        if (previousStolen != null) return previousStolen;

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
                if (closestStolen > distance) {
                    closestStolen = distance;
                    stolen = loc;
                }
            }
        }

        if (stolen != null) {
            previousStolen = stolen;
            return stolen;
        }

        if (bestTarget != null) {
            int knights = in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER);
            int soldiers = in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER);
            int archers = in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER);
            int mages = in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER);
            if (knights + soldiers + archers + mages > 2 * bestScore) {
                previousStolen = null;
                return bestTarget;
            }
        }

        previousStolen = null;
        return in.staticVariables.allyBase;
    }
}
