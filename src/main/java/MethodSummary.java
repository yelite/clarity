import soot.*;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.annotation.logic.Loop;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yelite on 2016/10/26.
 */

public class MethodSummary {
    protected Map<SootMethod, Map<Stmt, Loop>> loopHeaders = new HashMap<>();
    protected Map<SootMethod, Map<Stmt, Loop>> loopExits = new HashMap<>();
    private Map<SootMethod, Map<Integer, Local>> paraMap = new HashMap<>();

    static public Integer receiver = -1;


    public MethodSummary() {}


    public void addParameterMapping(SootMethod m, Body body) {
        Map<Integer, Local> mapping = new HashMap<>();

        for (Unit u : body.getUnits()) {
            if (u instanceof IdentityStmt) {
                IdentityStmt stmt = (IdentityStmt) u;
                Value lhsValue = stmt.getLeftOpBox().getValue();
                assert (lhsValue instanceof Local);
                Local lhs = (Local) lhsValue;


                Integer rhs;
                Value rhsValue = stmt.getRightOpBox().getValue();
                if (rhsValue instanceof ParameterRef) {
                    ParameterRef rhsRef = (ParameterRef) rhsValue;
                    rhs = rhsRef.getIndex();
                } else if (rhsValue instanceof ThisRef) {
                    rhs = -1;
                } else {
                    rhs = -2;
                }

                mapping.put(rhs, lhs);
            }
        }

        paraMap.put(m, mapping);
    }

    public Map<Integer, Local> getParameterMapping(SootMethod m) {
        return paraMap.get(m);
    }


    public void addLoop(SootMethod m, Collection<Loop> loops) {
        Map<Stmt, Loop> methodHeaders = new HashMap<>();
        Map<Stmt, Loop> methodExits = new HashMap<>();

        for (Loop l : loops) {
            methodHeaders.put(l.getHead(), l);
            for (Stmt s : l.getLoopExits()) {
                methodExits.put(s, l);
            }
        }

        this.loopHeaders.put(m, methodHeaders);
        loopExits.put(m, methodExits);
    }


    public Loop getLoopFromHeader(SootMethod m, Stmt h) {
        Map<Stmt, Loop> loops = loopHeaders.get(m);
        if (loops == null) {
            return null;
        } else {
            return loops.get(h);
        }
    }


    public Loop getLoopFromExit(SootMethod m, Stmt e) {
        Map<Stmt, Loop> loops = loopExits.get(m);
        if (loops == null) {
            return null;
        } else {
            return loops.get(e);
        }
    }
}
