package Rookie;

import aic2019.UnitType;

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
}
