package LilRookie;

import aic2019.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        Injection in = new Injection(uc);

        if(uc.getRound() < 2) {
            in.messages.sendCreateAndSendToLocation(in.staticVariables.baseId, UnitType.WORKER, 4, 20);
        }

        while (true){
            in.staticVariables.update();
            in.memoryManager.increaseValueByOne(in.constants.ID_ALLIES_COUNTER);


			/*If this unit is a base, try spawning a soldier at direction dir*/
			if (uc.getType() == UnitType.BASE) {
                in.base.run();
            }

			else if (in.unitController.getType() == UnitType.SOLDIER){
			    Location target = in.soldier.getSoldierTarget();
			    in.move.myMove(target);
			    in.attack.tryAttackBestTarget(target);
            } else if (in.unitController.getType() == UnitType.WORKER) {
                in.worker.run();
            }

            in.unitController.yield(); //End of turn
        }

    }
}
