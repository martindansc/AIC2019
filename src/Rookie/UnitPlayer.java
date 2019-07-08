package Rookie;

import aic2019.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        Injection in = new Injection(uc);

        while (true){
            in.staticVariables.update();
            in.messages.update();
            in.memoryManager.countUnits();
            in.map.sendResourcesMessage();

            Location target = in.move.getTarget();

			if (uc.getType() == UnitType.BASE) {
                in.base.run();
            } else if (in.staticVariables.type == UnitType.WORKER) {
                in.worker.run();
            } else if (in.staticVariables.type == UnitType.SOLDIER) {
                in.soldier.run(target);
            } else if (in.staticVariables.type == UnitType.ARCHER) {
                in.archer.run(target);
            } else if (in.staticVariables.type == UnitType.MAGE) {
                in.mage.run(target);
            } else if (in.staticVariables.type == UnitType.CATAPULT) {
                in.catapult.run(target);
            }

            in.unitController.yield(); //End of turn
        }

    }
}
