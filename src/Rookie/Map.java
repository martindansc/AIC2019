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

    public void markTower(Location loc) {
        if (in.memoryManager.getUnitFromLocation(loc) == UnitType.TOWER) return;
        int[] objective = in.objectives.createTowerObjective(loc);
        in.memoryManager.addObjective(UnitType.CATAPULT, objective);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int suma = i * i + j * j;
                if (suma < 33 && suma > 19) {
                    in.memoryManager.setLocationDangerous(new Location (loc.x + i, loc.y + j));
                    in.memoryManager.setLocationDangerous(new Location (loc.x - i, loc.y + j));
                    in.memoryManager.setLocationDangerous(new Location (loc.x + i, loc.y - j));
                    in.memoryManager.setLocationDangerous(new Location (loc.x - i, loc.y - j));
                }
            }
        }
        in.memoryManager.saveUnitToMap(loc, UnitType.TOWER);
    }

    public void unmarkTower(Location loc) {
        if ( in.memoryManager.getUnitFromLocation(loc) != UnitType.TOWER) return;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                int suma = i * i + j * j;
                if (suma < 33 && suma > 19) {
                    in.memoryManager.setLocationSafe(new Location (loc.x + i, loc.y + j));
                    in.memoryManager.setLocationSafe(new Location (loc.x - i, loc.y + j));
                    in.memoryManager.setLocationSafe(new Location (loc.x + i, loc.y - j));
                    in.memoryManager.setLocationSafe(new Location (loc.x - i, loc.y - j));
                }
            }
        }
        in.memoryManager.saveUnitToMap(loc, null);
    }

    public void markEnemyBase() {
        Location loc = in.staticVariables.enemyBase;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int suma = i * i + j * j;
                if (suma < 51 && suma > 33) {
                    in.memoryManager.setLocationSafe(new Location (loc.x + i, loc.y + j));
                    in.memoryManager.setLocationSafe(new Location (loc.x - i, loc.y + j));
                    in.memoryManager.setLocationSafe(new Location (loc.x + i, loc.y - j));
                    in.memoryManager.setLocationSafe(new Location (loc.x - i, loc.y - j));
                }
            }
        }
    }


}
