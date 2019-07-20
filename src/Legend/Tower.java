package Legend;

import aic2019.Location;

public class Tower {
    private Injection in;

    public Tower(Injection in) {
        this.in = in;
    }

    public void run(Location target) {
        in.attack.genericTryAttack();
        in.attack.genericTryAttackTown(target);
    }
}
