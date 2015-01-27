import java.util.Arrays;

/**
 * @Author: Mike Hoyt
 *
 * This is the State representation for the learning algorithm.  Hold what block there is, plus the current
 * layout which is represented as an int[].  The layout will be the amount of free spaces from the 3rd row down.
 */
public class State {
    private Block block;
    private int[] layout;

    public State( Block block, int[] layout ) {
        this.block = block;
        this.layout = layout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;

        State state = (State) o;

        if ( block.getType() != state.block.getType() ) return false;
        if (!Arrays.equals(layout, state.layout)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(layout) + 31 * block.getType();
    }
}
