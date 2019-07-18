package Master;

import aic2019.*;

public class Explorer {
    private Injection in;
    boolean micro;


    public Explorer(Injection in) {
        this.in = in;
    }

    public void run() {
        in.explorer.tryAttack();
        Location target = getTarget();
        in.explorer.tryMove(target);
        in.staticVariables.myLocation = in.unitController.getLocation();
        in.staticVariables.allenemies = in.unitController.senseUnits(in.staticVariables.allies, true);
        in.explorer.tryAttack();
        in.attack.genericTryAttackTown(target);
    }

    public Location getTarget() {
        int closestUnexplored = 100000;
        Location target = null;
        for (TownInfo town: in.staticVariables.allenemytowns) {
            Location loc = town.getLocation();
            if (town.getOwner() != in.staticVariables.allies) {
                int distance = in.staticVariables.myLocation.distanceSquared(loc);
                if (!in.memoryManager.isTownExplored(loc)) {
                    if (distance < closestUnexplored) {
                        closestUnexplored = distance;
                        target = loc;
                    }
                } else if (in.staticVariables.myLocation.distanceSquared(loc) < 37) {
                    in.memoryManager.setTownScore(loc, calculateScore(loc));
                }
                if (town.getOwner() == in.staticVariables.opponent) {
                    if (in.memoryManager.getTownStatus(loc) == in.constants.CLAIMED_TOWN) {
                        in.memoryManager.setStolen(loc);
                        in.memoryManager.setTownScore(loc, 2);
                    } else {
                        in.memoryManager.setClaimedEnemy(loc);
                        in.memoryManager.setTownScore(loc, 2);
                    }
                }
            } else {
                in.memoryManager.setClaimed(loc);
                in.memoryManager.setTownScore(loc, 0);
            }
        }

        if (target != null) {

            boolean tower = false;
            for (UnitInfo enemy: in.staticVariables.allenemies) {
                if (enemy.getType() == UnitType.TOWER) {
                    if (target.distanceSquared(enemy.getLocation()) < 26) {
                        tower = true;
                        break;
                    }
                }
            }
            if ((micro && in.staticVariables.myLocation.distanceSquared(target) < 65) || closestUnexplored < 50 || tower) {
                in.memoryManager.markTownAsExplored(target);
                int score = calculateScore(target);
                in.memoryManager.setTownScore(target, score);
            }

            return target;
        }

        return in.staticVariables.allyBase;
    }

    public int calculateScore(Location loc) {
        int score = 0;
        for (int i = -2; i < 3; i++) {
            for (int j = -2; j < 3; j++) {
                Location target = new Location(loc.x + i, loc.y + j);
                if (in.unitController.canSenseLocation(target)) {
                    UnitInfo enemy = in.unitController.senseUnit(target);
                    if (enemy != null && enemy.getTeam() != in.staticVariables.allies) {
                        score++;
                    }
                }
            }
        }
        return score;
    }


    public void tryMove(Location target) {
        if (!in.unitController.canMove()) return;

        micro = in.explorerPathfinder.getNextLocationTarget(target);
    }

    public boolean tryAttack()  {
        if (!in.unitController.canAttack()) return false;
        UnitInfo[] enemies = in.staticVariables.enemies;
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
}
