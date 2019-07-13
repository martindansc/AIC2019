package Skilled;

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
    //True if unit moved in this turn
    Boolean unitMoved;

    public Worker(Injection in) {
        this.in = in;
        fixRandomDirection();
        unitMoved = false;
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
        unitMoved = false;
        this.selectObjective();

        //Si estic a sobre dun resource agafarlo
        if(in.unitController.canGather()){
            in.unitController.gather();
        }

        if(currentAction == "GOTORANDOM"){
            goToRandom();
        }
        else if(currentAction == "GOTORESOURCE"){
            goToResource();
        }
        else if(currentAction == "GATHERRESOURCE"){
            gatherResource();
        }
        else if(currentAction == "GOTOTOWN"){
            goToTown();
        }

        //Si estic a sobre dun resource agafarlo
        if(unitMoved && in.unitController.canGather()){
            in.unitController.gather();
        }
    }


    public void goToRandom(){
        //Scout viewing zone
        Boolean resourceFound = scout("random");

        //If not resource found
        if(!resourceFound){
            moveRandom();
            //Scout new viewing zone
            //TODO
            return;
        }

        //If resource found
        currentAction = "GOTORESOURCE";
        goToResource();
        return;
    }

    public void goToResource(){
        //Move to desired resource
        if (in.unitController.canMove()) {
            Direction dir = in.pathfinder.getNextLocationTarget(objectiveLocation,
                    (Location loc) -> this.checkIfMovementIsSafe(loc).equals("CANMOVE"));
            if (dir != null && dir != Direction.ZERO) {
                String nextMovementIsSafe = checkIfMovementIsSafe(in.staticVariables.myLocation, dir);
                if(nextMovementIsSafe == "CANMOVE") {
                    in.unitController.move(dir);
                    unitMoved = true;
                }
                //TODO: tenir en compte catapultes i altres
                //else if()
            }
            //Check if desired resource has been reached
            if (in.staticVariables.myLocation.isEqual(objectiveLocation)) {
                currentAction = "GATHERRESOURCE";
                gatherResource();
                return;
            }
        }
        //Scout viewing zone
        //TODO
        return;
    }

    public void gatherResource(){

        if(in.unitController.canGather()){
            in.unitController.gather();
        }
        //Si ja hem recol.lectat el que voliem del resource tornem a la town o base objectiu
        float wood = in.staticVariables.unitInfo.getWood();
        if(wood >= in.constants.WORKERS_GATHER_WOOD){ //60
            objectiveBase = in.helper.getClosestTownToLocation(objectiveLocation);
            currentAction =  "GOTOTOWN";
            goToTown();
            return;
        }
        float iron = in.staticVariables.unitInfo.getIron();
        if( iron >= in.constants.WORKERS_GATHER_IRON){ //20
            objectiveBase = in.helper.getClosestTownToLocation(objectiveLocation);
            currentAction =  "GOTOTOWN";
            goToTown();
            return;
        }
        float crystal = in.staticVariables.unitInfo.getCrystal();
        if( crystal >= in.constants.WORKERS_GATHER_MINERAL){ //6
            objectiveBase = in.helper.getClosestTownToLocation(objectiveLocation);
            currentAction = "GOTOTOWN";
            goToTown();
            return;
        }
        //Scout viewing zone
        //TODO
        return;
    }

    public void goToTown(){
        //Ceck if destination is own and correct it
        checkDestTownOwnAndCorrect();
        //Move to desired town or base
        if (!in.unitController.canMove()) return;
        Direction dir = in.pathfinder.getNextLocationTarget(objectiveBase);
        if (dir != null && dir != Direction.ZERO) {
            String nextMovementIsSafe = checkIfMovementIsSafe(in.staticVariables.myLocation, dir);
            if(nextMovementIsSafe == "CANMOVE") {
                in.unitController.move(dir);
                unitMoved = true;
            }
            //TODO: tenir en compte catapultes i altres
            //else if()
        }
        //Desposit resource if possible
        Boolean resourceDeposited = depositResource(dir);
        //Scout viewing zone
        //TODO
        if(resourceDeposited){
            currentAction =  "GOTORESOURCE";
            return;
        }
        return;
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
                objectiveBase = in.helper.getClosestTownToLocation(in.unitController.getLocation());
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
                unitMoved = true;
            }
            //If wanted possition is not accessible try the others
            else if (nextMovementIsSafe == "SAFEBUTNOTMOVE") {
                Boolean hasMoved = false;
                int currentDirIndx = (int) (Math.random() * 8);
                while (!hasMoved) {
                    Direction auxiliarRandomDir = Direction.values()[currentDirIndx];
                    if (checkIfMovementIsSafe(in.staticVariables.myLocation, auxiliarRandomDir) == "CANMOVE") {
                        in.unitController.move(auxiliarRandomDir);
                        unitMoved = true;
                        hasMoved = true;
                    } else {
                        currentDirIndx = (currentDirIndx + 1) % 8;
                    }
                }
            }
        }
    }

    public String checkIfMovementIsSafe(Location nextLocation){
        Direction dir = in.staticVariables.myLocation.directionTo(nextLocation);
        //Check if next location is out of the map
        if(in.unitController.isOutOfMap(nextLocation)){
            return "WALL";
        }
        //check if next location is having a catapult attack next turn
        else if(false){
            //TODO
        }
        //check if next location is in range of attacking enemy unit
        else if(!in.memoryManager.isLocationSafe(nextLocation)){
            return "TOWER/BASE";
        }
        //check if unit can move to that location
        else if(in.unitController.canMove(dir)){
            return "CANMOVE";
        }
        return "SAFEBUTNOTMOVE";
    }

    public String checkIfMovementIsSafe(Location loc, Direction dir){
        Location nextLocation = loc.add(dir);
        return checkIfMovementIsSafe(nextLocation);
    }


    public Boolean scout(String mode){
        //TODO: actualitzar posicions del scout al mapa

        //Si hi ha algun resource i estic en mode random retornar la loc del resource
        if(mode == "random"){
            ResourceInfo[] resourcesSeen = in.unitController.senseResources();
            Location returnLocation = getClosestResource(resourcesSeen);
            if(!returnLocation.isEqual(new Location(100000, 100000))){
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

    public void selectObjective() {
        // is there any message that points me to go somewhere and it's better than my current objective?
        int[] message = in.messages.readMessage();
        if(message[0] != 0) {
            this.fixObjectiveLocation(new Location(message[0], message[1]), true);
        }

        // do I currently have an objective set up?
        // if I don't have an objective, I can check for one in the objectives array and get the best
        if(directionIsRandom) {
            int closestObjective = Integer.MAX_VALUE;
            Location bestLocation = null;

            int[][] objectives = in.memoryManager.getObjectives();

            for (int[] objective: objectives) {
                if(!in.objectives.isFull(objective)){
                    // for now, as heuristic we are going to get the distance to the resource
                    Location objectiveLocation = in.objectives.getLocationObjective(objective);
                    int distance = in.staticVariables.myLocation.distanceSquared(objectiveLocation);
                    if(distance < closestObjective) {
                        closestObjective = distance;
                        bestLocation = objectiveLocation;
                    }
                }
            }

            if(bestLocation != null){
                this.fixObjectiveLocation(bestLocation, true);
            }

        }

        // claim objective
        if(objectiveLocation != null && !directionIsRandom) {
            in.objectives.claimObjective(this.objectiveLocation);
        }
    }

}
