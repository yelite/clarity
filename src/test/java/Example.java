import java.util.*;

/**
 * Created by yelite on 2016/10/18.
 */


class Example {
    public static void main(String[] argv) {
        testCase1();
        testCase2();
        testCase3();
    }

    public static void testCase1() {
        List<Integer> int_list = new ArrayList<>();

        for (int i : int_list) {
            Integer max = computeMaxIdx(int_list);
        }
    }

    public static void testCase2() {
        // Cannot reason about recursive data structure
        List<Integer> int_list = new LinkedList<>();

        for (int i : int_list) {
            Integer max = computeMaxIdx(int_list);
        }
    }

    public static void testCase3() {
        ArrayList<Integer> int_list = new ArrayList<>();

        for (int i : int_list) {
            Integer max = computeMaxIdx(int_list);
            // Write int_list so that the traversal is not redundant
            int_list.add(1);
        }
    }

    private static Integer computeMaxIdx(List<Integer> c) {
        Integer max = 0;

        for (int i = 0; i < c.size(); i++) {
            if (c.get(i) > max) {
                max = i;
                break;
            }
        }

        return max;
    }
}
