package Rookie;

import aic2019.*;

public class StaticVariables {

    private final Injection in;


    public int round;
    public Team opponent;
    public Team allies;
    public Direction[] dirs;
    public Resource[] resources;
    public ResourceInfo[] resourcesSeen;
    public Location myLocation;
    public UnitType type;
    public UnitInfo[] units;
    public UnitInfo[] enemies;
    public UnitInfo[] allenemies;
    public int baseId;
    public int myId;
    public TownInfo[] enemytowns;
    public TownInfo[] myTowns;
    public float wood;
    public float iron;
    public float crystal;
    public UnitInfo unitInfo;

    public StaticVariables(Injection in) {
        this.in = in;

        opponent = in.unitController.getOpponent();
        allies = in.unitController.getTeam();
        dirs = Direction.values();
        type = in.unitController.getType();

        baseId = in.unitController.senseUnit(allies.getInitialLocation()).getID();
        myId = in.unitController.getInfo().getID();

        type = in.unitController.getType();
        enemytowns = in.unitController.getTowns(allies, true);
        resources = Resource.values();
    }

    public void update() {
        myLocation = in.unitController.getLocation();
        round = in.unitController.getRound();
        enemies = in.unitController.senseUnits(opponent, false);
        allenemies = in.unitController.senseUnits(allies, true);
        units = in.unitController.senseUnits();
        myTowns = in.unitController.getTowns(allies, false);
        enemytowns = in.unitController.getTowns(allies, true);
        wood = in.unitController.getWood();
        iron = in.unitController.getIron();
        crystal = in.unitController.getCrystal();
        resourcesSeen = in.unitController.senseResources();
        unitInfo = in.unitController.getInfo();
    }
}