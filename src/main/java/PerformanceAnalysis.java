import soot.*;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;
import vasco.DataFlowSolution;
import vasco.callgraph.CallGraphTransformer;

import java.util.*;

/**
 * Created by yelite on 2016/9/22.
 */

public class PerformanceAnalysis extends SceneTransformer {
    static public void main(String[] argv) throws Exception {
        CallGraphTransformer cgt = new CallGraphTransformer();

        Pack pack = PackManager.v().getPack("wjtp");
        pack.add(new Transform("wjtp.fcpa", cgt));
        pack.add(new Transform("wjtp.RedundantIterationDetection", new PerformanceAnalysis()));

        List<String> args = new LinkedList<>(Arrays.asList(argv));

        String classPath = "";
        Iterator<String> iter = args.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals("-cp")) {
                iter.remove();
                classPath = iter.next();
                iter.remove();
            }
        }

        if (args.size() == 0) {
            System.err.println("Please specify your main class");
        }
        String mainClass = args.get(0);

        // copy from vasco
        String[] soot_args = {
                "-cp", classPath,
                "-w", "-app",
                "-keep-line-number",
                "-keep-bytecode-offset",
                "-p", "cg", "implicit-entry:false",
                "-p", "cg.spark", "enabled",
                "-p", "cg.spark", "simulate-natives",
                "-p", "cg", "verbose",
                "-p", "cg", "safe-forname",
                "-p", "cg", "safe-newinstance",
                "-main-class", mainClass,
                "-f", "none", mainClass
        };

        soot.Main.main(soot_args);
    }

    protected void internalTransform(String phaseName, Map<String, String> options) {
        MethodSummary methodSummary = generateSummary();

        FootPrintAnalysis fp = new FootPrintAnalysis(methodSummary);
        fp.doAnalysis();
        DataFlowSolution<Unit, FootPrintFlowSet> solution = fp.getSolutionForUserCode();

        methodSummary.loopHeaders.entrySet().stream()
                .filter(entry -> !entry.getKey().isJavaLibraryMethod())
                .forEach(entry -> {
                    SootMethod method = entry.getKey();
                    entry.getValue().forEach((stmt, loop) -> {
                        FootPrintFlowSet flow = solution.getValueAfter(stmt);
                        if (!flow.check()) {
                            System.out.println(method + " has possible performance bug.");
                        }
                    });
                });
    }

    private MethodSummary generateSummary() {
        MethodSummary methodSummary = new MethodSummary();

        Scene s = Scene.v();
        ReachableMethods methods = s.getReachableMethods();;

        QueueReader<MethodOrMethodContext> queueReader = methods.listener();
        while (queueReader.hasNext()) {
            SootMethod method = queueReader.next().method();

            LoopFinder lf = new LoopFinder();
            lf.transform(method.getActiveBody());
            methodSummary.addLoop(method, lf.loops());

            methodSummary.addParameterMapping(method, method.getActiveBody());
        }

        return methodSummary;
    }
}
