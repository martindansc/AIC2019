package Rookie;

import aic2019.*;

public class Attack {

    private final Injection in;

    public Attack(Injection in) {
        this.in = in;
    }

    public boolean tryAttackBestTarget(Location town)  {
        UnitInfo[] enemies = in.staticVariables.allenemies;
        if(!in.unitController.canAttack()) return false;
        if (enemies.length == 0 && town.isEqual(in.staticVariables.myLocation)) return false;

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

        if (in.staticVariables.type == UnitType.MAGE) {
            Location[] locs = in.unitController.getVisibleLocations(5);
            int[] scores = new int[locs.length];
            for (int i = 0; i < locs.length; i++) {
                int distance = locs[i].distanceSquared(in.staticVariables.myLocation);
                if (distance > 2 && in.unitController.canAttack(locs[i])) {
                    for (Direction dir : in.staticVariables.dirs) {
                        Location target = locs[i].add(dir);
                        if (in.unitController.canSenseLocation(target)) {
                            UnitInfo unit = in.unitController.senseUnit(target);
                            TownInfo city = in.unitController.senseTown(target);
                            if (unit != null) {
                                if (unit.getTeam() == in.staticVariables.allies) {
                                    scores[i]--;
                                } else {
                                    scores[i]++;
                                }
                            } else if (city != null){
                                if (city.getOwner() == in.staticVariables.allies) {
                                    scores[i]--;
                                } else {
                                    scores[i]++;
                                }
                            }
                        }
                    }
                }
            }

            int index = -1;
            int bestscore = 0;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] > bestscore) {
                    index = i;
                    bestscore = scores[i];
                }
            }

            if (index != -1) {
                in.unitController.attack(locs[index]);
                return true;
            }
        } else {

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
                if (in.unitController.canAttack(town) && in.unitController.senseTown(town).getOwner() != in.staticVariables.allies) {
                    in.unitController.attack(town);
                    return true;
                }
            }
        }

        return false;
    }

}