import javafx.util.Pair;
import soot.Local;
import soot.SootField;

import java.util.List;

/**
 * Created by yelite on 2016/10/24.
 */
public class GeneralInput {
    public final String id;
    public final Integer pos;
    private final List<String> fields;

    public GeneralInput(Integer pos) {
        this.pos = pos;
        this.fields = null;
        this.id = String.valueOf(pos);
    }

    public GeneralInput(Integer pos, List<String> fields) {
        this.pos = pos;
        this.fields = fields;
        this.id = String.valueOf(pos) + "." + String.join(".", fields);
    }

    public boolean isPure() {
        return (this.fields == null);
    }

    public GeneralVariable instantialize(Local l) {
        if (this.isPure()) {
            return new GeneralVariable(l);
        } else {
            return new GeneralVariable(l, fields);
        }
    }
}
