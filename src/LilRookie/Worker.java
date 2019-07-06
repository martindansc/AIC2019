package LilRookie;
import aic2019.*;
import com.sun.org.apache.xpath.internal.operations.Bool;

public class Worker {
    private final Injection in;

    //Stores the current objective of the worker
    private String currentAction;

    //true if direction is random, false if its going to a desired location
    Boolean directionIsRandom;
    //Location of the closest resource
    private Location objectiveLocation;
    //RandomDirection
    Direction randomDir;

    public Worker(Injection in) {
        this.in = in;
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

    public void run(){
        if(currentAction == "GOTORANDOM"){
            //Scout viewing zone
            //TODO

            //If worker wants to move to wall change direction
            Location nextLocation =  in.staticVariables.myLocation.add(randomDir);
            if(in.unitController.isOutOfMap(nextLocation)){
                int randomNumber = (int)(Math.random()*8);
                randomDir = Direction.values()[randomNumber];
            }
            //Move unit if possible
            if (in.unitController.canMove()) {
                if(in.unitController.canMove(randomDir)){
                    in.unitController.move(randomDir);
                }
                //If wanted possition is not accessible try the others
                else{
                    Boolean hasMoved = false;
                    int currentDirIndx = (int)(Math.random()*8);
                    while(!hasMoved){
                        Direction auxiliarRandomDir = Direction.values()[currentDirIndx];
                        if(in.unitController.canMove(auxiliarRandomDir)){
                            in.unitController.move(auxiliarRandomDir);
                            hasMoved = true;
                        }
                        else{
                            currentDirIndx = (currentDirIndx + 1)%8;
                        }
                    }
                }
            }
            //Scout viewing zone
            //TODO
        }
        else if(currentAction == "GOTORESOURCE"){
            //Scout viewing zone
            //TODO
        }
        else if(currentAction == "GOTOTOWN"){

            //Scout viewing zone
            //TODO
        }
    }
}
