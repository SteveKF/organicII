import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;


public class Classifier {

    private boolean condition;
    private int action;
    private double precision;
    private double error;
    private double fitness;

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

    public static final int NUM_CONDITIONS = 30; //number of classifiers
    public static final int NUM_ACTIONS = 2; //number of actions

    private final double DISCOUNT_FACTOR = 0.71;
    private final double BETA = 0.0009; //learn rate

    private static boolean unlearnt = false; // set false if you want to use the preexisting parameters


    public Classifier(int index) throws IOException {

        condition = false;
        action = -1;

        //executes learnt classifier
        if (unlearnt == false) {
            String[] parameterArray = new String[5];
            int i = 0;
            BufferedReader br = new BufferedReader(new FileReader("data/parameters"
                    + index + ".txt"));
            String line;
            while ((line = br.readLine()) != null) {
                parameterArray[i] = line;
                i++;
            }
            // Initialize with established start parameters
            precision = Double.parseDouble(parameterArray[0]);
            error = Double.parseDouble(parameterArray[1]);
            fitness = Double.parseDouble(parameterArray[2]);
            reward = Double.parseDouble(parameterArray[3]);

            br.close();
            //executes unlearnt classifier
        } else {
            fitness = 20;
            precision = 0.00001;
            error = 0.000001;
            reward = 1;

            PrintWriter writer = new PrintWriter("parameters" + index + ".txt", "UTF-8");

            writer.println(precision);
            writer.println(error);
            writer.println(fitness);
            writer.println(reward);

            writer.close();
        }
    }

    public void setCondition(int index, Unit unit, Unit target) {

        //changes condition from last frame to false so that it can check condition for next frame
        condition = false;

        //saves environment
        this.unit = unit;
        this.target = target;


        if (index > NUM_CONDITIONS) {
            System.err.println("Error");
        }
        //sets condition true if environment matches

        switch (index) {
            case 0:
                if(unit.isMoving()){
                    condition = true;
                    action = 0;
                }
                break;
            case 1:
                if(unit.isMoving()){
                    condition = true;
                    action = 1;
                }
                break;
            case 2:
                if(previousEnemyShieldpoints>=target.getShields()){
                    condition = true;
                    action = 0;
                }
                break;
            case 3:
                if(previousEnemyShieldpoints>=target.getShields()){
                    condition = true;
                    action = 1;
                }
                break;
            case 4:
                if(previousDistance<unit.getDistance(target)){
                    condition = true;
                    action = 0;
                }
                break;
            case 5:
                if(previousDistance<unit.getDistance(target)){
                    condition = true;
                    action = 1;
                }
                break;
            case 6:
                if(previousDistance>=unit.getDistance(target)){
                    condition = true;
                    action = 0;
                }
                break;
            case 7:
                if(previousDistance>=unit.getDistance(target)){
                    condition = true;
                    action = 1;
                }
                break;
            case 8:
                if(unit.isUnderAttack()){
                    condition = true;
                    action = 0;
                }
                break;
            case 9:
                if(unit.isUnderAttack()){
                    condition = true;
                    action = 1;
                }
                break;
            case 10:
                if(previousHitpoints>=unit.getHitPoints()){
                    condition = true;
                    action = 0;
                }
                break;
            case 11:
                if(previousHitpoints>=unit.getHitPoints()){
                    condition = true;
                    action = 1;
                }
                break;
            case 12:
                if(previousEnemyHitpoints>=target.getHitPoints()){
                    condition = true;
                    action = 0;
                }
                break;
            case 13:
                if(previousEnemyHitpoints>=target.getHitPoints()){
                    condition = true;
                    action = 1;
                }
                break;
            case 14:
                if(unit.isAttackFrame()){
                    condition = true;
                    action = 0;
                }
                break;
            case 15:
                if(unit.isAttackFrame()){
                    condition = true;
                    action = 1;
                }
                break;
            case 16:
                if(previousHitpoints<unit.getHitPoints()){
                    condition = true;
                    action = 0;
                }
                break;
            case 17:
                if(previousHitpoints<unit.getHitPoints()){
                    condition = true;
                    action = 1;
                }
                break;
            case 18:
                if(previousEnemyHitpoints<target.getHitPoints()){
                    condition = true;
                    action = 0;
                }
                break;
            case 19:
                if(previousEnemyHitpoints<target.getHitPoints()){
                    condition = true;
                    action = 1;
                }
                break;
            case 20:
                if(target.isUnderAttack()){
                    condition = true;
                    action = 0;
                }
                break;
            case 21:
                if(target.isUnderAttack()){
                    condition = true;
                    action = 1;
                }
                break;
            case 22:
                if(target.isAttackFrame()){
                    condition = true;
                    action = 0;
                }
                break;
            case 23:
                if(target.isAttackFrame()){
                    condition = true;
                    action = 1;
                }
                break;
            case 24:
                if(target.isMoving()){
                    condition = true;
                    action = 0;
                }
                break;
            case 25:
                if(target.isMoving()){
                    condition = true;
                    action = 1;
                }
                break;
            case 26:
                if(!target.isMoving()){
                    condition = true;
                    action = 0;
                }
                break;
            case 27:
                if(!target.isMoving()){
                    condition = true;
                    action = 1;
                }
                break;
            case 28:
                if(!target.isUnderAttack()){
                    condition = true;
                    action = 0;
                }
                break;
            case 29:
                if(!target.isUnderAttack()){
                    condition = true;
                    action = 1;
                }
                break;
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

        if(unit.isUnderAttack())
            reward += -100;
        if(!target.isUnderAttack())
            reward += -100;

        previousDistance = unit.getDistance(target);
        previousHitpoints = hitpoints;
        previousEnemyHitpoints = enemyHitpoints;
        previousEnemyShieldpoints = enemyShieldpoints;

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

}