import com.google.common.collect.Sets;
import soot.jimple.toolkits.annotation.logic.Loop;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by yelite on 2016/10/27.
 */
public class LoopTrap {
    protected final LoopTrap previous;
    private final Loop loop;
    private final boolean root;

    public final FootPrintSet traverseFP = new FootPrintSet();
    public final FootPrintSet readFP = new FootPrintSet();

    public LoopTrap() {
        this.loop = null;
        this.previous = null;
        this.root = true;
    }

    public LoopTrap(Loop loop, LoopTrap previous) {
        this.loop = loop;
        this.previous = previous.copy();
        this.root = false;
    }

    public boolean inLoop() {
        return this.previous != null;
    }

    public LoopTrap copy() {
        LoopTrap other;
        if (root) {
            other = new LoopTrap();
        } else {
            other = new LoopTrap(this.loop, this.previous);
        }
        other.traverseFP.fillFrom(this.traverseFP);
        other.readFP.fillFrom(this.readFP);
        return other;
    }

    public LoopTrap enterLoop(Loop newLoop) {
        Objects.requireNonNull(newLoop);
        return new LoopTrap(newLoop, this);
    }

    public LoopTrap merge(LoopTrap other) {
        LoopTrap newTrap;

        if (!this.root) {
            newTrap = new LoopTrap(this.loop, this.previous);
        } else if (!other.root) {
            newTrap = new LoopTrap(other.loop, other.previous);
        } else {
            newTrap = new LoopTrap();
        }

        newTrap.readFP.addAll(this.readFP.union(other.readFP));
        newTrap.traverseFP.addAll(this.traverseFP.union(other.traverseFP));

        return newTrap;
    }

    public void addTraverseFP(Symbol s) {
        traverseFP.add(s);
    }

    public void addReadFP(Symbol s) {
        readFP.add(s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root, loop, traverseFP, readFP, previous);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof LoopTrap)) {
            return false;
        }
        if (other == this) {
            return true;
        }

        LoopTrap rhs = (LoopTrap) other;
        return Objects.equals(this.traverseFP, rhs.traverseFP)
                && Objects.equals(this.readFP, rhs.readFP)
                && Objects.equals(this.loop, rhs.loop)
                && Objects.equals(this.root, rhs.root)
                && Objects.equals(this.previous, rhs.previous);
    }
}
