package LilRookie;

import aic2019.*;

public class Messages {
    private final Injection in;

    Messages(Injection in) {
        this.in = in;
    }

    public void sendCreateAndSendToLocation(int unitId, UnitType type, int locX, int locY) {
        int[] params = new int [in.constants.MESSAGE_SIZE];

        params[0] = locX;
        params[1] = locY;

        if(UnitType.WORKER.equals(type)) {
            params[2] = 1;
        }
        else {
            params[2] = 2;
        }

        in.memoryManager.sendMessageTo(unitId, 1, params);
    }

    public void sendCreateAndSendToLocation(int unitId, UnitType type, Location loc) {
        this.sendCreateAndSendToLocation(unitId, type, loc.x, loc.y);
    }


    public void sendToLocation(int unitId, int locX, int locY) {
        int[] params = new int [in.constants.MESSAGE_SIZE];

        params[0] = locX;
        params[1] = locY;

        in.memoryManager.sendMessageTo(unitId, 1, params);
    }

    public void sendToLocation(int unitId, Location loc) {
       this.sendToLocation(unitId, loc.x, loc.y);
    }
}
