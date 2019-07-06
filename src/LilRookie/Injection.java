package LilRookie;

import aic2019.*;

public class Injection {

    public final UnitController unitController;
    public final Constants constants = new Constants();
    public final StaticVariables staticVariables;
    public final MemoryManager memoryManager;
    public final Attack attack;


    Injection (UnitController uc) {
        this.unitController = uc;
        this.staticVariables = new StaticVariables(this);
        this.memoryManager = new MemoryManager(this);
        this.attack = new Attack(this);
    }
}
