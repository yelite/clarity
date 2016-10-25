import javafx.util.Pair;
import jdk.nashorn.internal.codegen.FunctionSignature;
import soot.Pack;
import soot.PackManager;
import soot.SceneTransformer;
import soot.Transform;
import soot.jimple.toolkits.pointer.LocalMayAliasAnalysis;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yelite on 2016/9/22.
 */

public class PerformanceAnalysis extends SceneTransformer {
    static public void main(String[] argv) throws Exception {
        String[] a = "\0".split(" ");
        System.out.println();
        Thread.sleep(10000);

        Pack pack = PackManager.v().getPack("wjtp");
        Transform transform = new Transform("wjtp.RedundantIterationDetection", PerformanceBugAnalysis.v(mainClassName));
        pack.add(transform);

        soot.Main.main(argv);
    }

    protected void internalTransform(String phaseName, Map<String, String> options) {
        HashMap<String, Pair<LocalMayAliasAnalysis, LocalMustAliasAnalysis>>
    }
}
