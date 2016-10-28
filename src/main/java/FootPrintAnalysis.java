import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.annotation.logic.Loop;
import vasco.BackwardInterProceduralAnalysis;
import vasco.Context;
import vasco.DataFlowSolution;
import vasco.ProgramRepresentation;
import vasco.soot.DefaultJimpleRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yelite on 2016/10/24.
 */
public class FootPrintAnalysis extends BackwardInterProceduralAnalysis<SootMethod, Unit, FootPrintFlowSet> {
    private final MethodSummary methodSummary;
    private static final Local returnLocal = new JimpleLocal("@return", IntType.v());

    public FootPrintAnalysis(MethodSummary methodSummary) {
        super();
        this.methodSummary = methodSummary;
    }

    @Override
    public FootPrintFlowSet boundaryValue(SootMethod entryPoint) {
        return new FootPrintFlowSet();
    }

    @Override
    public FootPrintFlowSet copy(FootPrintFlowSet src) {
        return src.copy();
    }

    @Override
    public FootPrintFlowSet meet(FootPrintFlowSet op1, FootPrintFlowSet op2) {
        return op1.copyWithMerge(op2);
    }

    @Override
    public ProgramRepresentation<SootMethod, Unit> programRepresentation() {
        return DefaultJimpleRepresentation.v();
    }

    @Override
    public FootPrintFlowSet topValue() {
        return new FootPrintFlowSet();
    }

    @Override
    protected void computeOutFlow(Context<SootMethod, Unit, FootPrintFlowSet> currentContext, Unit node) {
        List<Unit> successors = currentContext.getControlFlowGraph().getSuccsOf(node);
        SootMethod method = currentContext.getMethod();
        if (successors.size() != 0) {
            FootPrintFlowSet out = topValue();
            for (Unit succ : successors) {
                FootPrintFlowSet succIn = currentContext.getValueBefore(succ);

                assert (succ instanceof Stmt);
                Loop headerLoop = methodSummary.getLoopFromHeader(method, (Stmt) succ);
                Loop exitLoop = methodSummary.getLoopFromExit(method, (Stmt) node);

                if (headerLoop != null && headerLoop.getLoopStatements().contains(node)) {
                    out = meet(out, succIn.copyWithExitingLoop());
                } else if (exitLoop != null && exitLoop.getLoopStatements().contains(succ)) {
                    out = meet(out, succIn.copyWithEnteringLoop(exitLoop));
                } else {
                    out = meet(out, succIn);
                }
            }
            currentContext.setValueAfter(node, out);
        }
    }

    @Override
    public FootPrintFlowSet normalFlowFunction(Context<SootMethod, Unit, FootPrintFlowSet> context, Unit node, FootPrintFlowSet outValue) {
        assert (node instanceof Stmt);
        Stmt stmt = (Stmt) node;
        SootMethod method = context.getMethod();

        Loop headerLoop = methodSummary.getLoopFromHeader(method, stmt);
        Loop exitLoop = methodSummary.getLoopFromExit(method, stmt);
        FootPrintFlowSet in;

        assert(headerLoop == null || exitLoop == null);
        if (headerLoop != null) {
            in = outValue.copyWithExitingLoop();
        } else if (exitLoop != null) {
            in = outValue.copyWithEnteringLoop(exitLoop);
        } else {
            in = outValue.copy();
        }

        if (stmt instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) stmt;
            Value lhsValue = assignStmt.getLeftOpBox().getValue();
            Value rhsValue = assignStmt.getRightOpBox().getValue();
            Symbol lhsSymbol = Symbol.fromLimitedLocal(lhsValue);

            if (lhsValue instanceof ArrayRef) {
                in.addWriteFP(new Symbol(lhsSymbol.variable));
            }

            if (rhsValue instanceof ArrayRef) {
                ArrayRef ref = (ArrayRef) rhsValue;
                if (ref.getIndex() instanceof Local) {
                    in.addReadFP(Symbol.fromLimitedLocal(ref.getBase()));
                }
            }

            if (rhsValue instanceof ConcreteRef || rhsValue instanceof Local) {
                Symbol rhsSymbol = Symbol.fromLimitedLocal(rhsValue);
                in.subsituteByValue(lhsValue, rhsSymbol);
            } else if (rhsValue instanceof NewArrayExpr) {
                in.addWriteFP(lhsSymbol);
            }
        }

