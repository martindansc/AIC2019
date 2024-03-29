package Expert;

import aic2019.Location;
import aic2019.UnitController;
import aic2019.UnitType;

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
        for(int i = 0; i < in.constants.COUNTERS_SPACE; i++) {
            uc.write(key + i, 0);
        }
    }

    public void roundClearCounter(int key) {
        uc.write(key + (uc.getRound() + 1)%in.constants.COUNTERS_SPACE, 0);
    }

    public void increaseValue(int key, int ammount) {
        this.roundClearCounter(key);
        int realId = key + uc.getRound()%in.constants.COUNTERS_SPACE;
        int value = uc.read(realId);
        uc.write(realId, value + ammount);
    }

    public void increaseValueByOne(int key) {
        this.increaseValue(key, 1);
    }

    public int readValue(int key) {
        this.roundClearCounter(key);
        int realId = key + (uc.getRound() - 1)%in.constants.COUNTERS_SPACE;
        int realIdThisRound = key + (uc.getRound())%in.constants.COUNTERS_SPACE;
        return Math.max(uc.read(realId), uc.read(realIdThisRound));
    }

    public int readValueThisRound(int key) {
        int realId = key + (uc.getRound())%in.constants.COUNTERS_SPACE;
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

    private int[] newEmptyObjective() {
        return new int[in.constants.OBJECTIVE_SIZE];
    }

    private int getObjectiveId(int type, int num) {
        // types start at 1
        return in.constants.ID_OBJECTIVES + (type - 1) * in.constants.OBJECTIVE_SIZE* in.constants.MAX_OBJECTIVES +
                num * in.constants.OBJECTIVE_SIZE;
    }

    private void updateObjective(int id, int[] params) {
        if(params[0] != 0) uc.write(id, params[0]);
        if(params[1] != 0) uc.write(id + 1, params[1]);
        if(params[2] != 0) uc.write(id + 2, params[2]);
        if(params[3] != 0) uc.write(id + 3, params[3]);
        if(params[4] != 0) uc.write(id + 4, params[4]);

        if(params[9] != 0) uc.write(id + 9, params[9]);
        if(params[10] != 0) uc.write(id + 10, params[10]);
        if(params[11] != 0) uc.write(id + 11, params[11]);
        if(params[12] != 0) uc.write(id + 12, params[12]);
    }

    private void insertObjective(int id, int[] params) {
        uc.write(id, params[0]);
        uc.write(id + 1, params[1]);
        uc.write(id + 2, params[2]);
        uc.write(id + 3, params[3]);
        uc.write(id + 4, params[4]);

        // reset counters
        resetCounter(id + 6);

        // add location objective
        this.setObjectiveIdInLocation(params[2], params[3], id);

        uc.write(id + 9, params[9]);
        uc.write(id + 10, params[10]);
        uc.write(id + 11, params[11]);
        uc.write(id + 12, params[12]);
    }

    public int[] addObjective(UnitType unitType, int[] params) {

        if(uc.getEnergyLeft() < 2000) return this.newEmptyObjective();

        //todo: check that the objective doesn't exists, if it does maybe update it
        int maybeId = this.getObjectiveIdInLocation(params[2], params[3]);
        if(maybeId > 0) {
            return this.getObjective(maybeId, -1);
        }

        int worstObjective = 0;
        int worstObjectiveValue = 0;

        int type = in.helper.unitTypeToInt(unitType);

        int lastId = -1;

        for(int i = 0; i < in.constants.MAX_OBJECTIVES; i++) {
            int id = this.getObjectiveId(type, i);
            if(uc.read(id) == 0) {
                this.insertObjective(id, params);
                lastId = id;
                break;
            }
            else {
                int value = uc.read(id + 11);
                if (value > worstObjectiveValue){
                    worstObjective = uc.read(id + 5);
                    worstObjectiveValue = value;
                }
            }
        }

        if(lastId == -1 && worstObjective != 0 && params[11] < worstObjective) {
            this.removeObjectiveIdInLocation(uc.read(worstObjective + 2), uc.read(worstObjective + 3));
            this.insertObjective(worstObjective, params);
            lastId = worstObjective;
        }

        if(lastId == -1) return this.newEmptyObjective();

        return this.getObjective(lastId, -1);

    }

    public int[] getObjective(int id, int objectiveType) {
        int[] objective = this.newEmptyObjective();

        int readObjectiveType = uc.read(id);

        if(uc.read(id) != 0 && (objectiveType == -1 || objectiveType == readObjectiveType)) {
            objective[0] = readObjectiveType;
            objective[1] = uc.read(id + 1);
            objective[2] = uc.read(id + 2);
            objective[3] = uc.read(id + 3);
            objective[4] = uc.read(id + 4);
            objective[5] = id;

            // reset counters
            objective[6] = this.readValue(id + 6);
            objective[7] = this.readValueThisRound(id + 6);

            objective[9] = uc.read(id + 9);
            objective[10] = uc.read(id + 10);

            // value
            objective[11] = uc.read(id + 11);

            objective[12] = uc.read(id + 12);

        }

        return objective;
    }

    public int[] getObjective(Location loc) {
        int id = this.getObjectiveIdInLocation(loc);
        return getObjective(id,-1);
    }

    public int[][] getObjectives(UnitType unitType, int objectiveType) {
        int[][] objectives = new int[in.constants.MAX_OBJECTIVES][in.constants.OBJECTIVE_SIZE];

        int type = in.helper.unitTypeToInt(unitType);

        for(int i = 0; i < in.constants.MAX_OBJECTIVES; i++) {
            int id = this.getObjectiveId(type, i);
            objectives[i] = this.getObjective(id, objectiveType);
        }

        return objectives;
    }

    public int[][] getObjectives(UnitType unitType) {
        return this.getObjectives(unitType, -1);
    }

    public int[][] getObjectives() {
        return this.getObjectives(in.staticVariables.type, -1);
    }

    public void removeObjective(int id) {
        // remove location
        this.removeObjectiveIdInLocation(uc.read(id + 2), uc.read(id + 3));

        for(int i = 0; i < in.constants.OBJECTIVE_SIZE; i++) {
            uc.write(id + i, 0);
        }
    }

    public void removeObjective(Location loc) {
        int idObjectiveLocation = in.helper.locationToInt(loc.x, loc.y);
        if (idObjectiveLocation == 0) return;
        removeObjective(idObjectiveLocation);
    }

    public boolean existsObjectiveInLocation(int locX, int locY) {
        int idObjectiveLocation = in.helper.locationToInt(locX, locY);
        if (idObjectiveLocation == 0) return false;
        return 0 < uc.read(in.constants.ID_LOCATION_OBJECTIVES + idObjectiveLocation);
    }

    public int getObjectiveIdInLocation(int locX, int locY) {
        int idObjectiveLocation = in.helper.locationToInt(locX, locY);
        return uc.read(in.constants.ID_LOCATION_OBJECTIVES + idObjectiveLocation);
    }

    private void setObjectiveIdInLocation(int locX, int locY, int id) {
        int idObjectiveLocation = in.helper.locationToInt(locX, locY);
        uc.write(in.constants.ID_LOCATION_OBJECTIVES  + idObjectiveLocation, id);
    }

    private void removeObjectiveIdInLocation(int locX, int locY) {
        int idObjectiveLocation = in.helper.locationToInt(locX, locY);
        uc.write(in.constants.ID_LOCATION_OBJECTIVES  + idObjectiveLocation, 0);
    }

    public int getObjectiveIdInLocation(Location loc) {
        int idObjectiveLocation = in.helper.locationToInt(loc);
        return uc.read(in.constants.ID_LOCATION_OBJECTIVES + idObjectiveLocation);
    }

    public int getObjectiveType(int id) {
        return uc.read(id);
    }

    public void claimObjective(int idObjective) {
        uc.write(idObjective + 10, in.staticVariables.myId);
        this.increaseValueByOne(idObjective + 6);
    }

    public void updateRoundAction(int idObjective) {
        uc.write(idObjective + 12, in.staticVariables.round);
    }

    // MAP FUNCTIONS

    public void clearUnitMap(Location loc) {
        int index = getIndexMap(loc);
        in.unitController.write(index, 0);
    }

    public void saveUnitToMap(Location loc, UnitType unit) {
        int index = getIndexMap(loc);
        in.unitController.write(index, in.helper.unitTypeToInt(unit));
    }

    public UnitType getUnitFromLocation(Location loc) {
        int index = getIndexMap(loc);
        return in.helper.intToUnitType(in.unitController.read(index));
    }

    public int getIndexMap(int locX, int locY) {
        return in.constants.ID_MAP_INFO + in.helper.locationToInt(locX, locY) * in.constants.INFO_PER_CELL;
    }

    public int getIndexMap(Location loc) {
        return in.constants.ID_MAP_INFO + in.helper.locationToInt(loc) * in.constants.INFO_PER_CELL;
    }

    public void setLocationSafe(Location loc) {
        int index = getIndexMap(loc);
        in.unitController.write(index + 4, in.unitController.read(index + 4) - 1);
    }

    public void setLocationDangerous(int locX, int locY) {
        int index = getIndexMap(locX, locY);
        in.unitController.write(index + 4, in.unitController.read(index + 4) + 1);
    }

    public boolean getPaintedTower(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        return in.unitController.read(index + 3) == 0;
    }

    public void markTowerForPainting(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index + 3, 1);
    }

    public void unmarkTowerForPainting(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index + 3, 0);
    }

    public void setLocationDangerous(Location loc) {
        setLocationDangerous(loc.x, loc.y);
    }

    public boolean isLocationSafe(Location loc) {
        int index = getIndexMap(loc);
        int enemies = in.unitController.read(index + 4);
        return enemies <= 0;
    }

    public boolean isTownExplored(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        return in.unitController.read(index) > 0;
    }

    public void markTownAsExplored(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index, 1);
    }

    public void setTownScore(Location loc, int score) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index + 1, score);
    }

    public int getTownScore(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        return in.unitController.read(index + 1);
    }

    public void setClaimed(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index + 2, in.constants.CLAIMED_TOWN);
    }

    public void setStolen(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index + 2, in.constants.STOLEN_TOWN);
    }

    public void setClaimedEnemy(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        in.unitController.write(index + 2, in.constants.ENEMY_TOWN);
    }

    public int getTownStatus(Location loc) {
        int index = getIndexMap(loc.x, loc.y);
        return in.unitController.read(index + 2);
    }

}
