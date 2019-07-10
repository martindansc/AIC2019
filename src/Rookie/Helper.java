package Rookie;

import aic2019.*;

public class Helper {
    private final Injection in;

    Helper(Injection in) {
        this.in = in;
    }

    public int unitTypeToInt(UnitType ut) {
        if (ut == UnitType.WORKER) {
            return 1;
        } else if (ut == UnitType.TOWER) {
            return 2;
        }

        return 3;
    }

    public UnitType intToUnitType(int type) {

        if (type == 1) {
            return UnitType.WORKER;
        } else if (type == 2) {
            return UnitType.TOWER;
        }

        return UnitType.ARCHER;
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
}
