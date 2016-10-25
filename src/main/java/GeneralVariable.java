
import soot.Local;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by yelite on 2016/10/24.
 */
public class GeneralVariable {
    private final Local variable;
    private final LinkedList<String> fields = new LinkedList<>();
    private int hash = 0;

    public GeneralVariable(Local variable) {
        this.variable = variable;
    }

    public GeneralVariable(Local variable, List<String> fields) {
        this.variable = variable;
        this.fields.addAll(fields);
    }

    public GeneralVariable subsituteHead(Local newLocal, String field) {
        LinkedList<String> newFields = new LinkedList<>();
        newFields.add(field);
        newFields.addAll(fields);
        return new GeneralVariable(newLocal, newFields);
    }

    public boolean isPure() {
        return (this.fields.isEmpty());
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            String[] f = fields.toArray(new String[fields.size()]);
            hash = Objects.hash(variable.getName(), f);
        }
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GeneralVariable))
            return false;
        if (other == this)
            return true;

        GeneralVariable rhs = (GeneralVariable) other;
        return Objects.equals(this.variable.getName(), rhs.variable.getName())
            && Objects.equals(this.fields, rhs.fields);
    }
}
