import soot.Local;

import java.util.Objects;

/**
 * Created by yelite on 2016/10/26.
 */
public class ArrayIndex implements GeneralField {
    public final Symbol index;

    public ArrayIndex(Symbol index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "[" + index.toString() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(index) * 4 + 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof ArrayIndex)) {
            return false;
        }
        if (other == this) {
            return true;
        }

        ArrayIndex rhs = (ArrayIndex) other;
        return Objects.equals(this.index, rhs.index);
    }

    static public ArrayIndex fromLocal(Local index) {
        return new ArrayIndex(new Symbol(index));
    }
}
