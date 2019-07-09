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

    public void sendResourcesMessage() {
        for (ResourceInfo resource : in.staticVariables.resourcesSeen) {
            Location loc = resource.getLocation();
            int[] objective = in.objectives.createResourceObjective(loc);
            in.memoryManager.addObjective(UnitType.WORKER, objective);
        }
    }

    public UnitInfo getClosestAlliedUnitToLocation(UnitType unitType) {
        return null;
    }


}
