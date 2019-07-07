package Rookie;

import aic2019.*;

public class Map {
/*
A tenir en compte en el mapa:
- Tipus de terrain
- Catapulta fire incoming?

Estructures i unitats
- Static
    - Base
        - Health
    - Recurs
    - Poble
        - Lleieltat
- Dynamic:
    -> last round seen
    - Torre
        - Propietari
        - Health
    - Quarter
        - Propietari
        - Health
    - Unitat
        - Propietari
        - Tipus
        - Mes info?
- Empty
*/

    Injection in;

    Map(Injection in) {
        this.in = in;
    }

    public boolean canSendUnitToLocation(int locX, int locY) {
        return (in.memoryManager.getTimesSendUnitLocation(locX, locY) < 1);
    }

    public void sendResourcesMessage() {
        for (ResourceInfo resource : in.staticVariables.resourcesSeen) {
            in.messages.sendCreateAndSendToLocation(in.staticVariables.baseId, UnitType.WORKER, resource.getLocation().x, resource.getLocation().y);
        }
    }


}
