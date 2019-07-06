package LilRookie;

import aic2019.*;

public class StaticVariables {

    public UnitController uc;

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

    public StaticVariables(UnitController uc) {
        this.uc = uc;

        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();
        type = uc.getType();
        enemytowns = uc.getTowns(allies, true);
    }

    public void update() {
        myLocation = uc.getLocation();
        round = uc.getRound();
        enemies = uc.senseUnits(opponent, false);
        allenemies = uc.senseUnits(allies, true);
        units = uc.senseUnits();
        myTowns = uc.getTowns(allies, false);
    }
}