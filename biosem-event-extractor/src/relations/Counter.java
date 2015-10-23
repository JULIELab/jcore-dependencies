package relations;

/**
 *
 * @author Chinh
 * @Date: Feb 28, 2011
 */
public class Counter implements Comparable {

    public int count = 0;

    public Counter(int c) {
        count = c;

    }

    public void add(int value) {
        count = count + value;
    }

    public int getValue() {
        return count;
    }

    public void inc() {
        count++;
    }

    public Counter() {
        count = 1;
    }

    @Override
    public int compareTo(Object o) {
        return ((Counter) o).getValue() - count;
    }
}
