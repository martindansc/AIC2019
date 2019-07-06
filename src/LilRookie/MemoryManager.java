package LilRookie;

import aic2019.*;

public class MemoryManager {

    private final UnitController uc;
    private final Constants constants = new Constants();
    private final Injection in;

    // GENERAL

    public MemoryManager(Injection in) {
        this.in = in;
        this.uc = in.unitController;
    }

    public void update() {
        this.increaseValueByOne(constants.ALLIES_COUNTER);
    }

    // HELPERS


    // SIMPLE COUNTERS FUNCTION


    public void increaseValue(int key, int ammount) {
        int realId = key + uc.getRound()%3;
        int value = uc.read(realId);
        uc.write(realId, value + ammount);
    }

    public void increaseValueByOne(int key) {
        this.increaseValue(key, 1);
    }

    public int readValue(int key) {
        uc.write(key + (uc.getRound() + 1)%3, 0);
        int realId = key + (uc.getRound() - 1)%3;
        return uc.read(realId);
    }



}
