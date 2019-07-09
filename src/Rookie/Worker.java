package Rookie;

import aic2019.Direction;
import aic2019.Location;
import aic2019.ResourceInfo;
import aic2019.TownInfo;

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
        randomDir = in.staticVariables.dirs[randomNumber];
        currentAction = "GOTORANDOM";
    }

    public void run(){
        this.selectObjective();
        //TODO: si estic a sobre dun resource agafarlo si estic anant a home

        if(currentAction == "GOTORANDOM"){
            currentAction = goToRandom();
            if(currentAction == "GOTORESOURCE"){
                currentAction = goToResource();
            }
        }
        if(currentAction == "GOTORESOURCE"){
            currentAction = goToResource();
            if(currentAction == "GATHERRESOURCE"){
                currentAction = gatherResource();
            }
        }
        if(currentAction == "GATHERRESOURCE"){
            currentAction = gatherResource();
            //TODO
        }
        if(currentAction == "GOTOTOWN"){
            currentAction = goToTown();
        }
    }


    public String goToRandom(){
        //Scout viewing zone
        Boolean resourceFound = scout("random");

        //If not resource found
        if(!resourceFound){
            moveRandom();
            //Scout new viewing zone
            //TODO
            return "GOTORANDOM";
        }

        //If resource found
        return "GOTORESOURCE";
    }

    public String goToResource(){
        //Move to desired resource
        if (in.unitController.canMove()) {
            Direction dir = in.pathfinder.getNextLocationTarget(objectiveLocation);
            if (dir != null && dir != Direction.ZERO) {
                String nextMovementIsSafe = checkIfMovementIsSafe(in.staticVariables.myLocation, dir);
                if(nextMovementIsSafe == "CANMOVE") {
                    in.unitController.move(dir);
                }
                //TODO: tenir en compte catapultes i altres
                //else if()
            }
            //Check if desired resource has been reached, can't gather if can't move (CDs)
            if (in.staticVariables.myLocation.isEqual(objectiveLocation)) {
                return "GATHERRESOURCE";
            }
        }
        //Scout viewing zone
        //TODO

        return "GOTORESOURCE";
    }

    public String gatherResource(){

        if(in.unitController.canGather()){
            in.unitController.gather();
        }
        //Si ja hem recol.lectat el que voliem del resource tornem a la town o base objectiu
        float wood = in.staticVariables.unitInfo.getWood();
        if(wood >= 60.0){ //60
            objectiveBase = getClosestTownToLocation(objectiveLocation);
            return "GOTOTOWN";
        }
        float iron = in.staticVariables.unitInfo.getIron();
        if( iron >= 20.0){ //20
            objectiveBase = getClosestTownToLocation(objectiveLocation);
            return "GOTOTOWN";
        }
        float crystal = in.staticVariables.unitInfo.getCrystal();
        if( crystal >= 6.0){ //6
            objectiveBase = getClosestTownToLocation(objectiveLocation);
            return "GOTOTOWN";
        }
        //Scout viewing zone
        //TODO
        return "GATHERRESOURCE";
    }

    public String goToTown(){
        //Ceck if destination is own and correct it
        checkDestTownOwnAndCorrect();
        //Move to desired town or base
        if (!in.unitController.canMove()) return "GOTOTOWN";
        Direction dir = in.pathfinder.getNextLocationTarget(objectiveBase);
        if (dir != null && dir != Direction.ZERO) {
            String nextMovementIsSafe = checkIfMovementIsSafe(in.staticVariables.myLocation, dir);
            if(nextMovementIsSafe == "CANMOVE") {
                in.unitController.move(dir);
            }
            //TODO: tenir en compte catapultes i altres
            //else if()
        }
        //Desposit resource if possible
        Boolean resourceDeposited = depositResource(dir);
        //Scout viewing zone
        //TODO
        if(resourceDeposited) return "GOTORESOURCE";
        return "GOTOTOWN";
    }

    public Boolean depositResource(Direction dir){
        //Si puc depositar deposito
        Direction dirbase = in.unitController.getLocation().directionTo(objectiveBase);
        if(in.unitController.canDeposit(dirbase)){
            in.unitController.deposit(dirbase);
            return true;
        }
        else return false;
    }

    public void checkDestTownOwnAndCorrect(){
        if(!objectiveBase.isEqual(in.staticVariables.allies.getInitialLocation())){
            boolean foundAllyTown = false;
            for (TownInfo closestTown : in.staticVariables.myTowns) {
                if (closestTown.getLocation().isEqual(objectiveBase)) {
                    foundAllyTown = true;
                    break;
                }
            }
            if (!foundAllyTown) {
                objectiveBase = getClosestTownToLocation(in.unitController.getLocation());
            }
        }
    }

    public void moveRandom(){
        if(in.unitController.canMove()) {
            String nextMovementIsSafe = checkIfMovementIsSafe(in.staticVariables.myLocation, randomDir);

            //If worker wants to move to wall change direction
            if (nextMovementIsSafe == "WALL") {
                int randomNumber = (int) (Math.random() * 8);
                randomDir = Direction.values()[randomNumber];
            }
            //If there is a catapult attack on the going position dont move
            //TODO
            //If you see a enemy unit move in opposite direction
            //TODO
            //If it is safe to move, move
            else if (nextMovementIsSafe == "CANMOVE") {
                in.unitController.move(randomDir);
            }
            //If wanted possition is not accessible try the others
            else if (nextMovementIsSafe == "SAFEBUTNOTMOVE") {
                Boolean hasMoved = false;
                int currentDirIndx = (int) (Math.random() * 8);
                while (!hasMoved) {
                    Direction auxiliarRandomDir = Direction.values()[currentDirIndx];
                    if (checkIfMovementIsSafe(in.staticVariables.myLocation, auxiliarRandomDir) == "CANMOVE") {
                        in.unitController.move(auxiliarRandomDir);
                        hasMoved = true;
                    } else {
                        currentDirIndx = (currentDirIndx + 1) % 8;
                    }
                }
            }
        }
    }

    public String checkIfMovementIsSafe(Location loc, Direction dir){
        Location nextLocation =  loc.add(dir);
        //Check if next location is out of the map
        if(in.unitController.isOutOfMap(nextLocation)){
            return "WALL";
        }
        //check if next location is having a catapult attack next turn
        else if(false){
            //TODO
        }
        //check if next location is in range of attacking enemy unit
        else if(false){
            //TODO
        }
        //check if unit can move to that location
        else if(in.unitController.canMove(dir)){
            return "CANMOVE";
        }
        return "SAFEBUTNOTMOVE";
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

    public void selectObjective() {
        // is there any message that points me to go somewhere and it's better than my current objective?
        int[] message = in.messages.readMessage();
        if(message[0] != 0) {
            this.fixObjectiveLocation(new Location(message[0], message[1]), true);
        }

        // do I currently have an objective set up?
        // if I don't have an objective, I can check for one in the objectives array and get the best
        if(!directionIsRandom) {
            int closestObjective = Integer.MAX_VALUE;
            Location bestLocation = null;

            int[][] objectives = in.memoryManager.getObjectives();

            for (int[] objective: objectives) {
                if(!in.objectives.isFull(objective)){
                    // for now, as heuristic we are going to get the distance to the resource
                    Location objectiveLocation = in.objectives.getLocationObjective(objective);
                    int distance = this.objectiveLocation.distanceSquared(objectiveLocation);
                    if(distance < closestObjective) {
                        closestObjective = distance;
                        bestLocation = objectiveLocation;
                    }
                }
            }

            if(bestLocation != null){
                this.fixObjectiveLocation(new Location(message[0], message[1]), true);
            }

        }

        // claim objective
        if(objectiveLocation != null && !directionIsRandom) {
            in.objectives.claimObjective(this.objectiveLocation);
        }
    }

}
