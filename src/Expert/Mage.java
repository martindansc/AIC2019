package Expert;

import aic2019.Direction;
import aic2019.Location;
import aic2019.UnitInfo;

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
        tryAttack(target);
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
        int closeDistance = 10000;
        for (UnitInfo enemy: in.staticVariables.allenemies) {
            int currentDistance = in.staticVariables.myLocation.distanceSquared(enemy.getLocation());
            if (currentDistance < closeDistance) {
                closeDistance = currentDistance;
            }
        }

        if (closeDistance > 13) {
            if (in.unitController.canAttack(town) && in.unitController.senseTown(town) != null && in.unitController.senseTown(town).getOwner() != in.staticVariables.allies) {
                int allyUnits = 0;
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        UnitInfo unit = in.unitController.senseUnit(new Location(town.x + i, town.y + j));
                        if (unit != null) {
                            if (unit.getTeam() == in.staticVariables.allies) {
                                allyUnits++;
                            }
                        }
                    }
                }
                if (allyUnits == 0) {
                    in.unitController.attack(town);
                    return true;
                }
            }
            return false;
        }

        int myAttack = in.attack.getMyAttack();

        Location[] locs = in.unitController.getVisibleLocations(5);
        Location bestLocation = null;
        int bestLocationScore = 0;
        for (int i = 0; i < locs.length; i++) {
            int distance = locs[i].distanceSquared(in.staticVariables.myLocation);
            if (distance > 2 && in.unitController.canAttack(locs[i])) {
                int currentScore = 0;
                for (int j = -1; j < 2; j++) {
                    for (int k = -1; k < 2; k++) {
                        UnitInfo unit = in.unitController.senseUnit(new Location (locs[i].x + j, locs[i].y + k));
                        if (unit != null) {
                            if (unit.getTeam() != in.staticVariables.allies) {
                                currentScore++;
                            } else {
                                currentScore--;
                            }
                        }
                    }
                }
                if (currentScore > bestLocationScore) {
                    bestLocationScore = currentScore;
                    bestLocation = locs[i];
                }
            }
        }

        if (bestLocation != null) {
            in.unitController.attack(bestLocation);
            return true;
        }
        return false;
    }

    public boolean tryMove(Location target) {
        if (!in.unitController.canMove()) return false;
        boolean isTargetBase = in.staticVariables.allyBase.isEqual(target);
        boolean isTargetObstructed = in.unitController.canSenseLocation(target) && in.unitController.isObstructed(target, in.staticVariables.myLocation);

        if (!microResult) {
            Direction dir = in.pathfinder.getNextLocationTarget(target);
            if (dir != null && in.unitController.senseImpact(in.staticVariables.myLocation.add(dir)) == 0) {
                if (isTargetBase || isTargetObstructed || !in.attack.canAttackTarget(target)) {
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
            if (!in.memoryManager.isLocationSafe(loc)) {
                numEnemies += 100;
                return;
            }
            if (in.unitController.senseImpact(loc) != 0) {
                numEnemies += 100;
                return;
            }
            int distance = unit.getLocation().distanceSquared(loc);
            if (distance <= unit.getType().attackRangeSquared) ++numEnemies;
            if (distance < minDistToEnemy) minDistToEnemy = distance;

        }

        boolean canAttack() {
            return in.staticVariables.type.getAttackRangeSquared() >= minDistToEnemy;
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return true;
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
