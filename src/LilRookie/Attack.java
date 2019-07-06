package LilRookie;

import aic2019.*;

public class Attack {

    private final Injection in;

    public Attack(Injection in) {
        this.in = in;
    }

    public boolean tryAttackBestTarget(Location town)  {
        UnitInfo[] enemies = in.staticVariables.allenemies;
        if(!in.unitController.canAttack() || enemies.length == 0) return false;

        int myAttack = 0;
        boolean aoe = false;

        if (in.staticVariables.type == UnitType.ARCHER) {
            myAttack = GameConstants.ARCHER_ATTACK;
        } else if (in.staticVariables.type == UnitType.BASE) {
            myAttack = GameConstants.BASE_ATTACK;
            //aoe = true;
        } else if (in.staticVariables.type == UnitType.CATAPULT) {
            myAttack = GameConstants.CATAPULT_ATTACK;
        } else if (in.staticVariables.type == UnitType.EXPLORER) {
            myAttack = GameConstants.EXPLORER_ATTACK;
        } else if (in.staticVariables.type == UnitType.KNIGHT) {
            myAttack = GameConstants.KNIGHT_ATTACK;
        } else if (in.staticVariables.type == UnitType.MAGE) {
            myAttack = GameConstants.MAGE_ATTACK;
            aoe = true;
        } else if (in.staticVariables.type == UnitType.SOLDIER) {
            myAttack = GameConstants.SOLDIER_ATTACK;
        } else if (in.staticVariables.type == UnitType.TOWER) {
            myAttack = GameConstants.TOWER_ATTACK;
        }

        if (aoe || in.staticVariables.type == UnitType.CATAPULT) {
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
            if (in.unitController.canAttack(target)) {
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
            in.unitController.attack(killableLoc);
            return true;
        } else if (bestTarget != null) {
            in.unitController.attack(bestLoc);
            return true;
        } else {
            boolean found = false;
            for (TownInfo mytown : in.staticVariables.myTowns) {
                if (mytown.getLocation().isEqual(town)) {
                    found = true;
                    break;
                }
            }
            if (in.unitController.canAttack(town) && !found) {
                in.unitController.attack(town);
            }
        }

        return false;
    }

}