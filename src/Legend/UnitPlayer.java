package Legend;

import aic2019.Location;
import aic2019.UnitController;
import aic2019.UnitType;

public class UnitPlayer {

    public void run(UnitController uc) {

        Injection in = new Injection(uc);
        if(in.staticVariables.type == UnitType.BASE) in.map.markEnemyBase();

        while (true){
            in.staticVariables.update();
            in.messages.update();
            in.helper.countUnits();

            Location target = in.macro.getTarget();

			if (in.staticVariables.type == UnitType.BASE) {
                in.base.run();
            } else if (in.staticVariables.type == UnitType.WORKER) {
                in.worker.run();
            } else if (in.staticVariables.type == UnitType.SOLDIER) {
                in.soldier.run(target);
            } else if (in.staticVariables.type == UnitType.KNIGHT) {
                in.knight.run(target);
            } else if (in.staticVariables.type == UnitType.ARCHER) {
                in.archer.run(target);
            } else if (in.staticVariables.type == UnitType.MAGE) {
                in.mage.run(target);
            } else if (in.staticVariables.type == UnitType.CATAPULT) {
                in.catapult.run();
            } else if (in.staticVariables.type == UnitType.EXPLORER) {
			    in.explorer.run();
            } else if (in.staticVariables.type == UnitType.TOWER) {
                in.tower.run(target);
            } else if (in.staticVariables.type == UnitType.BARRACKS) {
                in.barracks.run();
            }

            // use the extra cpu for delayed actions
            this.delayedActions(in);

            in.unitController.yield(); //End of turn
        }

    }

    public void delayedActions(Injection in) {

        if(in.unitController.getEnergyLeft() < 3000) return;

        int[][] objectives = in.memoryManager.getObjectives(UnitType.BASE);
        for (int[] objective: objectives) {

            // skip when necessary
            if(in.unitController.getEnergyLeft() < 1000) return;
            if(objective[0] == 0) continue;

            // add tower objective
            if(objective[0] == in.constants.NEUTRAL_TOWER || objective[0] == in.constants.ENEMY_TOWER) {
                if(in.unitController.getEnergyLeft() > 6000) {
                    in.memoryManager.removeObjective(objective[5]);
                    in.map.markTower(in.objectives.getLocationObjective(objective), objective[0]);

                }
            }

            // remove tower objective
            if(objective[0] == in.constants.DESTROYED_TOWER) {
                if(in.unitController.getEnergyLeft() > 6000) {
                    if(in.objectives.getRound(objective) + UnitType.CATAPULT.attackDelay + 1
                            < in.staticVariables.round) {
                        in.memoryManager.removeObjective(objective[5]);
                        in.map.unmarkTower(in.objectives.getLocationObjective(objective));
                    }
                }
            }

        }
    }
}
