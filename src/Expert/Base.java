package Expert;

import aic2019.Direction;
import aic2019.Location;
import aic2019.UnitInfo;
import aic2019.UnitType;

public class Base {

    private final Injection in;
    private int countWork = 0;

    Base(Injection in) {
        this.in = in;
    }

    public void run() {

        addCocoonUnits();

        tryAttack();

        Direction bestDir = this.getBestDirectionSpawn();

        int[] message = in.messages.readMessage();

        UnitType bestUnitType = this.chooseBestUnitType(message);

        if(bestUnitType != UnitType.BASE) {
            int id = spawnAndGetIdIfPossible(bestDir, bestUnitType);

            if(id != -1 && message[0] != 0) {
                in.messages.sendToLocation(id, message[0], message[1]);
            }
        }
    }

    private void addCocoonUnits() {
        Location[] spawnLocations = in.unitController.getVisibleLocations(2);
        for (Location spawnLocation: spawnLocations) {
            UnitInfo unit = in.unitController.senseUnit(spawnLocation);
            if(unit != null && unit.isBeingConstructed()) {
                in.helper.countUnit(unit.getType());
            }
        }
    }

    private UnitType chooseBestUnitType(int[] message) {
        int knights = in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER);
        int soldiers = in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER);
        int archers = in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER);
        int catapults = in.memoryManager.readValue(in.constants.ID_ALLIES_CATAPULT_COUNTER);
        int mages = in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER);
        int workers = in.memoryManager.readValue(in.constants.ID_ALLIES_WORKERS_COUNTER);

        if(in.messages.hasMessage()) {
            int[] newMessage = in.messages.readMessage();
            return in.helper.intToUnitType(newMessage[1]);
        }

        if (catapults < 1) {
            int[][] objectives = in.memoryManager.getObjectives(UnitType.CATAPULT);
            for (int[] objective: objectives) {
                if(!in.objectives.isFull(objective) &&
                        (in.staticVariables.round - in.objectives.getRound(objective) > 5 ||
                        catapults < 1)) {
                    return UnitType.CATAPULT;
                }
            }
        }

        if(countWork < 4) {
            int[][] objectives = in.memoryManager.getObjectives(UnitType.WORKER);
            for (int[] objective: objectives) {
                if(!in.objectives.isFull(objective)){
                    return UnitType.WORKER;
                }
            }
        }

        return in.market.heuristic();

    }

    private Direction getBestDirectionSpawn() {
        // get direction to enemy base
        Direction dir = in.staticVariables.myLocation.directionTo(in.staticVariables.opponent.getInitialLocation());
        Direction bestDir = dir;

        if(!in.unitController.canSpawn(bestDir, UnitType.SOLDIER)) {
            bestDir = dir.rotateLeft();
        }

        if(!in.unitController.canSpawn(bestDir, UnitType.SOLDIER)) {
            bestDir = dir.rotateRight();
        }

        while(!in.unitController.canSpawn(bestDir, UnitType.SOLDIER)) {

            if(dir.isEqual(bestDir)) return Direction.ZERO;

            bestDir = bestDir.rotateRight();
        }

        return bestDir;
    }

    private int spawnAndGetIdIfPossible(Direction dir, UnitType ut) {
        if (!in.market.canBuild(ut)) return -1;

        if (in.unitController.canSpawn(dir, ut)) {
            if (ut == UnitType.WORKER) {
                countWork++;
            }
            in.unitController.spawn(dir, ut);
            Location unitLocation = in.staticVariables.myLocation.add(dir);
            return in.unitController.senseUnit(unitLocation).getID();
        }
        return -1;
    }

    private boolean tryAttack() {
        if (!in.unitController.canAttack()) return false;

        UnitInfo bestCatapult = null;
        UnitInfo outerUnit = null;
        UnitInfo innerUnit = null;
        int health = 10000;

        for (UnitInfo enemy : in.staticVariables.allenemies) {
            int distance = enemy.getLocation().distanceSquared(in.staticVariables.myLocation);
            if (enemy.getType() == UnitType.CATAPULT) {
                int enemyhealth = enemy.getHealth();
                if (enemyhealth < health) {
                    health = enemyhealth;
                    bestCatapult = enemy;
                }
            }
            if (distance > 36) {
                outerUnit = enemy;
            } else if (distance < 3) {
                innerUnit = enemy;
            }
        }

        if (bestCatapult != null) {
            Location catapultLoc = bestCatapult.getLocation();
            if (bestCatapult.getLocation().distanceSquared(in.staticVariables.myLocation) > 36) {
                in.unitController.attack(catapultLoc.add(catapultLoc.directionTo(in.staticVariables.myLocation)));
                return true;
            }
            in.unitController.attack(catapultLoc);
            return true;
        }

        if (outerUnit != null) {
            Location outerLoc = outerUnit.getLocation();
            in.unitController.attack(outerLoc.add(outerLoc.directionTo(in.staticVariables.myLocation)));
            return true;
        }

        boolean attacked = in.attack.genericTryAttack();

        if (innerUnit != null) {
            Location innerLoc = innerUnit.getLocation();
            Location target = innerLoc.add(in.staticVariables.myLocation.directionTo(innerLoc));
            if (in.unitController.canAttack(target)) {
                in.unitController.attack(target);
            }
            return true;
        }

        return attacked;
    }

}
