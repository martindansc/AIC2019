package Veteran;

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
        in.attack.genericTryAttack();
        if (in.soldier.tryMove(target)) {
            in.staticVariables.myLocation = in.unitController.getLocation();
            in.staticVariables.allenemies = in.unitController.senseUnits(in.staticVariables.allies, true);
        }
        in.attack.genericTryAttack();
        in.attack.genericTryAttackTown(target);
    }

    public boolean tryMove(Location target) {
        if (!in.unitController.canMove()) return false;
        boolean isTargetBase = in.staticVariables.allyBase.isEqual(target);
        boolean isTargetObstructed = in.unitController.canSenseLocation(target) && in.unitController.isObstructed(target, in.staticVariables.myLocation);

        if (!microResult) {
            Direction dir = in.pathfinder.getNextLocationTarget(target, loc -> in.memoryManager.isLocationSafe(loc));
            if (dir != null && in.unitController.senseImpact(in.staticVariables.myLocation.add(dir)) == 0) {
                if (isTargetBase || isTargetObstructed || !in.attack.canAttackTarget(target) || (in.attack.canAttackTarget(target) && in.staticVariables.myLocation.add(dir).distanceSquared(target) < in.staticVariables.type.getAttackRangeSquared())) {
                    if (in.unitController.canMove(dir)) {
                        if (in.memoryManager.isLocationSafe(in.staticVariables.myLocation.add(dir))) {
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
            boolean isWaterObs = in.helper.isObstructedWater(enemyLoc, in.staticVariables.myLocation);
            if (isWaterObs) {
                int[] objective;
                if (enemy.getType() == UnitType.TOWER) {
                    objective = in.objectives.createTowerObjective(enemyLoc);
                } else {
                    objective = in.objectives.createWaterObjective(enemyLoc);
                }
                in.memoryManager.addObjective(UnitType.CATAPULT, objective);
            }
            if (!in.unitController.isObstructed(enemyLoc, in.staticVariables.myLocation) && !isWaterObs) {
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
            if (in.unitController.senseImpact(loc) != 0) {
                numEnemies += 100;
                return;
            }
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
