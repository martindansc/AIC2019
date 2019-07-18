package Expert;

import aic2019.Location;
import aic2019.ResourceInfo;
import aic2019.UnitInfo;
import aic2019.UnitType;

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

    public void sendResourcesObjective() {
        for (ResourceInfo resource : in.unitController.senseResources()) {
            if(in.unitController.getEnergyLeft() < 4000) return;
            Location loc = resource.getLocation();
            if (in.unitController.isObstructed(in.staticVariables.myLocation, loc)) return;
            if (in.helper.isObstructedWater(in.staticVariables.myLocation, loc)) return;
            //if(in.helper.getClosestTownToLocation(loc).distanceSquared(loc) < in.constants.WORKERS_CONSIDER_ClOSE_DISTANCE)
                //return;
            int[] objective = in.objectives.createResourceObjective(loc);
            in.memoryManager.addObjective(UnitType.WORKER, objective);
        }
    }

    public UnitInfo getClosestAlliedUnitToLocation(UnitType unitType) {
        return null;
    }

    public void markTower(Location loc, boolean isNeutral) {
        if (in.memoryManager.getUnitFromLocation(loc) == UnitType.TOWER && in.memoryManager.getPaintedTower(loc)) return;
        in.memoryManager.saveUnitToMap(loc, UnitType.TOWER);
        in.memoryManager.markTowerForPainting(loc);

        int type = isNeutral ? in.constants.NEUTRAL_TOWER : in.constants.ENEMY_TOWER;

        int[] objective = in.objectives.createCatapultObjective(loc, type);
        in.memoryManager.addObjective(UnitType.CATAPULT, objective);

        if(in.unitController.getEnergyLeft() < 6000) return;

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
        in.memoryManager.unmarkTowerForPainting(loc);
    }

    public void unmarkTower(Location loc) {
        if (in.memoryManager.getUnitFromLocation(loc) != UnitType.TOWER) return;
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
                    in.memoryManager.setLocationDangerous(new Location (loc.x + i, loc.y + j));
                    in.memoryManager.setLocationDangerous(new Location (loc.x - i, loc.y + j));
                    in.memoryManager.setLocationDangerous(new Location (loc.x + i, loc.y - j));
                    in.memoryManager.setLocationDangerous(new Location (loc.x - i, loc.y - j));
                }
            }
        }
    }


}
