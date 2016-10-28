import java.util.*;

/**
 * Created by yelite on 2016/10/18.
 */


class Container {
    private HashMap[] item = new HashMap[3];

    public HashMap getItem() {
        return item[0];
    }

    public void setItem(HashMap v) {
        this.item[0] = v;
    }
}


class Example {
    public int e = 1;

    public static void main(String[] argv) {
        List<Integer> int_list = new LinkedList<Integer>();
        for (int i = 0; i < 10; i++) {
            int_list.add(i);
        }

        Example e = new Example();
        e.computeMax(int_list);
        for (int i : int_list) {
            Integer max = e.computeMax(int_list);
            int_list.add(e.e);
        }

        Container c1 = new Container();
        Container c2 = new Container();
        c2.setItem(c1.getItem());
    }

    private Integer computeMax(Collection<Integer> c) {
        if (c.isEmpty()) {
            return null;
        }

        Iterator<Integer> iter = c.iterator();
        Integer max = Integer.MIN_VALUE;

        while (true) {
            Integer i = iter.next();
            if (i > max) {
                max = i;
                break;
            }
        }
        while (max < 3) {
            max = 3;
        }

        return max;
    }
}
