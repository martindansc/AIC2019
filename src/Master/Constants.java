package Master;

public class Constants {

    // MEMORY CONSTANTS
    public int ID_ALLIES_COUNTER = 1; // each counter uses 7 positions
    public int ID_ALLIES_SOLDIER_COUNTER = 8;
    public int ID_ALLIES_ARCHER_COUNTER = 15;
    public int ID_ALLIES_KNIGHT_COUNTER = 22;
    public int ID_ALLIES_CATAPULT_COUNTER = 29;
    public int ID_ALLIES_MAGE_COUNTER = 36;
    public int ID_ALLIES_WORKERS_COUNTER = 43;
    public int ID_ALLIES_EXPLORERS_COUNTER = 50;
    public int ID_WORKER_BARRACKS = 57;
    public int ID_BARRACKS_BUILT = 58;
    public int ID_BEST_UNIT_TYPE = 59;
    public int ID_CURRENT_OBJECTIVE_RESOURCES = 60;
    public int ID_NUMBER_OF_TOWERS_TO_BUILD_COUNTER = 61;
    public int ID_MESSAGING_BOX = 100; // 40000 positions
    public int ID_OBJECTIVES = 50000; // 3000 positions: 10 unit types * 20 objectives * 15
    public int ID_LOCATION_OBJECTIVES = 60000; // 10000 positions
    public int ID_MAP_INFO = 70000; // 50000 positions

    public int MESSAGE_SIZE = 4;
    public int MAX_MESSAGES_INBOX = 10;
    public int OBJECTIVE_SIZE = 15;
    public int MAX_OBJECTIVES = 20;
    public int INFO_PER_CELL = 5;
    public int COUNTERS_SPACE = 3;

    //HEURISTIC CONSTANTS
    public int HEU_SOLDIER = 160;
    public int HEU_KNIGHT = 80;
    public int HEU_ARCHER = 80;
    public int HEU_MAGE = 30;

    // TOWN CONSTANTS
    public int CLAIMED_TOWN = 1;
    public int STOLEN_TOWN = 2;
    public int ENEMY_TOWN = 3;
    public int CONQUEST_TOWN = 4;

    // OBJECTIVE CONSTANTS
    public int WORKERS_GET_WOOD = 1;
    public int WORKERS_GET_IRON = 2;
    public int WORKERS_GET_CRYSTAL = 3;
    public int WORKERS_DEPOSIT_POSITION = 4;
    public int ENEMY_TOWER = 5;
    public int NEUTRAL_TOWER = 6;
    public int WATER_OBJECTIVE = 7;
    public int DESTROYED_TOWER = 8;
    public int CREATE_TOWER_OBJECTIVE = 9;

    public int FORGET_OBJECTIVE_ROUNDS = 100;

    // UNITS
    public int CATAPULTS_CONSIDER_CLOSE_DISTANCE = 300;
    public int WORKERS_CONSIDER_ClOSE_DISTANCE = 300;
    public float WORKERS_GATHER_ABSOLUTE = 60.f;
    public float WORKERS_GATHER_WOOD = 60.f;
    public float WORKERS_GATHER_IRON = 20.f;
    public float WORKERS_GATHER_MINERAL = 6.f;
}
