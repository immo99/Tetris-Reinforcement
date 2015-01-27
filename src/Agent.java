import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This is the agent that plays the game.
 * It can act greedily, randomly, or use the TD-Learning for Q-values reinforcement learning.
 *
 * @author M. Allen
 * @author Michael Hoyt
 */

public class Agent
{
    // This allows the agent to record entire state of game board
    // (NOTE: probably a good idea to leave these alone; even
    // if your agent ends up throwing out some of the information,
    // you can handle that later by writing methods to do so).
    private Block currentBlock;
    private Block nextBlock;
    private int[][] board;
    //gets amount of freespaces until the first block in a column.
    private int[] emptBlocks;

    //State Action HashMap
    HashMap<State, HashMap<Integer, Double>> stateAction;

    //Action decoder
    HashMap<Integer, int[]> actions;
    
    // Agent type: can be used to test different
    // sorts of policies (random, fixed, learning).
    private int type;
    //current and previous states and actions
    private State currState;
    private State prevState;
    private int currAction;
    private int prevAction;

    //current/previous reward, epsilon, and boolean whether or not to learn.
    private double reward;
    private double prevReward;
    private double epsilon;
    private boolean learn;
    
    // Constants for different agent types.
    public static final int RANDOM_AGENT = 0;
    public static final int FIXED_AGENT = 1;
    public static final int LEARNING_AGENT = 2;
    
    /**
     * Basic constructor (creates space for storing board-state, sets type).
     *
     * @param rows Number of rows in board-space.
     * @param cols Number of columns in board-space.
     * @param t Type of agent.
     */
    public Agent( int rows, int cols, int t )
    {
        currentBlock = new Block( 1, 0, 0 );
        nextBlock = new Block( 1, 0, 0 );
        board = new int[rows][cols];
        emptBlocks = new int[cols];
        stateAction = new HashMap<State, HashMap<Integer, Double>>();
        actions = fillMap();
        type = t;
        reward = 0;
        prevReward = 0;
        learn = false;
        currAction = -1;
        prevAction = -1;
        epsilon = 0.05;
    }
    
    /**
     * Sets type of agent.
     *
     * @param t Agent type (will be either random/fixed/learning).
     */
    public void setType( int t )
    {
        type = t;
    }

    /**
     * Sets epsilon
     *
     * @param d epsilon to be used to pick action
     */
    public void setEpsilon( double d ) {
        epsilon = d;
    }

    /**
     * Turns the learning algorithm on
     */
    public void turnOnLearn() {
        learn = true;
    }
    
    /**
     * Agent gets a state representation of the amount of empty spaces in a column until the first block if learning,
     * otherwise gets default.  Creates a new state and if it is not currently in the state HashMap, it adds it.
     *
     * @param b The game-board at its current state.
     */
    public void getFullState( Board b )
    {
        currentBlock = b.copyCurrentBlock();


        if( learn ) {
            emptBlocks = b.getEmptyBlocks();
            currState = new State( currentBlock, emptBlocks );

            if( !stateAction.containsKey(currState) )
                addState( currentBlock, emptBlocks );

        }
        else {
            nextBlock = b.copyNextBlock();
            board = b.copyBoard();
        }
    }

    /**
     * Fill up the actions HashMap
     * @return a HashMap from 0-23 with the different combinations of actions
     */
    public HashMap<Integer, int[]> fillMap() {
        HashMap<Integer, int[]> act = new HashMap<Integer, int[]>();

        int curr = 0;

        for( int i = 0; i < 6; i++ ) {
            for( int j = 0; j < 4; j++, curr++ ) {
                int[] tmp = {i,j};

                act.put( curr, tmp );
            }
        }
        return act;
    }

    
    /**
     * Choose an action in game.
     *
     * @return Pair of integers, where first is number of spaces to move block
     *         to right, and second is number of times to rotate it before drop.
     */
    public int[] chooseAction()
    {
        // acts based on type
        if ( type == RANDOM_AGENT )
            return actRandomly();
        else if ( type == FIXED_AGENT )
            return actFixedly();
        else
            return actLearnedly();
    }
    
    /**
     * For an agent that chooses a random action each time (useful to compare
     * against a learning agent as a baseline).
     *
     * @return Two integer values: number of squares to move block right, number
     *         of times to rotate it.
     */
    public int[] actRandomly()
    {
        int moveR = 0;
        int rotate = 0;
        
        // 1. moves the block right some random amount (0-5 steps)
        // 2. randomly rotates (0-3 turns)
        // 3. drops
        if ( currentBlock.getWidth() == 2 )
            moveR = (int) ( Math.random() * 5 );
        else
            moveR = (int) ( Math.random() * 6 );
        
        rotate = (int) ( Math.random() * 4 );
        int[] act = { moveR, rotate };
        return act;
    }
    
