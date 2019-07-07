package Rookie;

import aic2019.*;

public class Move {
    private Injection in;

    public Move(Injection in){
        this.in = in;
    }

    public void myMove(Location target) {
        if (!in.unitController.canMove()) return;
        if (catapultInRange(target)) return;

        if (in.staticVariables.type.getAttackRangeSquared() >= in.staticVariables.myLocation.distanceSquared(target)) return;

        if (!doMicro()) {
            Direction dir = in.pathfinder.getNextLocationTarget(target);
            if (in.staticVariables.myLocation.add(dir).distanceSquared(target) >= in.staticVariables.type.getMinAttackRangeSquared()) {
                if (dir != null && in.unitController.canMove(dir)) {
                    in.unitController.move(dir);
                }
            }
        }
    }

    public boolean catapultInRange(Location target) {
        if (in.staticVariables.type != UnitType.CATAPULT) return false;
        if (target.distanceSquared(in.staticVariables.myLocation) <= GameConstants.CATAPULT_ATTACK_RANGE_SQUARED) return true;
        return false;
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
            if (!in.unitController.canMove(in.staticVariables.dirs[i])) continue;;
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
