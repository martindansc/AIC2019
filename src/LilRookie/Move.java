package LilRookie;

import aic2019.*;

public class Move {
    private Pathfinder pathfinder;
    private UnitController uc;
    private StaticVariables variables;

    public Move(StaticVariables variables, UnitController uc){
        this.uc = uc;
        this.variables = variables;
        pathfinder = new Pathfinder(variables, uc);
    }

    public void myMove(Location target) {
        if (!uc.canMove()) return;

        Direction dir = pathfinder.getNextLocationTarget(target);
        if (dir != null && uc.canMove(dir)) {
            uc.move(dir);
        }
    }
}
