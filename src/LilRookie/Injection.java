package LilRookie;

import aic2019.*;

public class Injection {

    public final UnitController unitController;
    public final Constants constants = new Constants();
    public final StaticVariables staticVariables;
    public final MemoryManager memoryManager;
    public final Attack attack;
    public final Move move;
    public final Pathfinder pathfinder;
    public final Soldier soldier;
    public final Worker worker;


    Injection (UnitController uc) {
        this.unitController = uc;
        this.staticVariables = new StaticVariables(this);
        this.memoryManager = new MemoryManager(this);
        this.attack = new Attack(this);
        this.move = new Move(this);
        this.pathfinder = new Pathfinder(this);
        this.soldier = new Soldier(this);
        this.worker = new Worker(this);
    }
}
