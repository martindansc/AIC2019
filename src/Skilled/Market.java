package Skilled;

import aic2019.Resource;
import aic2019.UnitType;

public class Market {
    private Injection in;

    public Market(Injection in) {
        this.in = in;
    }

    public boolean canBuild(UnitType unit) {
        if (!in.unitController.canTrade()) return false;

        float woodNeeded = unit.woodCost;
        float ironNeeded = unit.ironCost;
        float crystalNeeded = unit.crystalCost;

        float freewood = in.staticVariables.wood - woodNeeded;
        float freeiron = in.staticVariables.iron - ironNeeded;
        float freecrystal = in.staticVariables.crystal - crystalNeeded;

        if (freewood >= 0 && freeiron >= 0 && freecrystal >= 0) return true;
        if (freewood < 0 && freeiron < 0 && freecrystal < 0) return false;

        float crystalwood = in.unitController.tradeOutput(Resource.CRYSTAL, Resource.WOOD, 1);
        float crystaliron = in.unitController.tradeOutput(Resource.CRYSTAL, Resource.IRON, 1);
        float woodiron = in.unitController.tradeOutput(Resource.WOOD, Resource.IRON, 1);

        // Supone que no se crean magos (no se usa el cristal)

        if (freeiron < 0 && freewood >= 0) {
            if (-freeiron > freecrystal * crystaliron + freewood * woodiron) return false;
            else {
                in.unitController.trade(Resource.CRYSTAL, Resource.IRON, freecrystal);
                in.unitController.trade(Resource.WOOD, Resource.IRON, freewood);
                return true;
            }
        } else if (freeiron >= 0 && freewood < 0) {
            if (-freewood > freecrystal * crystalwood + freeiron * 1 / woodiron) return false;
            else {
                in.unitController.trade(Resource.CRYSTAL, Resource.WOOD, freecrystal);
                in.unitController.trade(Resource.IRON, Resource.WOOD, freeiron);
                return true;
            }
        }
        return false;
    }
}
