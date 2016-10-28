import soot.Local;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yelite on 2016/10/24.
 */

public class MethodTraverseFootPrint {
    private Map<String, GeneralInput> data = new HashMap<>();

    public MethodTraverseFootPrint() {}

    public void add(GeneralInput newInput) {
        if (data.containsKey(newInput.id)) {
            return;
        } else {
            data.put(newInput.id, newInput);
        }
    }

    public Set<Symbol> instantialize(List<Local> args, boolean isStatic) {
        int start = isStatic? 1 : 0;
        ArrayList<Local> newArgs = new ArrayList<>(args.size() + start);
        if (isStatic) {
            newArgs.add(null);
        }
        newArgs.addAll(args);
        return instantialize(newArgs);
    }

    public Set<Symbol> instantialize(ArrayList<Local> args) {
        return data.values().stream().map(i -> i.instantialize(args.get(i.pos)))
                .collect(Collectors.toSet());
    }
}
