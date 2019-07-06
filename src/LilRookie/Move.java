package LilRookie;

import aic2019.*;

public class Move {
    private Injection in;

    public Move(Injection in){
        this.in = in;
    }

    public void myMove(Location target) {
        if (!in.unitController.canMove()) return;

        if (in.staticVariables.round < 75) return;

        Direction dir = in.pathfinder.getNextLocationTarget(target);
        if (dir != null && in.unitController.canMove(dir)) {
            in.unitController.move(dir);
        }
    }
}
