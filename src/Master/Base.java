package Master;

import aic2019.*;

public class Base {

    private final Injection in;

    Base(Injection in) {
        this.in = in;
    }

    public void run() {
        if (in.staticVariables.round == 1) init();
        tryAttack();
        Direction bestDir = in.helper.getBestDirectionSpawn();
        int[] message = in.messages.readMessage();

        UnitType bestUnitType = in.helper.chooseBestUnitType(message);
        in.memoryManager.setBestUnitType(bestUnitType);

        if(bestUnitType != UnitType.BASE) {
            if (in.market.canBuild(bestUnitType) && bestUnitType!=UnitType.TOWER) {
                int id = in.helper.spawnAndGetIdIfPossible(bestDir, bestUnitType);

                if(id != -1 && message[0] != 0) {
                    in.messages.sendToLocation(id, message[0], message[1]);
                }
            }
        }
        in.helper.addCocoonUnits();
    }

    private void init() {
        for (ResourceInfo resource: in.staticVariables.resourcesSeen) {
            Location loc = resource.getLocation();
            int distance = in.staticVariables.myLocation.distanceSquared(loc);
            if (in.memoryManager.isLocationSafe(loc)) {
                if (distance < 3) {
                    in.map.markSafeResource(loc);
                    in.memoryManager.increaseSafeResources();
                } else if (!in.unitController.isObstructed(loc, in.staticVariables.myLocation)) {
                    in.map.markSafeResource(loc);
                    in.memoryManager.increaseSafeResources();
                }
            }
        }
    }

    public boolean genericTryAttack()  {
        if (!in.unitController.canAttack()) return false;
        UnitInfo[] enemies = in.staticVariables.allenemies;
        if (enemies.length == 0) return false;

        int myAttack = in.attack.getMyAttack();

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
            aoeAttack(killableEnemyLoc);
            return true;
        } else if (bestEnemy != null) {
            aoeAttack(bestEnemyLoc);
            return true;
        } else if (killableTarget != null) {
            aoeAttack(killableLoc);
            return true;
        } else if (bestTarget != null) {
            aoeAttack(bestLoc);
            return true;
        }

        return false;
    }

    public void aoeAttack(Location target){
        Location bestTargetLoc = null;
        int bestTargetScore = 0;

        for (Direction dir : in.staticVariables.dirs) {
            Location targetLoc = target.add(dir);

            if (in.unitController.canAttack(targetLoc)) {
                UnitInfo[] unitsInRange = in.unitController.senseUnits(targetLoc, 2);
                int score = 0;

                for (UnitInfo uir : unitsInRange){
                    Team team = uir.getTeam();
                    UnitType type = uir.getType();
                    if (team == in.staticVariables.allies){
                        if (type == UnitType.BASE) score -= 10;
                        else if (type == UnitType.TOWER) score -= 5;
                        else if (type == UnitType.MAGE) score -= 4;
                        else if (uir.getHealth() <= 4) score -= 2;
                        else score -= 1;
                    }
                    else if (team == in.staticVariables.opponent){
                        if (type == UnitType.CATAPULT) score += 10;
                        else if (type == UnitType.MAGE) score += 3;
                        else if (uir.getHealth() <= 4) score += 2;
                        else score += 1;
                    }
                    else score += 1;
                }

                if (score > bestTargetScore){
                    bestTargetLoc = targetLoc;
                    bestTargetScore = score;
                }
            }
        }

        in.unitController.attack(bestTargetLoc);
    }

    private boolean tryAttack() {
        if (!in.unitController.canAttack()) return false;

        UnitInfo bestCatapult = null;
        UnitInfo outerUnit = null;
        int health = 10000;

        for (UnitInfo enemy : in.staticVariables.allenemies) {
            UnitType enemyType = enemy.getType();
            int distance = enemy.getLocation().distanceSquared(in.staticVariables.myLocation);

            if (enemyType == UnitType.BASE){
                if (distance > 36){
                    in.unitController.attack(in.staticVariables.enemyBase.add(in.staticVariables.enemyBase.directionTo(in.staticVariables.myLocation)));
                }
                else {
                    in.unitController.attack(in.staticVariables.enemyBase);
                }
                return true;
            }
            if (enemyType == UnitType.CATAPULT) {
                int enemyhealth = enemy.getHealth();
                if (enemyhealth < health) {
                    health = enemyhealth;
                    bestCatapult = enemy;
                }
            }
            if (distance > 36) {
                outerUnit = enemy;
            }
        }

        if (bestCatapult != null) {
            aoeAttack(bestCatapult.getLocation());
            return true;
        }

        if (outerUnit != null) {
            aoeAttack(outerUnit.getLocation());
            return true;
        }

        if (genericTryAttack()) return true;

        TownInfo bestTown = null;
        int bestHealth = 100000;

        // Check towns in range if no enemies available
        for (TownInfo town : in.staticVariables.allenemytowns){
            if (bestTown == null || bestHealth > town.getLoyalty()){
                bestTown = town;
                bestHealth = town.getLoyalty();
            }
        }

        if (bestTown == null) return false;

        Location townLoc = bestTown.getLocation();
        int distance = townLoc.distanceSquared(in.staticVariables.myLocation);

        if (distance > 36){
            in.unitController.attack(townLoc.add(townLoc.directionTo(in.staticVariables.myLocation)));
        }
        else {
            in.unitController.attack(townLoc);
        }
        return true;
    }

}