    /**
     * For an agent that chooses a fixed action each time (useful to compare
     * against a learning agent as a baseline). It tries to place the tile in
     * the first location it can, as deep as possible.
     *
     * @return Two integer values: number of squares to move block right, number
     *         of times to rotate it.
     */
    public int[] actFixedly()
    {
        int moveR = 0;
        int rotate = 0;
        int left = 0;
        int deep = 1;
        boolean twoWide = false;
        learn = false;
        
        // for the single pink blocks, find the deepest hole
        if ( currentBlock.getType() == 5 )
        {
            for ( int i = 0; i < board[2].length; i++ )
                if ( board[2][i] == 0 )
                {
                    if ( deep == 1 )
                        left = i;
                    
                    // see how deep it is
                    for ( int j = 1; j < 3; j++ )
                    {
                        if ( board[2 + j][i] == 0 )
                        {
                            if ( j >= deep )
                            {
                                left = i;
                                deep = j + 1;
                            }
                        }
                        else
                            break;
                    }
                    
                }
            moveR = left;
        }
        else
        {
            // see if it can find a hole that is
            // at least two squares wide
            for ( int i = 0; i < board[2].length - 1; i++ )
            {
                // find a wide enough hole on top row
                if ( ( board[2][i] == 0 ) && ( board[2][i + 1] == 0 ) )
                {
                    twoWide = true;
                    if ( deep == 1 )
                    {
                        left = i;
                    }
                    // see how deep it is
                    for ( int j = 1; j < 3; j++ )
                    {
                        if ( ( board[2 + j][i] == 0 ) &&
                            ( board[2 + j][i + 1] == 0 ) )
                        {
                            if ( j >= deep )
                            {
                                left = i;
                                deep = j + 1;
                            }
                        }
                        else
                            break;
                    }
                }
            }
            // if it can find a wide enough hole, drop it in
            if ( twoWide )
            {
                moveR = left;
                // rotate the skinny blue blocks
                if ( currentBlock.getType() == 4 )
                    rotate = 1;
            }
            else
            {  // randomly select a move if no wide hole
                moveR = (int) ( Math.random() * 5 );
                rotate = (int) ( Math.random() * 4 );
            }
        }
        
        // output the relevant action
        int[] act = { moveR, rotate };
        return act;
    }
    
    /**
     * For an agent using reinforcement learning.  Picks a random action epsilon% of the time.  Otherwise,
     * it loops through the actions that the state can perform and returns the best one.
     *
     * @return Two integer values: number of squares to move block right, number
     *         of times to rotate it.
     */
    public int[] actLearnedly()
    {
        //do a random action epsilon% of the time
        if( Math.random() <= epsilon ) {
            Random rand = new Random();
            int randInt = rand.nextInt(24);
            currAction = randInt;
            return actions.get(randInt);
        }
        else {
            int max = 0;
            double maxVal = -1000000;
            for( Map.Entry<Integer, Double> entry : stateAction.get(currState).entrySet() ) {
                if( entry.getValue() > maxVal ) {
                    maxVal = entry.getValue();
                    max = entry.getKey();
                }
            }
            currAction = max;

            return actions.get(max);
        }
    }
    
    /**
     * Process rewards from any actions.
     * 
     * @param r The reward gained for the last action.
     */
    public void getReward( double r )
    {
        reward = r;
    }

    /**
     * Create states and actions for a state, then add it to stateAction.
     * Note, 0-23 in action refer to the different int[] arrays. Refer to actions for decoding.
     * @param b The block to add
     * @param arr The array to add
     */
    private void addState( Block b, int[] arr ) {
        HashMap<Integer, Double> actions = new HashMap<Integer, Double>();
        for( int i = 0; i < 24; i++ )
            actions.put( i, 0.0 );

        stateAction.put( new State( b, arr), actions );
    }

    /**
     * Performs the TD with Q Reinforcement Learning algorithm.
     *
     * @param alpha How much to care about the information.  Starts high and gets low the more you learn.
     *              Equal to (# total steps - steps so far) / #total steps)
     */
    public void learn( double alpha ) {
        if( prevState == null ) {
            //do nothing, only happens first time
        }
        else{
            double prevVal = stateAction.get(prevState).get(prevAction);
            double currVal = stateAction.get(currState).get(currAction);

            double newVal = ( prevVal + (alpha * ( prevReward + (0.9 * currVal) - prevVal )) );
            stateAction.get(prevState).put( prevAction, newVal );

        }

        prevReward = reward;
        prevAction = currAction;
        prevState = currState;

    }

}