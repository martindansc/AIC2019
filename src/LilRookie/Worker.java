package LilRookie;
import aic2019.*;

public class Worker {
    //General direction
    Direction dirDestination;
    //Direction used in last round
    Direction lastDirection;

    //Location of the closest resource
    Location resource;

    //Generate worker without a fixed destination
    public Worker(){

        //Get a first random direction
        /*Generate a random number from 0 to 7, both included*/
        int randomNumber = (int)(Math.random()*8);

        /*Get corresponding direction*/
        dirDestination = Direction.values()[randomNumber];
    }

    //Generate worker with a fixed destination
    public Worker(Direction destination){
        dirDestination = destination;
    }

    //Generate worker with destination to resource
    public Worker(Location resource){

        //dirDestination = utils.getDirectionFromLocations()
    }
}
