package Master;

import aic2019.Location;

public class Objectives {
    private final Injection in;

    public Objectives(Injection in) {
        this.in = in;
    }

    public void setLocationObjective(int[] objective, Location loc) {
        objective[2] = loc.x;
        objective[3] = loc.y;
    }

    public Location getLocationObjective(int[] objective) {
        return new Location(objective[2], objective[3]);
    }

    public void setNumberUnits(int[] objective, int numberUnitsRequired) {
        objective[1] = numberUnitsRequired;
    }

    public int getNumberUnits(int[] objective) {
        return objective[1];
    }

    public int getClaimedTimes(int[] objective) {
        return Math.max(objective[6], objective[7]);
    }


    public void claimObjective(int[] objective) {
        if(objective[5] == 0) {
            in.unitController.println("Error, objective must be added before claimed");
        }
        in.memoryManager.claimObjective(objective[5]);
    }

    public void claimObjective(Location loc) {
        int idObjective = in.memoryManager.getObjectiveIdInLocation(loc);
        if(idObjective != 0) {
            in.memoryManager.claimObjective(idObjective);
        }
    }

    public boolean isFull(int[] objective) {
        return objective[5] == 0 || this.getClaimedTimes(objective) >= this.getNumberUnits(objective);
    }

    public int getRound(int[] objective) {
        return objective[9];
    }
    public int getUpdatedRound(int[] objective) {
        return objective[12];
    }

    public int getLastClaimedId(int[] objective) {
        return objective[10];
    }

    private void addCommonVariables(int[] objective) {
        objective[9] = in.staticVariables.round;
        objective[10] = in.staticVariables.myId;
        objective[12] = in.staticVariables.round;
    }

    // CREATE FUNCTIONS

    public int[] createResourceObjective(Location loc) {
        int[] newObjective = new int[in.constants.OBJECTIVE_SIZE];
        newObjective[0] = in.constants.WORKERS_GET_WOOD;
        newObjective[1] = 1;
        this.setLocationObjective(newObjective, loc);
        this.addCommonVariables(newObjective);
        newObjective[11] = loc.distanceSquared(in.staticVariables.allyBase);

        return newObjective;
    }

    public int[] createCatapultObjective(Location loc, int type) {
        int[] newObjective = new int[in.constants.OBJECTIVE_SIZE];
        newObjective[0] = type;
        newObjective[1] = 1;
        this.setLocationObjective(newObjective, loc);
        this.addCommonVariables(newObjective);
        newObjective[11] = type == in.constants.NEUTRAL_TOWER ? 10 : 1;

        return newObjective;
    }
}
