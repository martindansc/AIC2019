package Expert;

import aic2019.*;

public class Catapult {
    private Injection in;
    private int counter = 0;
    private Location objectiveLocation;
    private boolean claimObjective = false;
    private boolean isTower;

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

        if (counter == 1 && in.memoryManager.getObjectiveType(in.memoryManager.getObjectiveIdInLocation(objectiveLocation))
                == in.constants.WATER_OBJECTIVE) {
            counter = 0;
            in.memoryManager.removeObjective(in.memoryManager.getObjectiveIdInLocation(objectiveLocation));
            objectiveLocation = null;
        }

        if (!in.unitController.canAttack()) return false;
        if (objectiveLocation == null) return false;
        if (in.staticVariables.allyBase.isEqual(objectiveLocation)) return false;

        if (in.unitController.canAttack(objectiveLocation)) {
            in.unitController.attack(objectiveLocation);
            if (counter < 2) {
                counter++;

                if(counter == 2) {
                    counter = 0;
                    in.memoryManager.removeObjective(in.memoryManager.getObjectiveIdInLocation(objectiveLocation));
                    if (isTower) {
                        int newObjective[] = in.objectives.createCatapultObjective(objectiveLocation,
                                in.constants.DESTROYED_TOWER);
                        in.memoryManager.addObjective(UnitType.BASE, newObjective);
                    }
                    objectiveLocation = null;
                }
            }
            return true;
        }
        return false;
    }

    public void tryMove(Location target) {
        if (!in.unitController.canMove()) return;
        if(target != null) {
            if (tooClose(target)) {
                if (in.unitController.canMove(in.staticVariables.myLocation.directionTo(target).opposite())) {
                    in.unitController.move(in.staticVariables.myLocation.directionTo(target).opposite());
                    return;
                }
            }
            if (catapultInRange(target)) return;
        }

        if (!doMicro()) {
            Direction dir = in.pathfinder.getNextLocationTarget(target, in.memoryManager::isLocationSafe);
            if (dir != null && in.unitController.canMove(dir)) {
                if (in.memoryManager.isLocationSafe(in.staticVariables.myLocation.add(dir))) {
                    in.unitController.move(dir);
                }
            }
        }
    }

    public boolean catapultInRange(Location target) {
        return target.distanceSquared(in.staticVariables.myLocation) <= GameConstants.CATAPULT_ATTACK_RANGE_SQUARED;
    }

    public boolean tooClose(Location target) {
        return target.distanceSquared(in.staticVariables.myLocation) <= GameConstants.CATAPULT_ATTACK_MIN_RANGE_SQUARED;
    }

    public boolean doMicro() {

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        for (UnitInfo enemy : in.staticVariables.allenemies) {
            if (enemy.getType() == UnitType.TOWER) {
                int type = enemy.getTeam() != in.staticVariables.opponent ?
                        in.constants.NEUTRAL_TOWER : in.constants.ENEMY_TOWER;
                int[] objectives = in.objectives.createCatapultObjective(enemy.getLocation(), type);
                in.memoryManager.addObjective(UnitType.BASE, objectives);
            }
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
            if (!in.memoryManager.isLocationSafe(loc)) {
                numEnemies += 100;
                return;
            }
            if (in.unitController.senseImpact(loc) != 0) {
                numEnemies += 100;
                return;
            }
            if (unit.getType() != UnitType.WORKER) {
                int distance = unit.getLocation().distanceSquared(loc);
                if (distance <= unit.getType().attackRangeSquared) ++numEnemies;
                if (distance < minDistToEnemy) minDistToEnemy = distance;
            }
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
        // can we add a new objective?
        int closestObjective = Integer.MAX_VALUE;
        int closestOccupedObjective = Integer.MAX_VALUE;
        Location bestLocation = null;
        Location bestOccupedLocation = null;

        int[][] objectives = in.memoryManager.getObjectives();

        for (int[] objective: objectives) {

            if(objective[0] == 0) continue;

            Location newObjectiveLocation = in.objectives.getLocationObjective(objective);
            int distance = in.staticVariables.myLocation.distanceSquared(newObjectiveLocation);

            if(in.staticVariables.type.getAttackRangeSquared() >= in.staticVariables.myLocation.distanceSquared(newObjectiveLocation) &&
                    !in.unitController.canAttack(objectiveLocation)) {
                if (isTower(newObjectiveLocation)) isTower = true;
                this.fixObjectiveLocation(newObjectiveLocation);
                claimObjective = true;
            }

            if(!in.objectives.isFull(objective) || in.objectives.getLastClaimedId(objective) == in.staticVariables.myId){
                // for now, as heuristic we are going to get the distance to the objective
                if(distance < closestObjective) {
                    closestObjective = distance;
                    bestLocation = newObjectiveLocation;
                }

            } else {
                if(distance < closestOccupedObjective) {
                    closestOccupedObjective = distance;
                    bestOccupedLocation = newObjectiveLocation;
                }
            }

            if(distance < in.constants.CATAPULTS_CONSIDER_CLOSE_DISTANCE ||
                    in.staticVariables.myLocation.distanceSquared(newObjectiveLocation) < in.constants.CATAPULTS_CONSIDER_CLOSE_DISTANCE) {
                in.objectives.claimObjective(newObjectiveLocation);
            }
        }

        if(bestLocation != null && (objectiveLocation == null || !claimObjective)){
            if (isTower(bestLocation)) isTower = true;
            this.fixObjectiveLocation(bestLocation);
            claimObjective = true;
        }

        // just to do something in case we have nothing to do but there is still an objective...
        // we won't claim it thought
        if(objectiveLocation == null && bestOccupedLocation != null){
            if (!bestOccupedLocation.isEqual(new Location(0,0))) {
                if (isTower(bestOccupedLocation)) isTower = true;
            }
            this.fixObjectiveLocation(bestOccupedLocation);
            claimObjective = false;
        }

        // claim objective
        if(objectiveLocation != null && claimObjective) {
            if (isTower(objectiveLocation)) isTower = true;
            in.objectives.claimObjective(this.objectiveLocation);
        }
    }

    public boolean isTower(Location loc) {
        int type = in.memoryManager.getObjectiveType(in.memoryManager.getObjectiveIdInLocation(loc));
        return type == in.constants.ENEMY_TOWER || type == in.constants.NEUTRAL_TOWER;
    }

}
