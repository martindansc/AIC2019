package LilRookie;

import aic2019.*;

public class Base {

    private final Injection in;

    Base(Injection in) {
        this.in = in;
    }

    public void run() {

        Direction bestDir = this.getBestDirectionSpawn();

        int[] message = in.memoryManager.getNewMessage(1);

        UnitType bestUnitType = this.chooseBestUnitType(message);
        int id = spawnAndGetIdIfPossible(bestDir, bestUnitType);

        if(id != -1 && message[0] != 0) {
            in.messages.sendToLocation(id, message[0], message[1]);
            in.memoryManager.clearMessageMine(1);
        }

    }

    private UnitType chooseBestUnitType(int[] message) {

        if(message[2] == 1) {
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
