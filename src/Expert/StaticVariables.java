package Expert;

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
    public Location allyBase;
    public Location enemyBase;
    public Location[] visiblelocs;
    public UnitType type;
    public UnitInfo[] units;
    public UnitInfo[] allyUnits;
    public UnitInfo[] enemies;
    public UnitInfo[] allenemies;
    public int baseId;
    public int myId;
    public TownInfo[] allenemytowns;
    public TownInfo[] myTowns;
    public float wood;
    public float iron;
    public float crystal;
    float crystalwood;
    float crystaliron;
    float woodiron;
    public UnitInfo unitInfo;

    public StaticVariables(Injection in) {
        this.in = in;

        opponent = in.unitController.getOpponent();
        allies = in.unitController.getTeam();
        dirs = Direction.values();
        type = in.unitController.getType();

        enemyBase = in.unitController.getTeam().getOpponent().getInitialLocation();
        allyBase = in.unitController.getTeam().getInitialLocation();
        baseId = in.unitController.senseUnit(allies.getInitialLocation()).getID();
        myId = in.unitController.getInfo().getID();

        resources = Resource.values();
    }

    public void update() {
        myLocation = in.unitController.getLocation();
        round = in.unitController.getRound();
        enemies = in.unitController.senseUnits(opponent, false);
        allenemies = in.unitController.senseUnits(allies, true);
        allyUnits = in.unitController.senseUnits(allies, false);
        units = in.unitController.senseUnits();
        myTowns = in.unitController.getTowns(allies, false);
        allenemytowns = in.unitController.getTowns(allies, true);
        wood = in.unitController.getWood();
        iron = in.unitController.getIron();
        crystal = in.unitController.getCrystal();
        resourcesSeen = in.unitController.senseResources();
        unitInfo = in.unitController.getInfo();
        visiblelocs = in.unitController.getVisibleLocations();
        if (type == UnitType.BASE) {
            crystalwood = in.unitController.tradeOutput(Resource.CRYSTAL, Resource.WOOD, 1);
            crystaliron = in.unitController.tradeOutput(Resource.CRYSTAL, Resource.IRON, 1);
            woodiron = in.unitController.tradeOutput(Resource.WOOD, Resource.IRON, 1);
        }
    }
}