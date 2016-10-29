import javafx.util.Pair;
import soot.Local;
import soot.Scene;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.internal.JimpleLocal;

import java.util.*;

/**
 * Created by yelite on 2016/10/24.
 */
public class Symbol {
    // globalValue variable as static namespace
    static public final Local globalValue = new JimpleLocal("@Global", Scene.v().getObjectType());
    static public final Symbol returnSymbol = new Symbol(new JimpleLocal("@Return", Scene.v().getObjectType()));

    public final Local variable;
    private final LinkedList<GeneralField> fields = new LinkedList<>();
    private int hash = 0;
    public final Set<Local> locals = new HashSet<>();
    public final Set<Pair<Local, Field>> shortFields = new HashSet<>();
    public final Set<Pair<Local, Local>> simpleIndices = new HashSet<>();

    public Symbol(Local variable) {
        this.variable = variable;
        generateInvolvedThings();
    }

    public Symbol(Local variable, GeneralField field) {
        this.variable = variable;
        this.fields.add(field);
        generateInvolvedThings();
    }

    public Symbol(Local variable, List<GeneralField> fields) {
        this.variable = variable;
        this.fields.addAll(fields);
        generateInvolvedThings();
    }

    public static Symbol fromLimitedLocal(Value value) {
        if (value instanceof Local)
        {
            return new Symbol((Local) value);
        }
        else if (value instanceof StaticFieldRef) {
            StaticFieldRef ref = (StaticFieldRef) value;

            String field = ref.getField().getName() + "@" + ref.getField().getDeclaringClass();

            return new Symbol(globalValue, new Field(field));
        }
        else if (value instanceof InstanceFieldRef)
        {
            InstanceFieldRef ref = (InstanceFieldRef) value;

            Value base = ref.getBase();
            assert (base instanceof Local);

            String field = ref.getField().getName();

            return new Symbol((Local) base, new Field(field));
        }
        else
        {
            assert (value instanceof ArrayRef);
            ArrayRef ref = (ArrayRef) value;

            Value base = ref.getBase();
            assert (base instanceof Local);

            Value index = ref.getIndex();
            if (index instanceof IntConstant) {
                return new Symbol((Local) base, new ConstantIndex(((IntConstant) index).value));
            } else {
                assert (index instanceof Local);
                return new Symbol((Local) base, ArrayIndex.fromLocal((Local) index));
            }
        }
    }

    public Symbol substituteLocal(Local oldLocal, Symbol newSymbol) {
        LinkedList<GeneralField> newFields = new LinkedList<>();
        Local newVariable;

        if (variable.equivHashCode() == oldLocal.equivHashCode()) {
            newVariable = newSymbol.variable;
            newFields.addAll(newSymbol.fields);
        } else {
            newVariable = this.variable;
        }

        for (GeneralField f : fields) {
            if (f instanceof ArrayIndex) {
                newFields.add(new ArrayIndex(((ArrayIndex) f).index.substituteLocal(oldLocal, newSymbol)));
            } else {
                newFields.add(f);
            }
        }

        return new Symbol(newVariable, newFields);
    }

    public Symbol substituteGeneralField(Local oldLocal, GeneralField oldField, Symbol newSymbol) {
        LinkedList<GeneralField> newFields = new LinkedList<>();
        Local newVariable;
        int skip = 0;

        if (variable.equivHashCode() == oldLocal.equivHashCode()
                && (!this.isPure() && fields.getFirst().equals(oldField))) {
            newVariable = newSymbol.variable;
            newFields.addAll(newSymbol.fields);
            skip = 1;
        } else {
            newVariable = this.variable;
        }

        ListIterator<GeneralField> iter = fields.listIterator(skip);
        while (iter.hasNext()) {
            GeneralField f = iter.next();
            if (f instanceof ArrayIndex) {
                newFields.add(new ArrayIndex(
                        ((ArrayIndex) f).index.substituteGeneralField(oldLocal, oldField, newSymbol))
                );
            } else {
                newFields.add(f);
            }
        }

        return new Symbol(newVariable, newFields);
    }

    public boolean isPure() {
        return (this.fields.isEmpty());
    }

    private void generateInvolvedThings() {
        locals.addAll(involvedLocals());
        shortFields.addAll(involvedShortFields());
        simpleIndices.addAll(involvedSimpleIndices());
    }

    private Collection<Local> involvedLocals() {
        LinkedList<Local> result = new LinkedList<>();
        result.add(variable);
        for (GeneralField f : fields) {
            if (f instanceof ArrayIndex) {
                result.addAll(((ArrayIndex) f).index.locals);
            }
        }
        return result;
    }

    private Collection<Pair<Local, Field>> involvedShortFields() {
        LinkedList<Pair<Local, Field>> result = new LinkedList<>();
        if (!isPure() && fields.getFirst() instanceof Field) {
            result.add(new Pair<>(variable, (Field) fields.getFirst()));
        }

        for (GeneralField f : fields) {
            if (f instanceof ArrayIndex) {
                result.addAll(((ArrayIndex) f).index.shortFields);
            }
        }

        return result;
    }

    private Collection<Pair<Local, Local>> involvedSimpleIndices() {
        LinkedList<Pair<Local, Local>> result = new LinkedList<>();
        if (!isPure()
                && fields.getFirst() instanceof ArrayIndex
                && ((ArrayIndex) fields.getFirst()).index.isPure()) {
            result.add(new Pair<>(variable, ((ArrayIndex) fields.getFirst()).index.variable));
        }

        for (GeneralField f : fields) {
            if (f instanceof ArrayIndex) {
                result.addAll(((ArrayIndex) f).index.simpleIndices);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return this.variable.toString() + "." + fields.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable.getName(), fields);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Symbol))
            return false;
        if (other == this)
            return true;

        Symbol rhs = (Symbol) other;
        return Objects.equals(this.variable.getName(), rhs.variable.getName())
            && Objects.equals(this.fields, rhs.fields);
    }
}
