package Rookie;

import aic2019.*;

public class Mage {
    private Injection in;
    private boolean enemies = false;
    private Boolean microResult;
    private Direction microDir;

    public Mage(Injection in) {
        this.in = in;
    }

    public void run(Location target) {
        microResult = doMicro();
        in.mage.tryMove(target);
        if (in.mage.tryMove(target)) {
            in.staticVariables.myLocation = in.unitController.getLocation();
            in.staticVariables.allenemies = in.unitController.senseUnits(in.staticVariables.allies, true);
        }
        tryAttack(target);
    }

    public boolean tryAttack(Location town) {
        if (!in.unitController.canAttack()) return false;
        if (!enemies && (!in.unitController.canSenseLocation(town) || (in.unitController.canSenseLocation(town) && in.unitController.isObstructed(town, in.staticVariables.myLocation)))) return false;

        int myAttack = in.attack.getMyAttack();

        Location[] locs = in.unitController.getVisibleLocations(5);
        int[] scores = new int[locs.length];
        for (int i = 0; i < locs.length; i++) {
            int distance = locs[i].distanceSquared(in.staticVariables.myLocation);
            if (distance > 2 && in.unitController.canAttack(locs[i])) {
                for (Direction dir : in.staticVariables.dirs) {
                    Location target = locs[i].add(dir);
                    if (in.unitController.canSenseLocation(target)) {
                        UnitInfo unit = in.unitController.senseUnit(target);
                        TownInfo city = in.unitController.senseTown(target);
                        if (unit != null) {
                            if (unit.getTeam() == in.staticVariables.allies) {
                                scores[i]--;
                            } else {
                                scores[i]++;
                            }
                        } else if (city != null){
                            if (city.getOwner() == in.staticVariables.allies) {
                                scores[i]--;
                            } else {
                                scores[i]++;
                            }
                        }
                    }
                }
            }
        }

        int index = -1;
        int bestscore = 0;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > bestscore) {
                index = i;
                bestscore = scores[i];
            }
        }

        if (index != -1) {
            in.unitController.attack(locs[index]);
            return true;
        }
        return false;
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

        for (int j = 0; j < in.staticVariables.allenemies.length; j++) {
            if (!in.unitController.isObstructed(in.staticVariables.allenemies[j].getLocation(), in.staticVariables.myLocation)) {
                enemies = true;
                for (int i = 0; i < 9; i++) {
                    microInfo[i].update(in.staticVariables.allenemies[j]);
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
            if (distance <= unit.getType().attackRangeSquared) ++numEnemies;
            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        boolean canAttack() {
            return in.staticVariables.type.getAttackRangeSquared() >= minDistToEnemy;
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return false;
            if (numEnemies < m.numEnemies) return true;
            if (numEnemies > m.numEnemies) return false;
            if (canAttack()) {
                if (!m.canAttack()) return true;
                return minDistToEnemy >= m.minDistToEnemy;
            }
            if (m.canAttack()) return false;
            return minDistToEnemy <= m.minDistToEnemy;
        }
    }

}
