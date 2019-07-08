package Rookie;

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

    public void resetCounter(int key) {
        uc.write(key, 0);
        uc.write(key, 1);
        uc.write(key, 2);
    }


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

    public int readValueThisRound(int key) {
        int realId = key + (uc.getRound())%3;
        return uc.read(realId);
    }

    // DIRECT MESSAGING FUNCTIONS

    public int getMessageId(int unitId) {
        return constants.ID_MESSAGING_BOX + unitId * constants.MESSAGE_SIZE;
    }

    public boolean checkIfMessageInBoxPlayer(int to) {
        return (uc.read(this.getMessageId(to)) != 0);
    }

    public void sendMessageTo(int to, int[] params) {
        if(!checkIfMessageInBoxPlayer(to)) {
            for (int i = 0; i < constants.MESSAGE_SIZE; i++) {
                uc.write(getMessageId(to) + i, params[i]);
            }
        }
    }

    public int[] getNewMessage() {
       return getNewMessageUnit(in.staticVariables.myId);
    }

    public int[] getNewMessageUnit(int unitId) {
        int[] params = new int[constants.MESSAGE_SIZE];
        for(int i = 0; i < constants.MESSAGE_SIZE; i++) {
            int id = getMessageId(unitId) + i;
            params[i] = uc.read(id);
        }

        return params;
    }

    public void clearMessageUnit(int unitId) {
        for(int i = 0; i < constants.MESSAGE_SIZE; i++) {
            int id = getMessageId(unitId) + i;
            uc.write(id, 0);
        }
    }

    public void clearMessageMine() {
        clearMessageUnit(in.staticVariables.myId);
    }


    // UNIT TYPE OBJECTIVE FUNCTIONS

    private int getObjectiveId(int type, int num) {
        // types start at 1
        return in.constants.ID_OBJECTIVES + (type - 1) * UnitType.values().length * in.constants.MAX_OBJECTIVES +
                num * in.constants.OBJECTIVE_SIZE;
    }

    public void addObjective(UnitType unitType, int numberUnitsRequired, int objective, int param1, int param2, int param3) {
        int type = in.helper.unitTypeToInt(unitType);

        for(int i = 0; i < in.constants.MAX_OBJECTIVES; i++) {
            int id = this.getObjectiveId(type, i);
            if(uc.read(id) == 0) {
                uc.write(id, objective);
                uc.write(id + 1, numberUnitsRequired);
                uc.write(id + 2, param1);
                uc.write(id + 3, param2);
                uc.write(id + 4, param3);

                // reset counters
                resetCounter(id + 6);

                break;
            }
        }

    }

    public int[][] getObjectives(UnitType unitType, int objectiveType) {
        int[][] objectives = new int[in.constants.MAX_OBJECTIVES][in.constants.OBJECTIVE_SIZE];

        int type = in.helper.unitTypeToInt(unitType);

        for(int i = 0; i < in.constants.MAX_OBJECTIVES; i++) {
            int id = this.getObjectiveId(type, i);

            int[] objective = new int[in.constants.OBJECTIVE_SIZE];

            int readObjectiveType = uc.read(id);

            if(uc.read(id) == 0 && (objectiveType == -1 || objectiveType == readObjectiveType)) {
                objective[0] = readObjectiveType;
                objective[1] = uc.read(id + 1);
                objective[2] = uc.read(id + 2);
                objective[3] = uc.read(id + 3);
                objective[4] = uc.read(id + 4);
                objective[5] = id;

                // reset counters
                objective[6] = this.readValue(id + 6);
                objective[7] = this.readValueThisRound(id + 6);

            }

            objectives[i] = objective;
        }

        return objectives;
    }

    public int[][] getObjectives(UnitType unitType) {
        return getObjectives(unitType, -1);
    }

    public int[][] getObjectives() {
        return getObjectives(in.staticVariables.type, -1);
    }

    public void removeObjective(int id) {
        uc.write(id, 0);
        uc.write(id + 1, 0);
        uc.write(id + 2, 0);
        uc.write(id + 3, 0);
        uc.write(id + 4, 0);

        // reset counters
        resetCounter(id + 6);
    }

    public void removeObjective(UnitType unitType, int objective, Location loc) {
        int type = in.helper.unitTypeToInt(unitType);

        for(int i = 0; i < in.constants.MAX_OBJECTIVES; i++) {
            int id = this.getObjectiveId(type, i);
            if(uc.read(id + 2) == loc.x && uc.read(id + 3) == loc.y && objective == uc.read(id)) {
                this.removeObjective(id);
                break;
            }
        }
    }


    // LOCATIONS
    public int getNumberOfunitsClaimedLocation() {
        return -1;
    }


    public void claimLocation(Location loc) {

    }

    public void addTimeUnitLocation(int locX, int locY) {
        int times = uc.read(constants.ID_MAP_CLAIMS + locX * 200 + locY);
        times++;
        uc.write(constants.ID_MAP_CLAIMS + locX * 200 + locY, times);
    }

    public int getTimesSendUnitLocation(int locX, int locY) {
        return uc.read(constants.ID_MAP_CLAIMS + locX * 200 + locY);
    }

    public void countUnits() {
        if (in.staticVariables.type == UnitType.SOLDIER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_SOLDIER_COUNTER);
        } else if (in.staticVariables.type == UnitType.ARCHER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_ARCHER_COUNTER);
        } else if (in.staticVariables.type == UnitType.KNIGHT) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_KNIGHT_COUNTER);
        } else if (in.staticVariables.type == UnitType.CATAPULT) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_CATAPULT_COUNTER);
        } else if (in.staticVariables.type == UnitType.MAGE) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_MAGE_COUNTER);
        }
    }

}
