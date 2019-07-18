package Expert;

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
            }

            in.map.sendResourcesObjective();

            in.unitController.yield(); //End of turn
        }

    }
}
