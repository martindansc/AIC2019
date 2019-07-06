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
    //Location of the closest base or town
    private Location objectiveBase;
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
            Boolean resourceFound = scout("random");

            if(!resourceFound){
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
        }
        if(currentAction == "GOTORESOURCE"){
            //Move to desired resource
            if (!in.unitController.canMove()) return;

            Direction dir = in.pathfinder.getNextLocationTarget(objectiveLocation);
            if (dir != null && in.unitController.canMove(dir)) {
                in.unitController.move(dir);
            }
            //Check if desired resource has been reached
            Location nextLocation =  in.staticVariables.myLocation.add(dir);
            if(nextLocation.isEqual(objectiveLocation)){
                currentAction = "GATHERRESOURCE";
                //TODO: Use cangather()??
                in.unitController.gather();
            }
            //Scout viewing zone
            //TODO

        }
        if(currentAction == "GATHERRESOURCE"){

            int health = in.staticVariables.unitInfo.getHealth();
            in.unitController.gather();
            //Si ja hem recol.lectat el que voliem del resource tornem a la town o base objectiu
            float wood = in.staticVariables.unitInfo.getWood();
            if(wood >= 60.0){ //60
                objectiveBase = getClosestTownToLocation(objectiveLocation);
                currentAction = "GOTOTOWN";
            }
            float iron = in.staticVariables.unitInfo.getIron();
            if( iron >= 20.0){ //20
                objectiveBase = getClosestTownToLocation(objectiveLocation);
                currentAction = "GOTOTOWN";
            }
            float crystal = in.staticVariables.unitInfo.getCrystal();
            if( crystal >= 6.0){ //6
                objectiveBase = getClosestTownToLocation(objectiveLocation);
                currentAction = "GOTOTOWN";
            }
            //Scout viewing zone
            //TODO
        }
        if(currentAction == "GOTOTOWN"){
            //Check if destination is own
            if(!objectiveBase.isEqual(in.staticVariables.allies.getInitialLocation())){
                if(in.unitController.senseTown(objectiveBase).getOwner() != in.staticVariables.allies){
                    objectiveBase = getClosestTownToLocation(in.unitController.getLocation());
                }
            }

            //Move to desired town or base
            if (!in.unitController.canMove()) return;
            Direction dir = in.pathfinder.getNextLocationTarget(objectiveBase);
            //Sino avanco cap a lobjectiu
            if (dir != null && in.unitController.canMove(dir)) {
                in.unitController.move(dir);
            }
            //Si puc depositar deposito
            Direction dirbase = in.unitController.getLocation().directionTo(objectiveBase);
            if(in.unitController.canDeposit(dirbase)){
                in.unitController.deposit(dirbase);
                currentAction = "GOTORESOURCE";
                //Move to desired resource
                if (!in.unitController.canMove()) return;

                dir = in.pathfinder.getNextLocationTarget(objectiveLocation);
                if (dir != null && in.unitController.canMove(dir)) {
                    in.unitController.move(dir);
                }
                /*
                //Check if desired resource has been reached
                Location nextLocation =  in.staticVariables.myLocation.add(dir);
                if(nextLocation.isEqual(in.staticVariables.myLocation)){
                    currentAction = "GATHERRESOURCE";
                    //TODO: Use cangather()??
                    in.unitController.gather();
                }
                 */
            }


            //Scout viewing zone
            //TODO
        }
    }



    public Boolean scout(String mode){
        //TODO: actualitzar posicions del scout al mapa

        //Si hi ha algun resource i estic en mode random retornar la loc del resource
        if(mode == "random"){
            ResourceInfo[] resourcesSeen = in.unitController.senseResources();
            Location returnLocation = getClosestResource(resourcesSeen);
            if(returnLocation != new Location(100000, 100000)){
                currentAction = "GOTORESOURCE";
                objectiveLocation = returnLocation;
                return true;
            }
        }
        return false;
    }

    public Location getClosestResource(ResourceInfo[] resourcesSeen){
        //TODO: buscar el mes proper a la base o poble(no a un mateix)
        Location returnLocation = new Location(100000, 100000);
        for(ResourceInfo rI : resourcesSeen){
            int currentDistance = Math.abs(returnLocation.distanceSquared(in.staticVariables.myLocation));
            int nextDistance = Math.abs(rI.getLocation().distanceSquared(in.staticVariables.myLocation));
            if( nextDistance < currentDistance ){
                returnLocation = rI.getLocation();
            }
        }
        return returnLocation;
    }

    //Returns closest town or base
    public Location getClosestTownToLocation(Location location){
        Location loc = in.staticVariables.allies.getInitialLocation();
        for(TownInfo tI : in.staticVariables.myTowns){
            int currentDistance = Math.abs(loc.distanceSquared(location));
            int nextDistance = Math.abs(tI.getLocation().distanceSquared(location));
            if( nextDistance < currentDistance ){
                loc = tI.getLocation();
            }
        }
        return loc;
    }
}
