package LilRookie;
import aic2019.*;
import com.sun.org.apache.xpath.internal.operations.Bool;

public class Worker {
    private StaticVariables variables;
    private UnitController uc;

    //Stores the current objective of the worker
    private String currentAction;

    //true if direction is random, false if its going to a desired location
    Boolean directionIsRandom;
    //Location of the closest resource
    private Location objectiveLocation;
    //RandomDirection
    Direction randomDir;

    public Worker(StaticVariables variables) {
        this.variables = variables;
        uc = variables.uc;

        fixRandomDirection();
    }

    public void fixObjectiveLocation(Location loc, Boolean resourceObjective){
        directionIsRandom = false;
        objectiveLocation = loc;
        if(resourceObjective) currentAction = "GOTORESOURCE";
        else currentAction = "GOTOTOWN";
    }

    public void fixRandomDirection(){
        directionIsRandom = true;
        int randomNumber = (int)(Math.random()*8);
        randomDir = Direction.values()[randomNumber];
        currentAction = "GOTORANDOM";
    }

    public void work(){
        if(currentAction == "GOTORANDOM"){
            if (uc.canMove()) {
                if(uc.canMove(randomDir)){
                    uc.move(randomDir);
                }
            }
        }
        else if(currentAction == "GOTORESOURCE"){

        }
        else if(currentAction == "GOTOTOWN"){

        }
    }
}
