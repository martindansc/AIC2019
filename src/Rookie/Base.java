package Rookie;

import aic2019.Direction;
import aic2019.Location;
import aic2019.UnitType;

public class Base {

    private final Injection in;

    Base(Injection in) {
        this.in = in;
    }

    int workers = 0; //DEBUG

    public void run() {

        Direction bestDir = this.getBestDirectionSpawn();

        int[] message = in.messages.readMessage();

        UnitType bestUnitType = this.chooseBestUnitType(message);

        int id = spawnAndGetIdIfPossible(bestDir, bestUnitType);

        if(id != -1 && message[0] != 0) {
            in.messages.sendToLocation(id, message[0], message[1]);
        }

    }

    private UnitType chooseBestUnitType(int[] message) {
        /*int knights = in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER);
        int soldiers = in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER);
        int archers = in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER);
        int catapults = in.memoryManager.readValue(in.constants.ID_ALLIES_CATAPULT_COUNTER);
        int mages = in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER);

        if(in.helper.intToUnitType(message[2]) == UnitType.WORKER) {
            return UnitType.WORKER;
        }

        if (soldiers < archers) return UnitType.SOLDIER;

        return UnitType.ARCHER;
        */
        //DEBUG
        if(workers<3){
            workers++;
            return UnitType.WORKER;
        }
        return UnitType.SOLDIER;
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

}
