import static java.util.Objects.requireNonNull;
import java.util.Objects;

/**
 * Created by yelite on 2016/10/26.
 */
public class Field implements GeneralField {
    public final String field;

    public Field(String field) {
        this.field = requireNonNull(field);
    }

    @Override
    public String toString() {
        return "." + field;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field) * 4;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Field)) {
            return false;
        }
        if (other == this) {
            return true;
        }

        Field rhs = (Field) other;
        return Objects.equals(this.field, rhs.field);
    }
}
