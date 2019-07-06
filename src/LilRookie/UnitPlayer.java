package LilRookie;

import aic2019.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        Injection in = new Injection(uc);

        while (true){
            in.staticVariables.update();
            in.memoryManager.increaseValueByOne(in.constants.ALLIES_COUNTER);

			/*Generate a random number from 0 to 7, both included*/
			int randomNumber = (int)(Math.random()*8);

			/*Get corresponding direction*/
			Direction dir = in.staticVariables.dirs[randomNumber];

			/*If this unit is a base, try spawning a soldier at direction dir*/
			if (uc.getType() == UnitType.BASE) {
                if (uc.canSpawn(dir, UnitType.SOLDIER)) uc.spawn(dir, UnitType.SOLDIER);
                //if (in.unitController.canSpawn(dir, UnitType.WORKER)) in.unitController.spawn(dir, UnitType.WORKER);
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
