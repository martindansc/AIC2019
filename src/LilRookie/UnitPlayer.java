package LilRookie;

import aic2019.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        StaticVariables variables = new StaticVariables(uc);

        Attack attack = new Attack(variables);


        while (true){
            variables.update();

			/*Generate a random number from 0 to 7, both included*/
			int randomNumber = (int)(Math.random()*8);

			/*Get corresponding direction*/
			Direction dir = Direction.values()[randomNumber];

			/*move in direction dir if possible*/
			if (uc.canMove(dir)) uc.move(dir);

			/*If this unit is a base, try spawning a soldier at direction dir*/
			if (uc.getType() == UnitType.BASE) {
                if (uc.canSpawn(dir, UnitType.SOLDIER)) uc.spawn(dir, UnitType.SOLDIER);
            }

			else {
			    attack.tryAttackBestTarget();
            }

            uc.yield(); //End of turn
        }

    }
}
