package Master;

import aic2019.Direction;
import aic2019.Location;
import aic2019.UnitInfo;
import aic2019.UnitType;

public class Base {

    private final Injection in;

    Base(Injection in) {
        this.in = in;
    }

    public void run() {

        in.helper.addCocoonUnits();

        tryAttack();

        Direction bestDir = in.helper.getBestDirectionSpawn();

        int[] message = in.messages.readMessage();

        UnitType bestUnitType = in.helper.chooseBestUnitType(message);

        if(bestUnitType != UnitType.BASE) {
            int id = in.helper.spawnAndGetIdIfPossible(bestDir, bestUnitType);

            if(id != -1 && message[0] != 0) {
                in.messages.sendToLocation(id, message[0], message[1]);
            }
        }
    }

    private boolean isSafeAttack(Location loc) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                Location myLoc = new Location(loc.x + i, loc.y + j);
                if (in.unitController.canSenseLocation(myLoc)) {
                    UnitInfo unit = in.unitController.senseUnit(myLoc);
                    if (unit == null) continue;
                    if (unit.getTeam() == in.staticVariables.allies) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean tryAttack() {
        if (!in.unitController.canAttack()) return false;

        UnitInfo bestCatapult = null;
        UnitInfo outerUnit = null;
        UnitInfo innerUnit = null;
        int health = 10000;

        for (UnitInfo enemy : in.staticVariables.allenemies) {
            int distance = enemy.getLocation().distanceSquared(in.staticVariables.myLocation);
            if (enemy.getType() == UnitType.CATAPULT) {
                int enemyhealth = enemy.getHealth();
                if (enemyhealth < health) {
                    health = enemyhealth;
                    bestCatapult = enemy;
                }
            }
            if (distance > 36) {
                outerUnit = enemy;
            } else if (distance < 3) {
                innerUnit = enemy;
            }
        }

        if (bestCatapult != null) {
            Location catapultLoc = bestCatapult.getLocation();
            if (bestCatapult.getLocation().distanceSquared(in.staticVariables.myLocation) > 36) {
                in.unitController.attack(catapultLoc.add(catapultLoc.directionTo(in.staticVariables.myLocation)));
                return true;
            }
            in.unitController.attack(catapultLoc);
            return true;
        }

        if (outerUnit != null) {
            Location outerLoc = outerUnit.getLocation();
            Location target = outerLoc.add(outerLoc.directionTo(in.staticVariables.myLocation));
            if (isSafeAttack(target)) {
                in.unitController.attack(target);
            }
            return true;
        }

        boolean attacked = in.attack.genericTryAttack();

        if (innerUnit != null) {
            Location innerLoc = innerUnit.getLocation();
            Location target = innerLoc.add(in.staticVariables.myLocation.directionTo(innerLoc));
            if (in.unitController.canAttack(target) && isSafeAttack(target)) {
                in.unitController.attack(target);
            }
            return true;
        }

        return attacked;
    }

}
