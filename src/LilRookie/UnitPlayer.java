package LilRookie;

import aic2019.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        Injection in = new Injection(uc);

        while (true){
            in.staticVariables.update();
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_COUNTER);

            Location target = in.soldier.getSoldierTarget();

            in.move.sendResourcesMessage();

			/*If this unit is a base, try spawning a soldier at direction dir*/
			if (uc.getType() == UnitType.BASE) {
                in.base.run();
            }

			else if (in.unitController.getType() == UnitType.SOLDIER){
			    in.move.myMove(target);
            } else if (in.unitController.getType() == UnitType.WORKER) {
                in.worker.run();
            }

            in.attack.tryAttackBestTarget(target);

            in.unitController.yield(); //End of turn
        }

    }
}
