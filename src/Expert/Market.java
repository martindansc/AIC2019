package Expert;

import aic2019.GameConstants;
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

        if (freewood < 0) {
            if (-freewood > freecrystal * in.staticVariables.woodcrystal + freeiron * 1/in.staticVariables.woodiron) return false;
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
            if (-freeiron > freecrystal * in.staticVariables.ironcrystal + freewood * in.staticVariables.woodiron) return false;
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
            if (-freecrystal > freeiron * 1/in.staticVariables.ironcrystal + freewood * 1/in.staticVariables.woodcrystal) return false;
            else {
                in.unitController.trade(maxFree, Resource.CRYSTAL, maxFreeFloat);
                if (in.unitController.getCrystal() > crystalNeeded) {
                    return true;
                }
            }
        }

        return false;
    }

    public UnitType heuristic() {
        int knights = in.memoryManager.readValue(in.constants.ID_ALLIES_KNIGHT_COUNTER);
        int soldiers = in.memoryManager.readValue(in.constants.ID_ALLIES_SOLDIER_COUNTER);
        int archers = in.memoryManager.readValue(in.constants.ID_ALLIES_ARCHER_COUNTER);
        int mages = in.memoryManager.readValue(in.constants.ID_ALLIES_MAGE_COUNTER);
        int total = knights + soldiers + archers + mages + 1;

        float soldierResources = GameConstants.SOLDIER_WOOD_COST + GameConstants.SOLDIER_IRON_COST * 1/in.staticVariables.woodiron;
        float knightResources = GameConstants.KNIGHT_WOOD_COST + GameConstants.KNIGHT_IRON_COST * 1/in.staticVariables.woodiron;
        float archerResources = GameConstants.ARCHER_WOOD_COST + GameConstants.ARCHER_IRON_COST * 1/in.staticVariables.woodiron;
        float mageResources = GameConstants.MAGE_WOOD_COST + GameConstants.MAGE_IRON_COST * 1/in.staticVariables.woodiron + GameConstants.MAGE_CRYSTAL_COST * 1/in.staticVariables.woodcrystal;

        float soldierScore = (1 - (float)(soldiers + knights)/total) * (in.constants.HEU_SOLDIER / soldierResources);
        float knightScore =  (1 - (float)(soldiers + knights)/total) * (in.constants.HEU_KNIGHT / knightResources);
        float archerScore =  (1 - (float)(archers + mages)/total) * (in.constants.HEU_ARCHER / archerResources);
        float mageScore =  (1 - (float)(archers + mages)/total) * (in.constants.HEU_MAGE / mageResources);

        UnitType bestType;
        if (soldierScore >= knightScore && soldierScore >= archerScore && soldierScore > mageScore) bestType = UnitType.SOLDIER;
        else if (knightScore >= archerScore && knightScore > mageScore) bestType = UnitType.KNIGHT;
        else if (archerScore > mageScore) bestType = UnitType.ARCHER;
        else bestType = UnitType.MAGE;

        return bestType;
    }

}
