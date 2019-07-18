package Master;

import aic2019.*;

public class Attack {

    private final Injection in;

    public Attack(Injection in) {
        this.in = in;
    }

    public int getMyAttack() {
        int myAttack = 0;

        if (in.staticVariables.type == UnitType.ARCHER) {
            myAttack = GameConstants.ARCHER_ATTACK;
        } else if (in.staticVariables.type == UnitType.BASE) {
            myAttack = GameConstants.BASE_ATTACK;
        } else if (in.staticVariables.type == UnitType.CATAPULT) {
            myAttack = GameConstants.CATAPULT_ATTACK;
        } else if (in.staticVariables.type == UnitType.EXPLORER) {
            myAttack = GameConstants.EXPLORER_ATTACK;
        } else if (in.staticVariables.type == UnitType.KNIGHT) {
            myAttack = GameConstants.KNIGHT_ATTACK;
        } else if (in.staticVariables.type == UnitType.MAGE) {
            myAttack = GameConstants.MAGE_ATTACK;
        } else if (in.staticVariables.type == UnitType.SOLDIER) {
            myAttack = GameConstants.SOLDIER_ATTACK;
        } else if (in.staticVariables.type == UnitType.TOWER) {
            myAttack = GameConstants.TOWER_ATTACK;
        }

        return myAttack;
    }

    public boolean canAttackTarget(Location target) {
        int distance = in.staticVariables.myLocation.distanceSquared(target);
        if (in.staticVariables.type.getAttackRangeSquared() < distance) return false;
        if (in.staticVariables.type.getMinAttackRangeSquared() > distance) return false;
        return true;
    }

    public boolean genericTryAttack()  {
        if (!in.unitController.canAttack()) return false;
        UnitInfo[] enemies = in.staticVariables.allenemies;
        if (enemies.length == 0) return false;

        int myAttack = getMyAttack();

        UnitInfo bestTarget = null;
        int bestTargetHealth = 10000;
        Location bestLoc = null;
        UnitInfo killableTarget = null;
        int killableTargetHealth = 0;
        Location killableLoc = null;
        UnitInfo killableEnemy = null;
        int killableEnemyHealth = 0;
        Location killableEnemyLoc = null;
        UnitInfo bestEnemy = null;
        int bestTargetEnemy = 10000;
        Location bestEnemyLoc = null;

        for (UnitInfo unit : enemies) {
            Location target = unit.getLocation();
            if (unit.getType() == UnitType.TOWER) {
                in.map.markTower(target, unit.getTeam() != in.staticVariables.opponent);
            }
            if (in.unitController.canAttack(target)) {
                int health = unit.getHealth();
                boolean teamEnemy = unit.getTeam() == in.staticVariables.opponent;
                if (bestTargetHealth > health) {
                    bestTarget = unit;
                    bestTargetHealth = health;
                    bestLoc = target;
                }
                if (teamEnemy && bestTargetEnemy < health) {
                    bestEnemy = unit;
                    bestTargetEnemy = health;
                    bestEnemyLoc = target;
                }
                if (myAttack >= health) {
                    if (killableTargetHealth < health) {
                        killableTarget = unit;
                        killableTargetHealth = health;
                        killableLoc = target;
                    }
                    if (teamEnemy && killableEnemyHealth < health) {
                        killableEnemy = unit;
                        killableEnemyHealth = health;
                        killableEnemyLoc = target;
                    }
                }
            }
        }

        if (killableEnemy != null) {
            in.unitController.attack(killableEnemyLoc);
            return true;
        } else if (bestEnemy != null) {
            in.unitController.attack(bestEnemyLoc);
            return true;
        } else if (killableTarget != null) {
            in.unitController.attack(killableLoc);
            return true;
        } else if (bestTarget != null) {
            in.unitController.attack(bestLoc);
            return true;
        }

        return false;
    }

    public void genericTryAttackTown(Location town) {
        if (!in.staticVariables.allyBase.isEqual(town)) {
            if (in.unitController.canAttack(town) && in.unitController.senseTown(town).getOwner() != in.staticVariables.allies) {
                in.unitController.attack(town);
                return;
            }
        }
        for (TownInfo enemyTown: in.staticVariables.allenemytowns) {
            Location townLoc = enemyTown.getLocation();
            if (in.unitController.canAttack(townLoc)) {
                in.unitController.attack(townLoc);
                return;
            }
        }
    }

}