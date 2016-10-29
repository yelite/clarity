import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ConcreteRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.annotation.logic.Loop;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by yelite on 2016/10/24.
 */
public class FootPrintFlowSet {
    private final FootPrintSet traverseFP = new FootPrintSet();
    private final FootPrintSet writeFP = new FootPrintSet();
    private final LoopTrap loopTrap;
    private boolean top;

    public FootPrintFlowSet() {
        this.loopTrap = new LoopTrap();
        this.top = true;
    }

    public FootPrintFlowSet(boolean top) {
        this.loopTrap = new LoopTrap();
        this.top = top;
    }

    public FootPrintFlowSet(LoopTrap trap, boolean top) {
        this.loopTrap = trap;
        this.top = top;
    }

    public FootPrintFlowSet copyWithMerge(FootPrintFlowSet other) {
        LoopTrap newTrap = this.loopTrap.merge(other.loopTrap);

        FootPrintFlowSet result = new FootPrintFlowSet(newTrap, this.top && other.top);
        result.traverseFP.addAll(this.traverseFP.union(other.traverseFP));
        result.writeFP.addAll(this.writeFP.union(other.writeFP));
        return result;
    }

    private void fillFP(FootPrintFlowSet other) {
        other.top = this.top;
        other.traverseFP.fillFrom(this.traverseFP);
        other.writeFP.fillFrom(this.writeFP);
    }

    public FootPrintFlowSet copy() {
        FootPrintFlowSet result = new FootPrintFlowSet(this.loopTrap.copy(), this.top);
        fillFP(result);
        return result;
    }

    public FootPrintFlowSet copyWithEnteringLoop(Loop loop) {
        FootPrintFlowSet result = new FootPrintFlowSet(this.loopTrap.enterLoop(loop), this.top);
        fillFP(result);
        return result;
    }

    public FootPrintFlowSet copyWithExitingLoop() {
        FootPrintSet loopTraverseFP = this.loopTrap.traverseFP;
        FootPrintSet loopReadFP = this.loopTrap.readFP;
        FootPrintFlowSet result;

        result = new FootPrintFlowSet(this.loopTrap.previous.copy(), this.top);
        result.traverseFP.addAll(this.traverseFP.difference(loopTraverseFP));
        result.traverseFP.fillFrom(loopReadFP);

        result.writeFP.fillFrom(this.writeFP);

        return result;
    }

    public FootPrintFlowSet copyWithExitingMethod(SootMethod calledMethod, InvokeExpr expr, Map<Integer, Local> paraMap) {
        FootPrintFlowSet out = this.copy();

        if (expr instanceof InstanceInvokeExpr) {
            Value base = ((InstanceInvokeExpr) expr).getBase();
            assert (base instanceof Local);
            out.subsituteByValue(
                    paraMap.get(MethodSummary.receiver),
                    Symbol.fromLimitedLocal(base)
            );
        }

        for (int i = 0; i < calledMethod.getParameterCount(); i++) {
            Value arg = expr.getArg(i);
            if (arg instanceof Local || arg instanceof ConcreteRef) {
                out.subsituteByValue(paraMap.get(i), Symbol.fromLimitedLocal(arg));
            }
        }

        return out;
    }

    public FootPrintFlowSet copyWithEnteringMethod(Local returnValue) {
        FootPrintFlowSet result = new FootPrintFlowSet(this.top);
        result.traverseFP.addAll(this.traverseFP.filter(returnValue));
        result.writeFP.addAll(this.writeFP.filter(returnValue));
        result.loopTrap.traverseFP.addAll(this.loopTrap.traverseFP.filter(returnValue));
        result.loopTrap.readFP.addAll(this.loopTrap.readFP.filter(returnValue));
        result.subsituteByValue(returnValue, Symbol.returnSymbol);
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
        this.top = false;
        boolean added = traverseFP.add(s);
        if (added) {
            loopTrap.addTraverseFP(s);
        }
    }

    public void addWriteFP(Symbol s) {
        this.top = false;
        writeFP.add(s);
    }

    public void addReadFP(Symbol s) {
        this.top = false;
        loopTrap.addReadFP(s);
    }

    public void subsituteByValue(Value oldValue, Symbol newSymbol) {
        this.traverseFP.subsituteByValue(oldValue, newSymbol);
        this.writeFP.subsituteByValue(oldValue, newSymbol);
        this.loopTrap.readFP.subsituteByValue(oldValue, newSymbol);
        this.loopTrap.traverseFP.subsituteByValue(oldValue, newSymbol);
    }

    public boolean checkProblem() {
        // Don't check static fields
        return !traverseFP.difference(writeFP).stream()
                .filter(s -> !s.locals.contains(Symbol.globalValue))
                .collect(Collectors.toSet())
                .isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(traverseFP, writeFP, loopTrap, top);
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
                && Objects.equals(this.loopTrap, rhs.loopTrap)
                && Objects.equals(this.top, rhs.top);
    }
}
