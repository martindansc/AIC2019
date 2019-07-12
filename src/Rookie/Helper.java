package Rookie;

import aic2019.*;

public class Helper {
    private final Injection in;

    Helper(Injection in) {
        this.in = in;
    }

    public int unitTypeToInt(UnitType ut) {
        UnitType[] values = UnitType.values();
        for(int i = 0; i < values.length; i++){
            if(ut == values[i]) return i + 1;
        }

        return -1;
    }

    public UnitType intToUnitType(int type) {
        if(type <= 0) return null;
        UnitType[] values = UnitType.values();
        return values[type - 1];
    }

    public int locationToInt(int locX, int locY) {
        return (locX - in.staticVariables.allyBase.x + 50) * 100 + (locY - in.staticVariables.allyBase.y + 50);
    }

    public int locationToInt(Location loc) {
        return locationToInt(loc.x, loc.y);
    }

    public Location intToLocation(int number) {
        return new Location((number / 100) + in.staticVariables.allyBase.x - 50, (number % 100) + in.staticVariables.allyBase.y - 50);
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
        } else if (in.staticVariables.type == UnitType.WORKER) {
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_WORKERS_COUNTER);
        }
    }
}
