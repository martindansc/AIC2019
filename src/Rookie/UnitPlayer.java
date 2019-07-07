package Rookie;

import aic2019.Location;
import aic2019.UnitController;
import aic2019.UnitInfo;
import aic2019.UnitType;

public class UnitPlayer {

    public void run(UnitController uc) {

        Injection in = new Injection(uc);

        while (true){
            in.staticVariables.update();

            in.map.sendResourcesMessage();
            Location target = in.soldier.getSoldierTarget();
            in.attack.tryAttackBestTarget(target);

			if (uc.getType() == UnitType.BASE) {
                in.base.run();
            } else if (in.staticVariables.type == UnitType.WORKER) {
                in.worker.run();
            } else {
			    if (in.staticVariables.type == UnitType.SOLDIER) {
                    in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_SOLDIER_COUNTER);
                } else if (in.staticVariables.type == UnitType.ARCHER) {
                    in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_ARCHER_COUNTER);
                }
                in.move.myMove(target);
            }

            in.unitController.yield(); //End of turn
        }

    }
}
