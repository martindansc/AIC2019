package LilRookie;

import aic2019.*;

public class Attack {

    private final Injection in;
    private StaticVariables variables;
    private UnitController uc;

    public Attack(Injection in) {
        this.in = in;
        this.variables = in.staticVariables;
        uc = variables.uc;
    }

    public boolean tryAttackBestTarget(Location town)  {
        UnitInfo[] enemies = variables.allenemies;
        if(!uc.canAttack() || enemies.length == 0) return false;

        int myAttack = 0;
        boolean aoe = false;

        if (variables.type == UnitType.ARCHER) {
            myAttack = GameConstants.ARCHER_ATTACK;
        } else if (variables.type == UnitType.BASE) {
            myAttack = GameConstants.BASE_ATTACK;
            aoe = true;
        } else if (variables.type == UnitType.CATAPULT) {
            myAttack = GameConstants.CATAPULT_ATTACK;
        } else if (variables.type == UnitType.EXPLORER) {
            myAttack = GameConstants.EXPLORER_ATTACK;
        } else if (variables.type == UnitType.KNIGHT) {
            myAttack = GameConstants.KNIGHT_ATTACK;
        } else if (variables.type == UnitType.MAGE) {
            myAttack = GameConstants.MAGE_ATTACK;
            aoe = true;
        } else if (variables.type == UnitType.SOLDIER) {
            myAttack = GameConstants.SOLDIER_ATTACK;
        } else if (variables.type == UnitType.TOWER) {
            myAttack = GameConstants.TOWER_ATTACK;
        }

        if (aoe || variables.type == UnitType.CATAPULT) {
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
        } else {
            boolean found = false;
            for (TownInfo mytown : variables.myTowns) {
                if (mytown.getLocation().isEqual(town)) {
                    found = true;
                    break;
                }
            }
            if (uc.canAttack(town) && !found) {
                uc.attack(town);
            }
        }

        return false;
    }

}