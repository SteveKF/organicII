import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import jnibwapi.Position;
import jnibwapi.Unit;


public class Classifier {

    private boolean condition;
    private int action;
    private double precision;
    private double error;
    private double fitness;
    private int[] geneticArray = new int[NUM_CONDITIONS];

    private int hitpoints;
    private int enemyShieldpoints;
    private int enemyHitpoints;
    private int previousHitpoints = 0;
    private int previousEnemyShieldpoints = 0;
    private int previousEnemyHitpoints = 0;
    private double previousDistance = 0;

    private double reward;
    //private static final int ENEMYRANGE = UnitType.UnitTypes.Protoss_Zealot.getGroundWeapon().getMaxRange();
    //private static final int MAXRANGE = UnitType.UnitTypes.Terran_Vulture.getGroundWeapon().getMaxRange();
    private Unit unit;
    private Unit target;

    public static int NUM_CLASSIFIERS = 30; //number of classifiers
    public static final int NUM_CONDITIONS = 30; //number of conditions which will be checked every frame
    public static final int NUM_ACTIONS = 2; //number of actions

    private final double DISCOUNT_FACTOR = 0.71; //discount factor to discount reward of last frame
    private final double BETA = 0.0009; //learn rate

    public static boolean unlearnt = false; // set false if you want to use the preexisting parameters

    private int callCounter = 0; //counter to check how often classifier was in actionset


    //empty constructor
    public Classifier(){

    }


    public Classifier(int index) throws IOException {

        condition = false;
        action = -1;

        //executes learnt(reads parameter from files) classifier
        if (unlearnt == false) {
            String[] parameterArray = new String[6];
            int i = 0;
            BufferedReader br = new BufferedReader(new FileReader("data/parameters"
                    + index + ".txt"));
            String line;
            while ((line = br.readLine()) != null) {
                parameterArray[i] = line;
                i++;
            }
            // Initialize classifier variables with established start parameters
            precision = Double.parseDouble(parameterArray[0]);
            error = Double.parseDouble(parameterArray[1]);
            fitness = Double.parseDouble(parameterArray[2]);
            reward = Double.parseDouble(parameterArray[3]);
            callCounter = Integer.parseInt(parameterArray[4]);
            StringBuffer bf = new StringBuffer(parameterArray[5]);
            for (int j = 0; j < geneticArray.length; j++) {
                geneticArray[j] = ((int) bf.charAt(j)) - 48;
            }


            br.close();

            //executes unlearnt classifier and initializes parameters with some values we thought of
        } else {
            fitness = 20;
            precision = 0.00001;
            error = 0.000001;
            reward = 1;
            for (int j = 0; j < geneticArray.length; j++) {
                geneticArray[j] = 0;
            }
            geneticArray[index] = 1;
        }
    }

