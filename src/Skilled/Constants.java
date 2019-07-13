package Skilled;

public class Constants {

    // MEMORY CONSTANTS

    public int ID_ALLIES_COUNTER = 1; // each counter uses 7 positions
    public int ID_ALLIES_SOLDIER_COUNTER = 8;
    public int ID_ALLIES_ARCHER_COUNTER = 15;
    public int ID_ALLIES_KNIGHT_COUNTER = 22;
    public int ID_ALLIES_CATAPULT_COUNTER = 29;
    public int ID_ALLIES_MAGE_COUNTER = 36;
    public int ID_ALLIES_WORKERS_COUNTER = 43;
    public int ID_MESSAGING_BOX = 100; // 40000 positions
    public int ID_OBJECTIVES = 50000; // 3000 positions: 10 unit types * 20 objectives * 15
    public int ID_LOCATION_OBJECTIVES = 60000; // 10000 positions
    public int ID_MAP_INFO = 70000; // 50000 positions

    public int MESSAGE_SIZE = 4;
    public int MAX_MESSAGES_INBOX = 10;
    public int OBJECTIVE_SIZE = 15;
    public int MAX_OBJECTIVES = 20;
    public int INFO_PER_CELL = 5;
    public int COUNTERS_SPACE = 7;


    // OBJECTIVE CONSTANTS
    public int WORKERS_GET_WOOD = 1;
    public int WORKERS_GET_IRON = 2;
    public int WORKERS_GET_CRYSTAL = 3;
    public int WORKERS_DEPOSIT_POSITION = 4;
    public int ENEMY_TOWER = 5;

    // UNITS
    public int CATAPULTS_CONSIDER_COSE_DISTANCE = 300;
    public int WORKERS_CONSIDER_ClOSE_DISTANCE = 300;
    public float WORKERS_GATHER_WOOD = 60.f;
    public float WORKERS_GATHER_IRON = 20.f;
    public float WORKERS_GATHER_MINERAL = 6.f;


}
