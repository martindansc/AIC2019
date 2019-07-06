package LilRookie;

import aic2019.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        StaticVariables variables = new StaticVariables(uc);

        Attack attack = new Attack(variables);
        Move move = new Move(variables, uc);
        Soldier soldier = new Soldier(variables);

        while (true){
            variables.update();

			/*Generate a random number from 0 to 7, both included*/
			int randomNumber = (int)(Math.random()*8);

			/*Get corresponding direction*/
			Direction dir = variables.dirs[randomNumber];

			/*If this unit is a base, try spawning a soldier at direction dir*/
			if (uc.getType() == UnitType.BASE) {
                if (uc.canSpawn(dir, UnitType.SOLDIER)) uc.spawn(dir, UnitType.SOLDIER);
            }

			else if (uc.getType() == UnitType.SOLDIER){
			    Location target = soldier.getSoldierTarget();
			    move.myMove(target);
			    attack.tryAttackBestTarget(target);
            }

            uc.yield(); //End of turn
        }

    }
}
