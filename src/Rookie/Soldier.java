package Rookie;

import aic2019.*;

public class Soldier {
    private Injection in;
    private Boolean microResult;
    private Direction microDir;

    public Soldier(Injection in) {
        this.in = in;
    }

    public void run(Location target) {
        microResult = doMicro();
        in.attack.genericTryAttack(target);
        if (in.soldier.tryMove(target)) {
            in.staticVariables.myLocation = in.unitController.getLocation();
            in.staticVariables.allenemies = in.unitController.senseUnits(in.staticVariables.allies, true);
        }
        in.attack.genericTryAttack(target);
    }

    public boolean tryMove(Location target) {
        if (!in.unitController.canMove()) return false;
        boolean isTargetBase = in.staticVariables.allyBase.isEqual(target);
        boolean isTargetObstructed = in.unitController.canSenseLocation(target) && in.unitController.isObstructed(target, in.staticVariables.myLocation);

        if (in.staticVariables.type.getAttackRangeSquared() >= in.staticVariables.myLocation.distanceSquared(target) && (!isTargetBase && !isTargetObstructed)) return false;

        if (!microResult) {
            Direction dir = in.pathfinder.getNextLocationTarget(target);
            if (isTargetBase || isTargetObstructed || in.staticVariables.myLocation.add(dir).distanceSquared(target) >= in.staticVariables.type.getMinAttackRangeSquared()) {
                if (dir != null && in.unitController.canMove(dir)) {
                    if (in.memoryManager.isLocationSafe(in.staticVariables.myLocation.add(dir))) {
                        in.unitController.move(dir);
                        return true;
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
            if (!in.unitController.isObstructed(enemy.getLocation(), in.staticVariables.myLocation)) {
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
            int distance = unit.getLocation().distanceSquared(loc);
            if (distance <= unit.getType().attackRangeSquared || (unit.getType() == UnitType.MAGE && distance < 14)) {
                ++numEnemies;
            }
            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        boolean canAttack() {
            return in.staticVariables.type.getAttackRangeSquared() >= minDistToEnemy;
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return true;
            if (2 * in.staticVariables.allyUnits.length < numEnemies) return false;
            if (canAttack()) {
                if (!m.canAttack()) return true;
                return minDistToEnemy >= m.minDistToEnemy;
            }
            if (m.canAttack()) return false;
            return minDistToEnemy <= m.minDistToEnemy;
        }
    }

}