        return in;
    }

    @Override
    public FootPrintFlowSet callEntryFlowFunction(Context<SootMethod, Unit, FootPrintFlowSet> context, SootMethod targetMethod, Unit node, FootPrintFlowSet entryValue) {
        FootPrintFlowSet out = entryValue.copy();
        Map<Integer, Local> paraMap = methodSummary.getParameterMapping(targetMethod);
        InvokeExpr exp;

        if (node instanceof AssignStmt) {
            Value rhs = ((AssignStmt) node).getRightOp();
            assert (rhs instanceof InvokeExpr);
            exp = (InvokeExpr) rhs;
        } else {
            assert (node instanceof InvokeStmt);
            exp = ((InvokeStmt) node).getInvokeExpr();
        }

        if (exp instanceof InstanceInvokeExpr) {
            Value base = ((InstanceInvokeExpr) exp).getBase();
            assert (base instanceof Local);
            out.subsituteByValue(
                    paraMap.get(MethodSummary.receiver),
                    Symbol.fromLimitedLocal(base)
            );
        }

        for (int i = 0; i < targetMethod.getParameterCount(); i++) {
            Value arg = exp.getArg(i);
            if (arg instanceof Local || arg instanceof ConcreteRef) {
                out.subsituteByValue(paraMap.get(i), Symbol.fromLimitedLocal(arg));
            }
        }

        return out;
    }

    @Override
    public FootPrintFlowSet callExitFlowFunction(Context<SootMethod, Unit, FootPrintFlowSet> context, SootMethod targetMethod, Unit node, FootPrintFlowSet outValue) {
        FootPrintFlowSet exitValue;
        if (node instanceof AssignStmt) {
            Value lhs = ((AssignStmt) node).getLeftOp();
            assert (lhs instanceof Local);
            exitValue = outValue.copyWithEnteringMethod((Local) lhs);
        } else {
            exitValue = new FootPrintFlowSet();
        }
        return exitValue;
    }

    @Override
    public FootPrintFlowSet callLocalFlowFunction(Context<SootMethod, Unit, FootPrintFlowSet> context, Unit node, FootPrintFlowSet outValue) {
        FootPrintFlowSet exitValue;
        if (node instanceof AssignStmt) {
            Value lhs = ((AssignStmt) node).getLeftOp();
            assert (lhs instanceof Local);
            exitValue = outValue.copyWithoutReturnValue((Local) lhs);
        } else {
            exitValue = outValue.copy();
        }
        return exitValue;
    }

    public DataFlowSolution<Unit, FootPrintFlowSet> getSolutionForUserCode() {
        Map<Unit, FootPrintFlowSet> inValues = new HashMap<>();
        Map<Unit, FootPrintFlowSet> outValues = new HashMap<>();
        for (SootMethod method : contexts.keySet()) {
            if (method.isJavaLibraryMethod()) {
                continue;
            }
            for (Unit node : programRepresentation().getControlFlowGraph(method)) {
                FootPrintFlowSet in = topValue();
                FootPrintFlowSet out = topValue();
                for (Context<SootMethod, Unit, FootPrintFlowSet> context : contexts.get(method)) {
                    in = meet(in, context.getValueBefore(node));
                    out = meet(out, context.getValueAfter(node));
                }
                inValues.put(node, in);
                outValues.put(node, out);
            }
        }
        // Return data flow solution
        return new DataFlowSolution<>(inValues, outValues);
    }
}
