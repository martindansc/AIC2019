package LilRookie;

import aic2019.*;

public class Attack {

    private MemoryManager manager;
    private UnitController uc;

    public Attack(MemoryManager manager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;
    }

    public boolean tryAttackBestTarget()  {
        UnitInfo[] enemies = manager.enemies;
        if(!uc.canAttack() || enemies.length == 0) return false;

        int myAttack = 0;
        boolean aoe = false;

        if (manager.type == UnitType.ARCHER) {
            myAttack = GameConstants.ARCHER_ATTACK;
        } else if (manager.type == UnitType.BASE) {
            myAttack = GameConstants.BASE_ATTACK;
            aoe = true;
        } else if (manager.type == UnitType.CATAPULT) {
            myAttack = GameConstants.CATAPULT_ATTACK;
        } else if (manager.type == UnitType.EXPLORER) {
            myAttack = GameConstants.EXPLORER_ATTACK;
        } else if (manager.type == UnitType.KNIGHT) {
            myAttack = GameConstants.KNIGHT_ATTACK;
        } else if (manager.type == UnitType.MAGE) {
            myAttack = GameConstants.MAGE_ATTACK;
            aoe = true;
        } else if (manager.type == UnitType.SOLDIER) {
            myAttack = GameConstants.SOLDIER_ATTACK;
        } else if (manager.type == UnitType.TOWER) {
            myAttack = GameConstants.TOWER_ATTACK;
        }

        if (aoe || manager.type == UnitType.CATAPULT) {
            return false;
        }

        UnitInfo bestTarget = null;
        int bestTargetHealth = 10000;
        UnitInfo killableTarget = null;
        int killableTargetHealth = 0;
        Location bestLoc = null;
        Location killableLoc = null;

        for (UnitInfo unit : enemies) {
            Location target = unit.getLocation();
            if (uc.canAttack(target)) {
                int health = unit.getHealth();
                if (bestTargetHealth > health) {
                    bestTarget = unit;
                    bestTargetHealth = health;
                    bestLoc = target;
                }
                if (myAttack >= health && killableTargetHealth < health) {
                    killableTarget = unit;
                    killableTargetHealth = health;
                    killableLoc = target;
                }
            }
        }

        if (killableTarget != null) {
            uc.attack(killableLoc);
            return true;
        } else if (bestTarget != null) {
            uc.attack(bestLoc);
            return true;
        }

        return false;
    }

}