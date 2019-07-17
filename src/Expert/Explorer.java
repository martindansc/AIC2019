package Expert;

import aic2019.*;

public class Explorer {
    private Injection in;
    private Boolean microResult;
    private Direction microDir;


    public Explorer(Injection in) {
        this.in = in;
    }

    public void run() {
        microResult = doMicro();
        in.attack.genericTryAttack();
        Location target = getTarget();
        if (in.explorer.tryMove(target)) {
            in.staticVariables.myLocation = in.unitController.getLocation();
            in.staticVariables.allenemies = in.unitController.senseUnits(in.staticVariables.allies, true);
        }
        in.attack.genericTryAttack();
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
                        in.memoryManager.setTownScore(loc, 5);
                    } else {
                        in.memoryManager.setClaimedEnemy(loc);
                        in.memoryManager.setTownScore(loc, 5);
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
                    if (in.staticVariables.myLocation.distanceSquared(enemy.getLocation()) < 26) {
                        tower = true;
                        break;
                    }
                }
            }
            if ((microResult && in.staticVariables.myLocation.distanceSquared(target) < 65) || closestUnexplored < 26 || tower) {
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


    public boolean tryMove(Location target) {
        if (!in.unitController.canMove()) return false;

        if (!microResult) {
            Direction dir = in.pathfinder.getNextLocationTarget(target, loc -> in.memoryManager.isLocationSafe(loc));
            if (dir != null && in.unitController.senseImpact(in.staticVariables.myLocation.add(dir)) == 0) {
                if (in.unitController.canMove(dir)) {
                    if (in.memoryManager.isLocationSafe(in.staticVariables.myLocation.add(dir))) {
                        in.unitController.move(dir);
                        return true;
                    }
                }
            }
        } else {
            in.unitController.move(microDir);
        }

        return false;
    }

    public boolean doMicro() {
        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        boolean enemies = false;
        for (int i = 0; i < 9; i++) {
            microInfo[i].updateArea();
            int length = Math.min(11, in.staticVariables.allenemies.length);
            for (int j = 0; j < length; j++) {
                UnitInfo enemy = in.staticVariables.allenemies[j];
                UnitType enemyType = enemy.getType();
                Team unitTeam = enemy.getTeam();
                int distance = microInfo[i].loc.distanceSquared(enemy.getLocation());

                if (unitTeam == in.staticVariables.opponent) {
                    microInfo[i].updateSafe(distance, enemyType);
                } else {
                    int health = enemy.getHealth();
                    int maxHealth = enemyType.maxHealth;
                    if (health < maxHealth) {
                        microInfo[i].updateSafe(distance, enemyType);
                    } else {
                        microInfo[i].updateGreedy(distance, enemyType);
                    }
                }

                if (enemyType == UnitType.TOWER) {
                    if (distance < enemyType.getAttackRangeSquared()) microInfo[i].numEnemies++;
                }
            }

            if (microInfo[i].numEnemies != 0) {
                enemies = true;
            }
        }

        if (!enemies) return false;

        int bestIndex = -1;

        for (int i = 8; i >= 0; i--) {
            if (!in.unitController.canMove(in.staticVariables.dirs[i])) continue;
            if (bestIndex < 0 || !microInfo[bestIndex].isBetter(microInfo[i])) bestIndex = i;
        }

        if (bestIndex != -1) {
            if (in.staticVariables.allenemies.length > 0) {
                microDir = (in.staticVariables.dirs[bestIndex]);
                return true;
            }
        }

        return false;
    }

    class MicroInfo {
        int numEnemies;
        int minDistToEnemy;
        Location loc;

        public MicroInfo(Location loc) {
            this.loc = loc;
            numEnemies = 0;
            minDistToEnemy = 100000;
        }

        void updateSafe(int distance, UnitType enemyType) {
            if (enemyType == UnitType.SOLDIER) {
                if (distance < 14) numEnemies++;
            } else if (enemyType == UnitType.ARCHER) {
                if (distance < 33) numEnemies++;
            } else if (enemyType == UnitType.KNIGHT) {
                if (distance < 9) numEnemies++;
            } else if (enemyType == UnitType.MAGE) {
                if (distance < 26) numEnemies++;
            } else if (enemyType == UnitType.EXPLORER) {
                if (distance < 14) numEnemies++;
            }

            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        void updateGreedy(int distance, UnitType enemyType) {
            if (enemyType == UnitType.SOLDIER || enemyType == UnitType.ARCHER || enemyType == UnitType.KNIGHT || enemyType == UnitType.EXPLORER) {
                if (distance <= enemyType.getAttackRangeSquared()) numEnemies++;
            } else if (enemyType == UnitType.MAGE) {
                if (distance < 14) numEnemies++;
            }

            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        void updateArea() {
            if (in.unitController.senseImpact(loc) != 0) {
                numEnemies += 100;
                return;
            }
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return true;
            if (numEnemies < m.numEnemies) return true;
            if (numEnemies > m.numEnemies) return false;
            return minDistToEnemy >= m.minDistToEnemy;
        }
    }
}
