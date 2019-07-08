package Rookie;

import aic2019.*;

public class Mage {
    private Injection in;

    public Mage(Injection in) {
        this.in = in;
    }

    public void run(Location target) {
        tryAttack(target);
        in.mage.tryMove(target);
        tryAttack(target);
    }

    public boolean tryAttack(Location town) {
        if (!in.unitController.canAttack()) return false;
        if (in.staticVariables.allyBase.isEqual(town)) return false;
        UnitInfo[] enemies = in.staticVariables.allenemies;
        if (enemies.length == 0 && town.isEqual(in.staticVariables.myLocation)) return false;

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

    public void tryMove(Location target) {
        if (!in.unitController.canMove()) return;
        boolean isTargetBase = in.staticVariables.allyBase.isEqual(target);
        boolean isTargetObstructed = in.unitController.canSenseLocation(target) && in.unitController.isObstructed(target, in.staticVariables.myLocation);

        if (in.staticVariables.type.getAttackRangeSquared() >= in.staticVariables.myLocation.distanceSquared(target) && (!isTargetBase && !isTargetObstructed)) return;

        if (!doMicro()) {
            Direction dir = in.pathfinder.getNextLocationTarget(target);
            if (isTargetBase || isTargetObstructed || in.staticVariables.myLocation.add(dir).distanceSquared(target) >= in.staticVariables.type.getMinAttackRangeSquared()) {
                if (dir != null && in.unitController.canMove(dir)) {
                    in.unitController.move(dir);
                }
            }
        }
    }

    public boolean doMicro() {

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        boolean enemies = false;
        for (UnitInfo enemy : in.staticVariables.allenemies) {
            if (in.staticVariables.type == UnitType.CATAPULT || !in.unitController.isObstructed(enemy.getLocation(), in.staticVariables.myLocation)) {
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
                in.unitController.move(in.staticVariables.dirs[bestIndex]);
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
