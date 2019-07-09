package Rookie;

import aic2019.Direction;
import aic2019.Location;
import aic2019.UnitInfo;
import aic2019.UnitType;

public class Base {

    private final Injection in;

    Base(Injection in) {
        this.in = in;
    }

    public void run() {

        tryAttack();

        Direction bestDir = this.getBestDirectionSpawn();

        int[] message = in.messages.readMessage();

        UnitType bestUnitType = this.chooseBestUnitType(message);

        int id = spawnAndGetIdIfPossible(bestDir, bestUnitType);

        if(id != -1 && message[0] != 0) {
            in.messages.sendToLocation(id, message[0], message[1]);
        }

    }

    private UnitType chooseBestUnitType(int[] message) {
        int knights = in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER);
        int soldiers = in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER);
        int archers = in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER);
        int catapults = in.memoryManager.readValue(in.constants.ID_ALLIES_CATAPULT_COUNTER);
        int mages = in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER);

        if(in.helper.intToUnitType(message[2]) == UnitType.WORKER) {
            return UnitType.WORKER;
        }

        if (soldiers < 2 * archers) return UnitType.SOLDIER;

        return UnitType.ARCHER;
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
        int health = 10000;

        for (UnitInfo enemy : in.staticVariables.allenemies) {
            if (enemy.getType() == UnitType.CATAPULT) {
                int enemyhealth = enemy.getHealth();
                if (enemyhealth < health) {
                    health = enemyhealth;
                    bestCatapult = enemy;
                }
            }
            if (enemy.getLocation().distanceSquared(in.staticVariables.myLocation) > 36) {
                outerUnit = enemy;
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

        return in.attack.genericTryAttack(in.staticVariables.allyBase);
    }

}
