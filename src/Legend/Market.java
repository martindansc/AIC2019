package Legend;

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

        float woodNeeded = unit.getWoodCost();
        float ironNeeded = unit.getIronCost();
        float crystalNeeded = unit.getCrystalCost();

        float freewood = in.staticVariables.wood - woodNeeded;
        float freeiron = in.staticVariables.iron - ironNeeded;
        float freecrystal = in.staticVariables.crystal - crystalNeeded;


        if (freewood >= 0 && freeiron >= 0 && freecrystal >= 0) {
            in.unitController.trade(Resource.CRYSTAL, Resource.WOOD, freecrystal);
            return true;
        }
        if (freewood < 0 && freeiron < 0 && freecrystal < 0) return false;

        if (freewood < 0) {
            if(freecrystal > -freewood * in.staticVariables.woodcrystal) {
                in.unitController.trade(Resource.CRYSTAL, Resource.WOOD, freecrystal);
            }
            else if(freeiron > -freewood * in.staticVariables.woodiron) {
                in.unitController.trade(Resource.IRON, Resource.WOOD, -freewood * in.staticVariables.woodiron);
            }

        }
        else if (freeiron < 0) {
            if(freecrystal > -freeiron * in.staticVariables.ironcrystal) {
                in.unitController.trade(Resource.CRYSTAL, Resource.IRON, freecrystal);
            }
            else if(freewood > -freeiron / in.staticVariables.woodiron) {
                in.unitController.trade(Resource.WOOD, Resource.IRON, -freeiron / in.staticVariables.woodiron);
            }

        }

        else if(freecrystal < 0) {
            if(freewood > -freecrystal / in.staticVariables.woodcrystal) {
                in.unitController.trade(Resource.WOOD, Resource.CRYSTAL, -freecrystal / in.staticVariables.woodcrystal);
            }
            else if(freeiron > -freecrystal / in.staticVariables.ironcrystal) {
                in.unitController.trade(Resource.IRON, Resource.CRYSTAL, -freecrystal / in.staticVariables.ironcrystal);
            }

        }

        return unit.getCrystalCost() <= in.unitController.getCrystal() &&
                unit.getWoodCost() <= in.unitController.getWood() &&
                unit.getIronCost() <= in.unitController.getIron();
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
