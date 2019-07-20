package Legend;

import aic2019.*;

public class ExplorerPathfinder {

    private Injection in;

    public ExplorerPathfinder(Injection in) {
        this.in = in;
    }

    final int INF = 1000000;

    boolean rotateRight = true; //if I should rotate right or left
    boolean rotate = false;
    Location lastObstacleFound = null; //latest obstacle I've found in my way
    int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
    Location prevTarget = null; //previous target
    Direction microDir;
    MicroInfo[] microInfo = new MicroInfo[9];

    Boolean getNextLocationTarget(Location target){
        //No target? ==> bye!
        if (target == null) return false;

        //different target? ==> previous data does not help!
        if (prevTarget == null || !target.isEqual(prevTarget)) resetPathfinding();

        //If I'm at a minimum distance to the target, I'm free!
        Location myLoc = in.staticVariables.myLocation;
        int d = myLoc.distanceSquared(target);
        if (d <= minDistToEnemy) resetPathfinding();

        //Update data
        prevTarget = target;
        minDistToEnemy = Math.min(d, minDistToEnemy);

        //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
        Direction dir = myLoc.directionTo(target);
        if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);

        //This should not happen for a single unit, but whatever
        if (canMove(dir)) resetPathfinding();

        //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
        //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
        doMicro();

        for (int i = 0; i < 16; ++i){
            for (int j = 0; j < in.staticVariables.dirs.length; j++) {
                if (in.staticVariables.dirs[j] == dir) {
                    if (canMove(dir) && microInfo[j].numEnemies == 0) {
                        in.unitController.move(dir);
                        return false;
                    }
                    break;
                }
            }
            if (!rotate && myLoc.add(dir.rotateLeft()).distanceSquared(target) > myLoc.add(dir.rotateRight()).distanceSquared(target)) {
                rotateRight = true;
                rotate = true;
            }
            Location newLoc = myLoc.add(dir);
            if (in.unitController.isOutOfMap(newLoc)) rotateRight = !rotateRight;
                //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
            else lastObstacleFound = myLoc.add(dir);
            if (rotateRight) dir = dir.rotateRight();
            else dir = dir.rotateLeft();
        }

        for (int j = 0; j < in.staticVariables.dirs.length; j++) {
            if (in.staticVariables.dirs[j] == dir) {
                if (canMove(dir) && microInfo[j].numEnemies == 0) {
                    in.unitController.move(dir);
                    return false;
                }
                break;
            }
        }

        in.unitController.move(microDir);

        return true;
    }

    void resetPathfinding(){
        lastObstacleFound = null;
        minDistToEnemy = INF;
    }

    public boolean canMove(Direction dir) {
        Location loc = in.staticVariables.myLocation.add(dir);
        Terrain type = in.unitController.senseTerrain(loc);
        UnitInfo unit = in.unitController.senseUnit(loc);
        if (type == Terrain.PLAINS && unit == null) {
            return true;
        }
        return false;
    }

    public void doMicro() {
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        for (int i = 0; i < 9; i++) {
            microInfo[i].updateArea();
            int length = Math.min(11, in.staticVariables.allenemies.length);
            for (int j = 0; j < length; j++) {
                UnitInfo enemy = in.staticVariables.allenemies[j];
                UnitType enemyType = enemy.getType();
                Team unitTeam = enemy.getTeam();
                int distance = microInfo[i].loc.distanceSquared(enemy.getLocation());

                if (unitTeam == in.staticVariables.opponent) {
                    microInfo[i].updateSafe(distance, enemyType);
                } else {
                    int health = enemy.getHealth();
                    int maxHealth = enemyType.maxHealth;
                    if (health < maxHealth) {
                        microInfo[i].updateSafe(distance, enemyType);
                    } else {
                        microInfo[i].updateGreedy(distance, enemyType);
                    }
                }

                if (enemyType == UnitType.TOWER) {
                    if (distance < enemyType.getAttackRangeSquared()) microInfo[i].numEnemies++;
                }
            }
        }

        int bestIndex = -1;

        for (int i = 8; i >= 0; i--) {
            if (!in.unitController.canMove(in.staticVariables.dirs[i])) continue;
            if (bestIndex < 0 || !microInfo[bestIndex].isBetter(microInfo[i])) bestIndex = i;
        }

        if (bestIndex != -1) {
            if (in.staticVariables.allenemies.length > 0) {
                microDir = (in.staticVariables.dirs[bestIndex]);
            }
        }
    }

    class MicroInfo {
        int numEnemies;
        int minDistToEnemy;
        Location loc;

        public MicroInfo(Location loc) {
            this.loc = loc;
            numEnemies = 0;
            minDistToEnemy = 100000;
        }

        void updateSafe(int distance, UnitType enemyType) {
            if (enemyType == UnitType.SOLDIER) {
                if (distance < 14) numEnemies++;
            } else if (enemyType == UnitType.ARCHER) {
                if (distance < 33) numEnemies++;
            } else if (enemyType == UnitType.KNIGHT) {
                if (distance < 9) numEnemies++;
            } else if (enemyType == UnitType.MAGE) {
                if (distance < 26) numEnemies++;
            } else if (enemyType == UnitType.EXPLORER) {
                if (distance < 14) numEnemies++;
            } else if (enemyType == UnitType.TOWER) {
                if (distance < 33) numEnemies++;
            }

            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        void updateGreedy(int distance, UnitType enemyType) {
            if (enemyType == UnitType.SOLDIER) {
                if (distance < 9) numEnemies++;
            } else if (enemyType == UnitType.ARCHER) {
                if (distance < 19) numEnemies++;
            } else if (enemyType == UnitType.KNIGHT) {
                if (distance < 9) numEnemies++;
            } else if (enemyType == UnitType.MAGE) {
                if (distance < 14) numEnemies++;
            } else if (enemyType == UnitType.EXPLORER) {
                if (distance < 9) numEnemies++;
            } else if (enemyType == UnitType.TOWER) {
                if (distance < 33) numEnemies++;
            }

            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        void updateArea() {
            if (in.unitController.senseImpact(loc) != 0) {
                numEnemies += 100;
                return;
            }
            if (!in.memoryManager.isLocationSafe(loc)) {
                numEnemies += 100;
                return;
            }
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return true;
            if (numEnemies >= 100) return false;
            if (m.numEnemies >= 100) return true;
            if (numEnemies < m.numEnemies) return true;
            if (numEnemies > m.numEnemies) return false;
            return minDistToEnemy >= m.minDistToEnemy;
        }
    }

}