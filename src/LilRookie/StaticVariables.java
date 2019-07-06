package LilRookie;

import aic2019.*;

public class StaticVariables {

    private final Injection in;


    public int round;
    public Team opponent;
    public Team allies;
    public Direction[] dirs;
    public Location myLocation;
    public UnitType type;
    public UnitInfo[] units;
    public UnitInfo[] enemies;
    public UnitInfo[] allenemies;
    public TownInfo[] enemytowns;
    public TownInfo[] myTowns;

    public StaticVariables(Injection in) {
        this.in = in;

        opponent = in.unitController.getOpponent();
        allies = in.unitController.getTeam();
        dirs = Direction.values();
        type = in.unitController.getType();
        enemytowns = in.unitController.getTowns(allies, true);
    }

    public void update() {
        myLocation = in.unitController.getLocation();
        round = in.unitController.getRound();
        enemies = in.unitController.senseUnits(opponent, false);
        allenemies = in.unitController.senseUnits(allies, true);
        units = in.unitController.senseUnits();
        myTowns = in.unitController.getTowns(allies, false);
    }
}