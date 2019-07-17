package Expert;

import aic2019.Location;
import aic2019.Terrain;
import aic2019.TownInfo;
import aic2019.UnitType;

public class Helper {
    private final Injection in;

    Helper(Injection in) {
        this.in = in;
    }

    public int unitTypeToInt(UnitType ut) {
        UnitType[] values = UnitType.values();
        for(int i = 0; i < values.length; i++){
            if(ut == values[i]) return i + 1;
        }

        return -1;
    }

    public UnitType intToUnitType(int type) {
        if(type <= 0) return null;
        UnitType[] values = UnitType.values();
        return values[type - 1];
    }

    public int locationToInt(int locX, int locY) {
        return (locX - in.staticVariables.allyBase.x + 50) * 100 + (locY - in.staticVariables.allyBase.y + 50);
    }

    public int locationToInt(Location loc) {
        return locationToInt(loc.x, loc.y);
    }

    public Location intToLocation(int number) {
        return new Location((number / 100) + in.staticVariables.allyBase.x - 50, (number % 100) + in.staticVariables.allyBase.y - 50);
    }

    public void countUnit(UnitType ut) {
        if (ut == UnitType.SOLDIER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_SOLDIER_COUNTER);
        } else if (ut == UnitType.ARCHER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_ARCHER_COUNTER);
        } else if (ut == UnitType.KNIGHT) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_KNIGHT_COUNTER);
        } else if (ut == UnitType.CATAPULT) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_CATAPULT_COUNTER);
        } else if (ut == UnitType.MAGE) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_MAGE_COUNTER);
        } else if (ut == UnitType.WORKER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_WORKERS_COUNTER);
        } else if (ut == UnitType.EXPLORER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_EXPLORERS_COUNTER);
        }
    }

    public void countUnits() {
        this.countUnit(in.staticVariables.type);
    }

    public boolean isObstructedWater(Location loc1, Location loc2) {
        int x0 = loc1.x;
        int x1 = loc2.x;
        int y0 = loc1.y;
        int y1 = loc2.y;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true) {

            if (in.unitController.senseTerrain(new Location(x0, y0)) == Terrain.WATER) return true;

            if (x0 == x1 && y0 == y1) break;

            e2 = 2 * err;

            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
        return false;
    }

    //Returns closest town or base
    public Location getClosestTownToLocation(Location location){
        Location loc = in.staticVariables.allies.getInitialLocation();
        for(TownInfo tI : in.staticVariables.myTowns){
            int currentDistance = Math.abs(loc.distanceSquared(location));
            int nextDistance = Math.abs(tI.getLocation().distanceSquared(location));
            if( nextDistance < currentDistance ){
                loc = tI.getLocation();
            }
        }
        return loc;
    }

    // Returns a Location 1/4th from our base in direction to the enemy base
    public Location getMeetingPoint() {
        int x = Math.abs(in.staticVariables.allyBase.x - in.staticVariables.enemyBase.x) / 4;
        int y = Math.abs(in.staticVariables.allyBase.y - in.staticVariables.enemyBase.y) / 4;

        if (in.staticVariables.allyBase.x > in.staticVariables.enemyBase.x) {
            x = -x;
        }
        if (in.staticVariables.allyBase.y > in.staticVariables.enemyBase.y) {
            y = -y;
        }

        return new Location(in.staticVariables.allyBase.x + x, in.staticVariables.allyBase.y + y);
    }

    public void updateScores() {
        for (TownInfo town: in.staticVariables.allenemytowns) {
            Location loc = town.getLocation();
            int distance = in.staticVariables.myLocation.distanceSquared(loc);
            if (distance < 17) {
                int score = in.explorer.calculateScore(loc);
                in.memoryManager.setTownScore(loc, score);
                in.memoryManager.markTownAsExplored(loc);
            }
        }
    }

}
