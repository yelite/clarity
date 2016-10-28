import soot.Local;
import soot.Value;
import soot.jimple.toolkits.annotation.logic.Loop;

import java.util.Objects;

/**
 * Created by yelite on 2016/10/24.
 */
public class FootPrintFlowSet {
    public final FootPrintSet traverseFP = new FootPrintSet();
    public final FootPrintSet writeFP = new FootPrintSet();
    public final LoopTrap loopTrap;

    public FootPrintFlowSet() {
        this.loopTrap = new LoopTrap();
    }

    public FootPrintFlowSet(LoopTrap trap) {
        this.loopTrap = trap;
    }

    public FootPrintFlowSet copyWithMerge(FootPrintFlowSet other) {
        LoopTrap newTrap = this.loopTrap.merge(other.loopTrap);

        FootPrintFlowSet result = new FootPrintFlowSet(newTrap);
        result.traverseFP.addAll(this.traverseFP.union(other.traverseFP));
        result.writeFP.addAll(this.writeFP.intersection(other.writeFP));
        return result;
    }

    private void fillFP(FootPrintFlowSet other) {
        other.traverseFP.fillFrom(this.traverseFP);
        other.writeFP.fillFrom(this.writeFP);
    }

    public FootPrintFlowSet copy() {
        FootPrintFlowSet result = new FootPrintFlowSet(this.loopTrap.copy());
        fillFP(result);
        return result;
    }

    public FootPrintFlowSet copyWithEnteringLoop(Loop loop) {
        FootPrintFlowSet result = new FootPrintFlowSet(this.loopTrap.enterLoop(loop));
        fillFP(result);
        return result;
    }

    public FootPrintFlowSet copyWithExitingLoop() {
        if (!this.loopTrap.inLoop()) {
            System.err.println("Exiting a loop without entering any");
        }

        FootPrintSet loopTraverseFP = this.loopTrap.traverseFP;
        FootPrintSet loopReadFP = this.loopTrap.readFP;
        FootPrintFlowSet result = new FootPrintFlowSet(this.loopTrap.previous);

        result.traverseFP.addAll(this.traverseFP.difference(loopTraverseFP));
        result.traverseFP.fillFrom(loopReadFP);

        return result;
    }

    public FootPrintFlowSet copyWithEnteringMethod(Local returnValue) {
        FootPrintFlowSet result = new FootPrintFlowSet();
        result.traverseFP.addAll(this.traverseFP.filter(returnValue));
        result.writeFP.addAll(this.writeFP.filter(returnValue));
        result.loopTrap.traverseFP.addAll(this.loopTrap.traverseFP.filter(returnValue));
        result.loopTrap.readFP.addAll(this.loopTrap.readFP.filter(returnValue));
        return result;
    }

    public FootPrintFlowSet copyWithoutReturnValue(Local returnValue) {
        FootPrintFlowSet result = this.copy();
        result.traverseFP.remove(returnValue);
        result.writeFP.remove(returnValue);
        result.loopTrap.traverseFP.remove(returnValue);
        result.loopTrap.readFP.remove(returnValue);
        return result;
    }

    public void addTraverseFP(Symbol s) {
        traverseFP.add(s);
        loopTrap.addTraverseFP(s);
    }

    public void addWriteFP(Symbol s) {
        writeFP.add(s);
    }

    public void addReadFP(Symbol s) {
        loopTrap.addReadFP(s);
    }

    public void subsituteByValue(Value oldValue, Symbol newSymbol) {
        this.traverseFP.subsituteByValue(oldValue, newSymbol);
        this.writeFP.subsituteByValue(oldValue, newSymbol);
        this.loopTrap.readFP.subsituteByValue(oldValue, newSymbol);
        this.loopTrap.traverseFP.subsituteByValue(oldValue, newSymbol);
    }

    public boolean check() {
        return !traverseFP.difference(writeFP).isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(traverseFP, writeFP, loopTrap);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof FootPrintFlowSet)) {
            return false;
        }
        if (other == this) {
            return true;
        }

        FootPrintFlowSet rhs = (FootPrintFlowSet) other;
        return Objects.equals(this.traverseFP, rhs.traverseFP)
                && Objects.equals(this.writeFP, rhs.writeFP)
                && Objects.equals(this.loopTrap, rhs.loopTrap);
    }
}
