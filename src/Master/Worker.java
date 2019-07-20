package Master;

import aic2019.*;

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
    //Best micro direction
    Direction microDir;
    //Is micro needed?
    Boolean microResult;

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

    public void run() {
        //Si estic a sobre dun resource agafarlo
        if(in.unitController.canGather()){
            in.unitController.gather();
        }

        int barracksWorkerId = in.memoryManager.getBarracksWorkerId();

        if (in.staticVariables.wood > 2 * GameConstants.BARRACKS_WOOD_COST &&
                in.staticVariables.iron > 2 * GameConstants.BARRACKS_IRON_COST && barracksWorkerId == 0) {
            in.memoryManager.setBarracksWorkerId(in.staticVariables.myId);
            objectiveLocation = null;
        }

        if (barracksWorkerId == in.staticVariables.myId && !in.memoryManager.isBarracksBuilt()) {
            int distanceToBase = in.staticVariables.myLocation.distanceSquared(in.staticVariables.allyBase);
            int distanceToEnemyBase = in.staticVariables.myLocation.distanceSquared(in.staticVariables.enemyBase);
            if (distanceToBase * 4 > distanceToEnemyBase) {
                for (int i = 0; i < in.staticVariables.dirs.length; i++) {
                    if (in.unitController.canSpawn(in.staticVariables.dirs[i], UnitType.BARRACKS)) {
                        in.unitController.spawn(in.staticVariables.dirs[i], UnitType.BARRACKS);
                        in.memoryManager.setBarracksBuilt();
                        fixRandomDirection();
                        break;
                    }
                }
            } else {
                Direction dir = in.pathfinder.getNextLocationTarget(in.staticVariables.enemyBase, loc -> in.memoryManager.isLocationSafe(loc));
                if (in.unitController.canMove(dir)) {
                    in.unitController.move(dir);
                }
            }
        } else {
            unitMoved = false;
            this.selectObjective();

            microResult = doMicro();
            if (microResult) {
                in.unitController.move(microDir);
                currentAction = "GOTORESOURCE";
            } else {
                if (currentAction == "GOTORANDOM") {
                    goToRandom();
                } else if (currentAction == "GOTORESOURCE") {
                    goToResource();
                } else if (currentAction == "GATHERRESOURCE") {
                    gatherResource();
                } else if (currentAction == "GOTOTOWN") {
                    goToTown();
                }
            }
        }

        //Si estic a sobre dun resource agafarlo
        if(unitMoved && in.unitController.canGather()){
            in.unitController.gather();
        }

        //If can deposit, deposit
        Direction dir = in.staticVariables.myLocation.directionTo(in.helper.getClosestTownToLocation(in.staticVariables.myLocation));
        depositResource(dir);
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
        //Macro to desired resource
        if (in.unitController.canMove()) {
            Direction dir = in.pathfinder.getNextLocationTarget(objectiveLocation,
                    (Location loc) -> in.memoryManager.isLocationSafe(loc));
            if (dir != null && dir != Direction.ZERO && in.unitController.senseImpact(in.staticVariables.myLocation.add(dir)) == 0) {
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
        float iron = in.staticVariables.unitInfo.getIron();
        float crystal = in.staticVariables.unitInfo.getCrystal();

        /* Long-temr optimum
        float distance = (float)Math.sqrt(in.staticVariables.myLocation.distanceSquared(in.helper.getClosestTownToLocation(objectiveLocation)));
        float result;
        if (distance > 30) {
            result = 120.f;
        } else {
            result = 17.7f + 31.1f * (float)Math.log(distance);
        }
        */

        if (wood + iron * 3 + crystal * 10 > in.constants.WORKERS_GATHER_ABSOLUTE) {
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
        //Macro to desired town or base
        if (!in.unitController.canMove()) return;
        Direction dir = in.pathfinder.getNextLocationTarget(objectiveBase, loc-> in.memoryManager.isLocationSafe(loc));
        if (dir != null && dir != Direction.ZERO && in.unitController.senseImpact(in.staticVariables.myLocation.add(dir)) == 0) {
            String nextMovementIsSafe = checkIfMovementIsSafe(in.staticVariables.myLocation, dir);
            if(nextMovementIsSafe == "CANMOVE") {
                in.unitController.move(dir);
                unitMoved = true;
            }
            //TODO: tenir en compte catapultes i altres
            //else if()
        }
        //Desposit resource if possible
        Direction dirbase = in.unitController.getLocation().directionTo(objectiveBase);
        Boolean resourceDeposited = depositResource(dirbase);
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
        if(in.unitController.canDeposit(dir)){
            in.unitController.deposit(dir);
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

            //If worker wants to macro to wall change direction
            if (nextMovementIsSafe == "WALL") {
                int randomNumber = (int) (Math.random() * 8);
                randomDir = Direction.values()[randomNumber];
            }
            //If there is a catapult attack on the going position dont macro
            //TODO
            //If you see a enemy unit macro in opposite direction
            //TODO
            //If it is safe to macro, macro
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
        //check if next location is in range of attacking base/tower
        else if(!in.memoryManager.isLocationSafe(nextLocation)){
            return "TOWER/BASE";
        } //check if unit can macro to that location
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
        /*
        int[] message = in.messages.readMessage();
        if(message[0] != 0) {
            this.fixObjectiveLocation(new Location(message[0], message[1]), true);
        }
        */
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

    public boolean doMicro() {

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) {
            microInfo[i] = new MicroInfo(in.staticVariables.myLocation.add(in.staticVariables.dirs[i]));
        }

        boolean noworkers = false;
        for (UnitInfo enemy : in.staticVariables.allenemies) {
            if (enemy.getType() == UnitType.TOWER) {
                int type = enemy.getTeam() != in.staticVariables.opponent ?
                        in.constants.NEUTRAL_TOWER : in.constants.ENEMY_TOWER;
                int[] objectives = in.objectives.createCatapultObjective(enemy.getLocation(), type);
                in.memoryManager.addObjective(UnitType.CATAPULT, objectives);
            }
            if (!in.unitController.isObstructed(enemy.getLocation(), in.staticVariables.myLocation)) {
                if (enemy.getType() != UnitType.WORKER) {
                    noworkers = true;
                    for (int i = 0; i < 9; i++) {
                        microInfo[i].update(enemy);
                    }
                }
            }
        }

        if (!noworkers) return false;

        int bestIndex = -1;

        for (int i = 8; i >= 0; i--) {
            if (!in.unitController.canMove(in.staticVariables.dirs[i])) continue;
            if (bestIndex < 0 || !microInfo[bestIndex].isBetter(microInfo[i])) bestIndex = i;
        }

        if (bestIndex != -1) {
            if (in.staticVariables.allenemies.length > 0) {
                microDir = (in.staticVariables.dirs[bestIndex]);
                return true;
            }
        }

        return false;
    }

    class MicroInfo {
        int numEnemies;
        int minDistToEnemy;
        Location loc;

        public MicroInfo(Location loc) {
            this.loc = loc;
            numEnemies = 0;
            minDistToEnemy =  100000;
        }

        void update(UnitInfo unit) {
            if (!in.memoryManager.isLocationSafe(loc)) {
                numEnemies += 100;
                return;
            }
            int distance = unit.getLocation().distanceSquared(loc);
            if (distance <= unit.getType().attackRangeSquared) ++numEnemies;
            if (distance < minDistToEnemy) minDistToEnemy = distance;
        }

        boolean isBetter(MicroInfo m) {
            if (!in.memoryManager.isLocationSafe(m.loc)) return true;
            if (numEnemies >= 100) return false;
            if (m.numEnemies >= 100) return true;
            if (numEnemies < m.numEnemies) return true;
            if (numEnemies > m.numEnemies) return false;
            return minDistToEnemy >= m.minDistToEnemy;
        }
    }


}
