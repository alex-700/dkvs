package algorithm.leader;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class LeaderSlave {
    public final ConcurrentSkipListSet<Integer> waitors;

    public LeaderSlave(Collection<Integer> waitors) {
        this.waitors = new ConcurrentSkipListSet<>(waitors);
    }
}