package LilRookie;

import aic2019.*;

public class MemoryManager {

    private final UnitController uc;
    private final Constants constants = new Constants();
    private final Injection in;

    // GENERAL

    public MemoryManager(Injection in) {
        this.in = in;
        this.uc = in.unitController;
    }

    // HELPERS


    // SIMPLE COUNTERS FUNCTION


    public void increaseValue(int key, int ammount) {
        int realId = key + uc.getRound()%3;
        int value = uc.read(realId);
        uc.write(realId, value + ammount);
    }

    public void increaseValueByOne(int key) {
        this.increaseValue(key, 1);
    }

    public int readValue(int key) {
        uc.write(key + (uc.getRound() + 1)%3, 0);
        int realId = key + (uc.getRound() - 1)%3;
        return uc.read(realId);
    }

    // MESSAGING FUNCTIONS

    public int getMessageId(int unitId,int type) {
        return constants.ID_MESSAGING_BOX + unitId * constants.MAX_TYPE_MESSAGES + type * constants.MESSAGE_SIZE;
    }

    public boolean checkIfMessageInBoxPlayer(int to, int type) {
        return (uc.read(this.getMessageId(to, type)) != 0);
    }

    public void sendMessageTo(int to, int type, int[] params) {
        if(!checkIfMessageInBoxPlayer(to, type)) {
            for (int i = 0; i < constants.MESSAGE_SIZE; i++) {
                uc.write(getMessageId(to, type) + i, params[i]);
            }
        }
    }

    public int[] getNewMessage(int type) {
       return getNewMessageUnit(in.staticVariables.myId, type);
    }

    public int[] getNewMessageUnit(int unitId, int type) {
        int[] params = new int[constants.MESSAGE_SIZE];
        for(int i = 0; i < constants.MESSAGE_SIZE; i++) {
            int id = getMessageId(unitId, type) + i;
            params[i] = uc.read(id);
        }

        return params;
    }

    public void clearMessageUnit(int unitId, int type) {
        for(int i = 0; i < constants.MESSAGE_SIZE; i++) {
            int id = getMessageId(unitId, type) + i;
            uc.write(id, 0);
        }
    }

    public void clearMessageMine(int type) {
        clearMessageUnit(in.staticVariables.myId, type);
    }

    public void addTimeUnitLocation(int locX, int locY) {
        int times = uc.read(constants.ID_MAP_CLAIMS + locX * 200 + locY);
        times++;
        uc.write(constants.ID_MAP_CLAIMS + locX * 200 + locY, times);
    }

    public int getTimesSendUnitLocation(int locX, int locY) {
        return uc.read(constants.ID_MAP_CLAIMS + locX * 200 + locY);
    }

}