    public void setCondition(Unit unit, Unit target) {

        // changes condition from last frame to false so that it can check condition for next frame
        condition = false;

        // saves environment (our vulture unit and the nearest target)
        this.unit = unit;
        this.target = target;

        // sets condition true and set an action if environment matches

        if (geneticArray[0] == 1) {
            if (unit.isMoving()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[1] == 1) {
            if (unit.isMoving()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[2] == 1) {
            if (previousEnemyShieldpoints >= target.getShields()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[3] == 1) {
            if (previousEnemyShieldpoints >= target.getShields()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[4] == 1) {
            if (previousDistance < unit.getDistance(target)) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[5] == 1) {
            if (previousDistance < unit.getDistance(target)) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[6] == 1) {
            if (previousDistance >= unit.getDistance(target)) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[7] == 1) {
            if (previousDistance >= unit.getDistance(target)) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[8] == 1) {
            if (unit.isUnderAttack()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[9] == 1) {
            if (unit.isUnderAttack()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[10] == 1) {
            if (previousHitpoints >= unit.getHitPoints()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[11] == 1) {
            if (previousHitpoints >= unit.getHitPoints()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[12] == 1) {
            if (previousEnemyHitpoints >= target.getHitPoints()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[13] == 1) {
            if (previousEnemyHitpoints >= target.getHitPoints()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[14] == 1) {
            if (unit.isAttackFrame()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[15] == 1) {
            if (unit.isAttackFrame()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[16] == 1) {
            if (previousHitpoints < unit.getHitPoints()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[17] == 1) {
            if (previousHitpoints < unit.getHitPoints()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[18] == 1) {
            if (previousEnemyHitpoints < target.getHitPoints()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[19] == 1) {
            if (previousEnemyHitpoints < target.getHitPoints()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[20] == 1) {
            if (target.isUnderAttack()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[21] == 1) {
            if (target.isUnderAttack()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[22] == 1) {
            if (target.isAttackFrame()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[23] == 1) {
            if (target.isAttackFrame()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[24] == 1) {
            if (target.isMoving()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[25] == 1) {
            if (target.isMoving()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[26] == 1) {
            if (!target.isMoving()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[27] == 1) {
            if (!target.isMoving()) {
                condition = true;
                action = 1;
            }
        }
        if (geneticArray[28] == 1) {
            if (!target.isUnderAttack()) {
                condition = true;
                action = 0;
            }
        }
        if (geneticArray[29] == 1) {
            if (!target.isUnderAttack()) {
                condition = true;
                action = 1;
            }
        }

    }

    public static void selectAction(int action, Unit unit, Unit target) {

        //attack enemy
        if (action == 0) {
            unit.attack(target, false);
        }

        //move away from enemy
        if (action == 1) {
            unit.move(new Position(target.getPosition().getPX() - 170, target.getPosition().getPY() - 170), false);
        }

    }

    //updates the parameter after every xcs loop
    public void update(double[] predictionarray) throws FileNotFoundException, UnsupportedEncodingException {


        //limit for precision
        if (precision >= 1000000000) {
            precision = 1000000000;
        }
        if (precision <= -1000000000) {
            precision = -1000000000;
        }
        //update precision
        precision = precision + BETA * (reward - precision);

        //limit for error
        if (error >= 1000000000) {
            error = 1000000000;
        }
        if (error <= -1000000000) {
            error = -1000000000;
        }
        //update error
        error = error + BETA * (Math.abs(reward - precision) - error);

        //limit for fitness
        if (fitness >= 1000000000) {
            fitness = 1000000000;
        }
        if (fitness <= -1000000000) {
            fitness = -1000000000;
        }
        //update fitness
        fitness = fitness + BETA * (1.0 / error - fitness);


        //change reward depending on environment
        setReward();

        //limit for reward
        if (reward >= 1000000000) {
            reward = 1000000000;
        }
        if (reward <= -1000000000) {
            reward = -1000000000;
        }

        //get max from predictionarray
        double tmp = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < predictionarray.length; i++) {
            if (tmp != Math.max(tmp, predictionarray[i])) {
                tmp = Math.max(tmp, predictionarray[i]);
            }
        }

        //update reward
        reward = reward + DISCOUNT_FACTOR * tmp;


    }


    //updates the reward depending on environment after action
    private void setReward() {

        hitpoints = unit.getHitPoints();
        enemyShieldpoints = target.getShields();
        enemyHitpoints = target.getHitPoints();


        if (hitpoints < previousHitpoints)
            reward += -100;

        if (hitpoints == previousHitpoints)
            reward += +100;


        if (enemyShieldpoints < previousEnemyShieldpoints)
            reward += +100;

        if (enemyShieldpoints >= previousEnemyShieldpoints)
            reward += -100;


        if (enemyHitpoints < previousEnemyHitpoints)
            reward += +100;

        if (enemyHitpoints == previousEnemyHitpoints)
            reward += -100;

        if (unit.isUnderAttack())
            reward += -100;
        if (!target.isUnderAttack())
            reward += -100;

        previousDistance = unit.getDistance(target);
        previousHitpoints = hitpoints;
        previousEnemyHitpoints = enemyHitpoints;
        previousEnemyShieldpoints = enemyShieldpoints;

    }

    //copys values from classifier to a new generated one
    public Classifier copyOf() {
        Classifier cl = new Classifier();

        int[] array = Arrays.copyOf(geneticArray, geneticArray.length);
        for (int i = 0; i < array.length; i++) {
            cl.setGeneticArray(i, array[i]);
        }

        cl.condition = condition;
        cl.action = action;
        cl.precision = precision;
        cl.error = error;
        cl.fitness = fitness;
        cl.hitpoints = hitpoints;
        cl.enemyShieldpoints = enemyShieldpoints;
        cl.enemyHitpoints = enemyHitpoints;
        cl.previousHitpoints = previousHitpoints;
        cl.previousEnemyShieldpoints = previousEnemyShieldpoints;
        cl.previousEnemyHitpoints = previousEnemyHitpoints;
        cl.previousDistance = previousDistance;
        cl.reward = reward;
        cl.unit = unit;
        cl.target = target;

        return cl;

    }


    public boolean getCondition() {

        return condition;
    }

    public int getAction() {

        return action;
    }

    public double getFitness() {

        return fitness;
    }

    public double getPrecision() {

        return precision;
    }

    public double getReward() {

        return reward;
    }

    public double getError() {

        return error;
    }

    public int[] getGeneticArray() {

        return geneticArray;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public void setError(double error) {
        this.error = error;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    //set the value of the genetic array at index
    public void setGeneticArray(int index, int value) {
        geneticArray[index] = value;
    }

    public static void setNumClassifier(int conditions) {
        NUM_CLASSIFIERS = conditions;
    }

    public int getCallCounter() {
        return callCounter;
    }

    //increases the counter if classifier was in actionset
    public void increaseCallCounter() {
        callCounter++;
    }

}