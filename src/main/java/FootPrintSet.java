import com.google.common.collect.Sets;
import javafx.util.Pair;
import soot.Local;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yelite on 2016/10/27.
 */

public class FootPrintSet {
    private final Set<Symbol> symbolSet;

    public FootPrintSet() {
        this.symbolSet = new HashSet<>();
    }

    public Set<Symbol> filter(Local l) {
        return symbolSet.stream().filter(s -> s.locals.contains(l)).collect(Collectors.toSet());
    }

    public void remove(Local l) {
        Iterator<Symbol> iter = symbolSet.iterator();
        Symbol s;
        while (iter.hasNext()) {
            s = iter.next();
            if (s.locals.contains(l)) {
                iter.remove();
            }
        }
    }

    public Map<Local, Set<Symbol>> getLocalTable() {
        Map<Local, Set<Symbol>> result = new HashMap<>();
        symbolSet.forEach(s -> {
            s.locals.forEach(l -> result.compute(l, (k, v) -> addDefault(s, v)));
        });
        return result;
    }

    public Map<Pair<Local, Field>, Set<Symbol>> getFieldTable() {
        Map<Pair<Local, Field>, Set<Symbol>> result = new HashMap<>();
        symbolSet.forEach(s -> {
            s.shortFields.forEach(l -> result.compute(l, (k, v) -> addDefault(s, v)));
        });
        return result;
    }

    public Map<Pair<Local, Local>, Set<Symbol>> getIndexTable() {
        Map<Pair<Local, Local>, Set<Symbol>> result = new HashMap<>();
        symbolSet.forEach(s -> {
            s.simpleIndices.forEach(l -> result.compute(l, (k, v) -> addDefault(s, v)));
        });
        return result;
    }

    static private Set<Symbol> addDefault(Symbol element, Set<Symbol> original) {
        Set<Symbol> result;
        if (original == null) {
            result = new HashSet<>();
        } else {
            result = original;
        }
        result.add(element);
        return result;
    }

    public boolean add(Symbol s) {
        return this.symbolSet.add(s);
    }

    public void addAll(Collection<Symbol> other) {
        this.symbolSet.addAll(other);
    }

    public void fillFrom(FootPrintSet other) {
        addAll(other.symbolSet);
    }

    public void subsituteLocal(Local oldLocal, Symbol newSymbol) {
        Iterator<Symbol> iter = symbolSet.iterator();
        LinkedList<Symbol> newSymbols = new LinkedList<>();
        while (iter.hasNext()) {
            Symbol s = iter.next();
            if (s.locals.contains(oldLocal)) {
                iter.remove();
                newSymbols.add(s.substituteLocal(oldLocal, newSymbol));
            }
        }
        symbolSet.addAll(newSymbols);
    }

    public void subsituteShortField(Local oldLocal, Field oldField, Symbol newSymbol) {
        Pair<Local, Field> idx = new Pair<>(oldLocal, oldField);
        Iterator<Symbol> iter = symbolSet.iterator();
        LinkedList<Symbol> newSymbols = new LinkedList<>();
        while (iter.hasNext()) {
            Symbol s = iter.next();
            if (s.shortFields.contains(idx)) {
                iter.remove();
                newSymbols.add(s.substituteGeneralField(oldLocal, oldField, newSymbol));
            }
        }
        symbolSet.addAll(newSymbols);
    }

    public void subsituteSimpleIndex(Local oldLocal, Local oldIndex, Symbol newSymbol) {
        Pair<Local, Local> idx = new Pair<>(oldLocal, oldIndex);
        GeneralField oldField = ArrayIndex.fromLocal(oldIndex);
        Iterator<Symbol> iter = symbolSet.iterator();
        LinkedList<Symbol> newSymbols = new LinkedList<>();
        while (iter.hasNext()) {
            Symbol s = iter.next();
            if (s.simpleIndices.contains(idx)) {
                iter.remove();
                newSymbols.add(s.substituteGeneralField(oldLocal, oldField, newSymbol));
            }
        }
        symbolSet.addAll(newSymbols);
    }

    public void subsituteByValue(Value oldValue, Symbol newSymbol) {
        if (oldValue instanceof Local)
        {
            subsituteLocal((Local) oldValue, newSymbol);
        }
        else if (oldValue instanceof StaticFieldRef) {
            StaticFieldRef ref = (StaticFieldRef) oldValue;

            String field = ref.getField().getName() + ref.getField().getType();

            subsituteShortField(Symbol.globalValue, new Field(field), newSymbol);
        }
        else if (oldValue instanceof InstanceFieldRef)
        {
            InstanceFieldRef ref = (InstanceFieldRef) oldValue;

            Value base = ref.getBase();
            assert (base instanceof Local);

            String field = ref.getField().getName();

            subsituteShortField((Local) base, new Field(field), newSymbol);
        }
        else
        {
            assert (oldValue instanceof ArrayRef);
            ArrayRef ref = (ArrayRef) oldValue;

            Value base = ref.getBase();
            assert (base instanceof Local);

            Value index = ref.getIndex();
            if (index instanceof Local) {
                subsituteSimpleIndex((Local) base, (Local) index, newSymbol);
            }
        }
    }

    public Set<Symbol> union(FootPrintSet other) {
        return Sets.union(this.symbolSet, other.symbolSet);
    }

    public Set<Symbol> intersection(FootPrintSet other) {
        return Sets.intersection(this.symbolSet, other.symbolSet);
    }

    public Set<Symbol> difference(FootPrintSet other) {
        return Sets.difference(this.symbolSet, other.symbolSet);
    }

    @Override
    public int hashCode() {
        return this.symbolSet.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof FootPrintSet)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return this.symbolSet.equals(((FootPrintSet) other).symbolSet);
    }

}
