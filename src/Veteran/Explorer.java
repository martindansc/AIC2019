package Veteran;

import aic2019.*;

public class Explorer {
    private Injection in;
    private Boolean microResult;
    private Direction microDir;


    public Explorer(Injection in) {
        this.in = in;
    }

    public void run() {
        /*
        microResult = doMicro();
        in.attack.genericTryAttack();
        if (in.explorer.tryMove(target)) {
            in.staticVariables.myLocation = in.unitController.getLocation();
            in.staticVariables.allenemies = in.unitController.senseUnits(in.staticVariables.allies, true);
        }
        in.attack.genericTryAttack();
        in.attack.genericTryAttackTown(target);
        */
    }

    public boolean tryMove(Location target) {
        if (!in.unitController.canMove()) return false;
        boolean isTargetBase = in.staticVariables.allyBase.isEqual(target);
        boolean isTargetObstructed = in.unitController.canSenseLocation(target) && in.unitController.isObstructed(target, in.staticVariables.myLocation);

        if (!microResult) {
            Direction dir = in.pathfinder.getNextLocationTarget(target, loc -> in.memoryManager.isLocationSafe(loc));
            if (dir != null) {
                if (isTargetBase || isTargetObstructed) {
                    if (in.unitController.canMove(dir)) {
                        if (in.memoryManager.isLocationSafe(in.staticVariables.myLocation.add(dir))) {
                            //TODO iterate enemies
                            in.unitController.move(dir);
                            return true;
                        }
                    }
                }
            }
        } else {
            in.unitController.move(microDir);
        }

        return false;
    }

    public boolean doMicro() {

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        boolean enemies = false;
        for (UnitInfo enemy : in.staticVariables.allenemies) {
            Location enemyLoc = enemy.getLocation();
            if (!in.unitController.isObstructed(enemyLoc, in.staticVariables.myLocation)) {
                enemies = true;
                for (int i = 0; i < 9; i++) {
                    microInfo[i].update(enemy);
                }
            }
        }

        if (!enemies) return false;

        int bestIndex = -1;

        for (int i = 8; i >= 0; i--) {
            if (!in.unitController.canMove(in.staticVariables.dirs[i])) continue;
            if (bestIndex < 0 || !microInfo[bestIndex].isBetter(microInfo[i])) bestIndex = i;
        }

        if (bestIndex != -1) {
            if (in.staticVariables.allenemies.length > 0) {
                microDir = (in.staticVariables.dirs[bestIndex]);
                return true;
            }
        }

        return false;
    }

    class MicroInfo {
        int numEnemies;
        int minDistToEnemy;
        Location loc;

        public MicroInfo(Location loc) {
            this.loc = loc;
            numEnemies = 0;
            minDistToEnemy =  100000;
        }

        void update(UnitInfo unit) {
            if (!in.memoryManager.isLocationSafe(loc)) {
                numEnemies += 100;
                return;
            }
            int distance = unit.getLocation().distanceSquared(loc);
            //TODO check for move + attack
            if (distance <= unit.getType().attackRangeSquared || (unit.getType() == UnitType.MAGE && distance < 14)) {
                ++numEnemies;
            }
            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return true;
            return minDistToEnemy >= m.minDistToEnemy;
        }
    }
}
