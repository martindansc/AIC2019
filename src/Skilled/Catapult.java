package Skilled;

import aic2019.Direction;
import aic2019.GameConstants;
import aic2019.Location;
import aic2019.UnitInfo;

public class Catapult {
    private Injection in;
    private int counter = 0;
    private int delay = 0;
    private Location objectiveLocation;

    public Catapult(Injection in) {
        this.in = in;
    }

    public void run() {
        selectObjective();
        in.catapult.tryMove(objectiveLocation);
        tryAttack();
    }

    public boolean tryAttack() {
        int myAttack = in.attack.getMyAttack();

        if (counter == 2) {
            delay++;
            if (delay == 5) {
                counter = 0;
                delay = 0;
                in.map.unmarkTower(objectiveLocation);
                in.memoryManager.removeObjective(in.memoryManager.getObjectiveIdInLocation(objectiveLocation));
                objectiveLocation = null;
            }
        }

        if (!in.unitController.canAttack()) return false;
        if (objectiveLocation == null) return false;
        if (in.staticVariables.allyBase.isEqual(objectiveLocation)) return false;

        if (in.unitController.canAttack(objectiveLocation)) {
            in.unitController.attack(objectiveLocation);
            if (counter < 2) {
                counter++;
            }
            return true;
        }
        return false;
    }

    public void tryMove(Location target) {
        if(target != null) {
            if (!in.unitController.canMove()) return;
            if (catapultInRange(target)) return;
        }

        if (!doMicro()) {
            Direction dir = in.pathfinder.getNextLocationTarget(target);
            if (dir != null && in.unitController.canMove(dir)) {
                in.unitController.move(dir);
            }
        }
    }

    public boolean catapultInRange(Location target) {
        return target.distanceSquared(in.staticVariables.myLocation) <= GameConstants.CATAPULT_ATTACK_RANGE_SQUARED;
    }

    public boolean doMicro() {

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        for (UnitInfo enemy : in.staticVariables.allenemies) {
            for (int i = 0; i < 9; i++) {
                microInfo[i].update(enemy);
            }
        }

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

    public void fixObjectiveLocation(Location loc){
        objectiveLocation = loc;
    }

    public void selectObjective() {
        // do I currently have an objective set up?
        // if I don't have an objective, I can check for one in the objectives array and get the best
        if(objectiveLocation == null) {
            int closestObjective = Integer.MAX_VALUE;
            Location bestLocation = null;

            int[][] objectives = in.memoryManager.getObjectives();

            for (int[] objective: objectives) {
                if(!in.objectives.isFull(objective)){
                    // for now, as heuristic we are going to get the distance to the resource
                    Location objectiveLocation = in.objectives.getLocationObjective(objective);
                    int distance = in.staticVariables.myLocation.distanceSquared(objectiveLocation);
                    if(distance < closestObjective) {
                        closestObjective = distance;
                        bestLocation = objectiveLocation;
                    }
                }
            }

            if(bestLocation != null){
                this.fixObjectiveLocation(bestLocation);
            }

        }

        // claim objective
        if(objectiveLocation != null) {
            in.objectives.claimObjective(this.objectiveLocation);
        }
    }

}
