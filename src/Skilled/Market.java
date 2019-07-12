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
        Resource maxFree;
        float maxFreeFloat;

        if (freewood > freecrystal && freewood > freeiron) {
            maxFree = Resource.WOOD;
            maxFreeFloat = freewood;
        } else if (freeiron > freecrystal) {
            maxFree = Resource.IRON;
            maxFreeFloat = freeiron;
        } else {
            maxFree = Resource.CRYSTAL;
            maxFreeFloat = freecrystal;
        }

        if (freewood >= 0 && freeiron >= 0 && freecrystal >= 0) return true;
        if (freewood < 0 && freeiron < 0 && freecrystal < 0) return false;

        float crystalwood = in.unitController.tradeOutput(Resource.CRYSTAL, Resource.WOOD, 1);
        float crystaliron = in.unitController.tradeOutput(Resource.CRYSTAL, Resource.IRON, 1);
        float woodiron = in.unitController.tradeOutput(Resource.WOOD, Resource.IRON, 1);

        if (freewood < 0) {
            if (-freewood > freecrystal * crystalwood + freeiron * 1/woodiron) return false;
            if (freeiron < 0) {
                in.unitController.trade(Resource.CRYSTAL, Resource.WOOD, freecrystal);
                return false;
            } else if (freecrystal < 0) {
                in.unitController.trade(Resource.IRON, Resource.WOOD, freeiron);
                return false;
            } else {
                in.unitController.trade(maxFree, Resource.WOOD, maxFreeFloat);
                if (in.unitController.getWood() > woodNeeded) {
                    return true;
                }
            }
        } else if (freeiron < 0) {
            if (-freeiron > freecrystal * crystaliron + freewood * woodiron) return false;
            if (freecrystal < 0) {
                in.unitController.trade(Resource.WOOD, Resource.IRON, freewood);
                return false;
            } else {
                in.unitController.trade(maxFree, Resource.IRON, maxFreeFloat);
                if (in.unitController.getIron() > ironNeeded) {
                    return true;
                }
            }
        } else {
            if (-freecrystal > freeiron * 1/crystaliron + freewood * 1/crystalwood) return false;
            else {
                in.unitController.trade(maxFree, Resource.CRYSTAL, maxFreeFloat);
                if (in.unitController.getCrystal() > crystalNeeded) {
                    return true;
                }
            }
        }

        return false;
    }
}
