package Rookie;

import aic2019.*;

public class Helper {
    private final Injection in;

    Helper(Injection in) {
        this.in = in;
    }

    public int unitTypeToInt(UnitType ut) {
        if(ut == UnitType.WORKER) {
            return 1;
        }

        return  2;
    }

    public UnitType intToUnitType(int type) {

        if(type == 1) {
            return UnitType.WORKER;
        }

        return UnitType.ARCHER;
    }

    public int LocationToInt(Location loc) {
        return loc.x * 100 + loc.y;
    }

    public Location IntToLocation(int number) {
        return new Location(number / 100, number % 100);
    }
}
