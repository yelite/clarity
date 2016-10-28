import java.util.Objects;

/**
 * Created by yelite on 2016/10/26.
 */
public class ConstantIndex implements GeneralField {
    public final int index;

    public ConstantIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "[" + String.valueOf(index) + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(index) * 4 + 2;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof ConstantIndex)) {
            return false;
        }
        if (other == this) {
            return true;
        }

        ConstantIndex rhs = (ConstantIndex) other;
        return Objects.equals(this.index, rhs.index);
    }
}
