package Legend;

import aic2019.Direction;
import aic2019.Location;

import java.util.function.Function;

public class Pathfinder {

    private Injection in;

    public Pathfinder(Injection in) {
        this.in = in;
    }

    final int INF = 1000000;

    boolean rotateRight = true; //if I should rotate right or left
    boolean rotate = false;
    Location lastObstacleFound = null; //latest obstacle I've found in my way
    int minDistToEnemy = INF; //minimum distance I've been to the enemy while going around an obstacle
    Location prevTarget = null; //previous target
    int stuckCounter = 0;

    Direction getNextLocationTarget(Location target, Function<Location, Boolean> conditions) {
        //No target? ==> bye!
        if (target == null) return null;

        //different target? ==> previous data does not help!
        if (prevTarget == null || !target.isEqual(prevTarget)) resetPathfinding();

        //If I'm at a minimum distance to the target, I'm free!
        Location myLoc = in.staticVariables.myLocation;
        int d = myLoc.distanceSquared(target);
        if (d <= minDistToEnemy) resetPathfinding();

        //Update data
        prevTarget = target;
        minDistToEnemy = Math.min(d, minDistToEnemy);

        //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
        Direction dir = myLoc.directionTo(target);
        if (lastObstacleFound != null) dir = myLoc.directionTo(lastObstacleFound);

        //This should not happen for a single unit, but whatever
        if (in.unitController.canMove(dir)) resetPathfinding();

        //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
        //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
        for (int i = 0; i < 16; ++i){
            if (in.unitController.canMove(dir) && conditions.apply(myLoc.add(dir))){
                return dir;
            }
            if (!rotate && myLoc.add(dir.rotateLeft()).distanceSquared(target) > myLoc.add(dir.rotateRight()).distanceSquared(target)) {
                rotateRight = true;
                rotate = true;
            }
            Location newLoc = myLoc.add(dir);
            if (in.unitController.isOutOfMap(newLoc)) rotateRight = !rotateRight;
                //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
            else lastObstacleFound = myLoc.add(dir);
            if (rotateRight) dir = dir.rotateRight();
            else dir = dir.rotateLeft();
        }

        if (in.unitController.canMove(dir) && conditions.apply(myLoc.add(dir))) return dir;

        return null;
    }

    Direction getNextLocationTarget(Location target) {
        return getNextLocationTarget(target, ((Location location) -> true));
    }

    void resetPathfinding(){
        lastObstacleFound = null;
        minDistToEnemy = INF;
        stuckCounter = 0;
    }


}