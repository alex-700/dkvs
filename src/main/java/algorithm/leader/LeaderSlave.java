package algorithm.leader;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

abstract class LeaderSlave {
    final ConcurrentSkipListSet<Integer> waitors;

    LeaderSlave(Collection<Integer> waitors) {
        this.waitors = new ConcurrentSkipListSet<>(waitors);
    }
}
