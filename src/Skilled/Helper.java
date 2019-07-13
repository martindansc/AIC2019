package Skilled;

import aic2019.*;

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
        }
    }

    public void countUnits() {
        this.countUnit(in.staticVariables.type);
    }

    public boolean isObstructedWater(Location loc1, Location loc2) {
        if (loc2.x - loc1.x == 0) {
            if (loc2.y > loc1.y) {
                for (int y = loc1.y; y <= loc2.y; y++) {
                    if (in.unitController.senseTerrain(new Location (loc1.x, y)) == Terrain.WATER) {
                        return true;
                    }
                }
            } else {
                for (int y = loc2.y; y <= loc1.y; y++) {
                    if (in.unitController.senseTerrain(new Location (loc1.x, y)) == Terrain.WATER) {
                        return true;
                    }
                }
            }
        } else if (loc2.x > loc1.x) {
            if (loc2.y >= loc1.y) {
                int m_new = 2 * (loc2.y - loc1.y);
                int slope_error_new = m_new - (loc2.x - loc1.x);

                for (int x = loc1.x, y = loc1.y; x <= loc2.x; x++) {
                    if (in.unitController.senseTerrain(new Location (x, y)) == Terrain.WATER) {
                        return true;
                    }
                    slope_error_new += m_new;

                    if (slope_error_new >= 0) {
                        y++;
                        slope_error_new -= 2 * (loc2.x - loc1.x);
                    }
                }
            } else {
                int m_new = 2 * (loc1.y - loc2.y);
                int slope_error_new = m_new - (loc2.x - loc1.x);

                for (int x = loc1.x, y = loc2.y; x <= loc2.x; x++) {
                    if (in.unitController.senseTerrain(new Location (x, y)) == Terrain.WATER) {
                        return true;
                    }
                    slope_error_new += m_new;

                    if (slope_error_new >= 0) {
                        y++;
                        slope_error_new -= 2 * (loc2.x - loc1.x);
                    }
                }
            }
        } else {
            if (loc2.y >= loc1.y) {
                int m_new = 2 * (loc2.y - loc1.y);
                int slope_error_new = m_new - (loc1.x - loc2.x);

                for (int x = loc2.x, y = loc1.y; x <= loc1.x; x++) {
                    if (in.unitController.senseTerrain(new Location (x, y)) == Terrain.WATER) {
                        return true;
                    }
                    slope_error_new += m_new;

                    if (slope_error_new >= 0) {
                        y++;
                        slope_error_new -= 2 * (loc1.x - loc2.x);
                    }
                }
            } else {
                int m_new = 2 * (loc1.y - loc2.y);
                int slope_error_new = m_new - (loc1.x - loc2.x);

                for (int x = loc2.x, y = loc2.y; x <= loc1.x; x++) {
                    if (in.unitController.senseTerrain(new Location (x, y)) == Terrain.WATER) {
                        return true;
                    }
                    slope_error_new += m_new;

                    if (slope_error_new >= 0) {
                        y++;
                        slope_error_new -= 2 * (loc1.x - loc2.x);
                    }
                }
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

}
