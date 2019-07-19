package Master;

import aic2019.Direction;
import aic2019.UnitType;

public class Barracks {

    private final Injection in;

    Barracks(Injection in) {
        this.in = in;
    }

    public void run() {
        Direction bestDir = in.helper.getBestDirectionSpawn();

        int[] message = in.messages.readMessage();
        UnitType bestUnitType = in.memoryManager.getBestUnitType();

        if(bestUnitType != UnitType.BASE) {
            int id = in.helper.spawnAndGetIdIfPossible(bestDir, bestUnitType);

            if(id != -1 && message[0] != 0) {
                in.messages.sendToLocation(id, message[0], message[1]);
            }
        }

        in.helper.addCocoonUnits();
    }

}
