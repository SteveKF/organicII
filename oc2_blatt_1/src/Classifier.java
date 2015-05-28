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

    private double reward;
    private static final int ENEMYRANGE = UnitType.UnitTypes.Protoss_Zealot.getGroundWeapon().getMaxRange();
    private static final int MAXRANGE = UnitType.UnitTypes.Terran_Vulture.getGroundWeapon().getMaxRange();
    private Unit unit;
    private Unit target;

    public static final int NUM_CONDITIONS = 4;
    public static final int NUM_ACTIONS = 2;

    private final double DISCOUNT_FACTOR = 0.71;
    private final double BETA = 0.15;
    
    private static boolean unlearnt = false; // set false if you want to use the preexisting parameters



    
    public Classifier(int index) throws IOException {
		if (unlearnt == false) {
			String[] parameterArray = new String[5];
			int i = 0;
			BufferedReader br = new BufferedReader(new FileReader("parameters"
					+ index + ".txt"));
			String line;
			while ((line = br.readLine()) != null) {
				parameterArray[i] = line;
				i++;
			}
			// Initialize with established start parameters
			fitness = Double.parseDouble(parameterArray[2]);
			precision = Double.parseDouble(parameterArray[0]);
			error = Double.parseDouble(parameterArray[1]);
			reward = Double.parseDouble(parameterArray[3]);

			br.close();
		} else {
			fitness = 20;
	        precision = 0.00001;
	        error = 0.000001;
	        condition = false;
	        action = -1;
	        reward = 1;
		}
    }

    public void setCondition(int index, Unit unit, Unit target) {
        //saves environment
        this.unit = unit;
        this.target = target;

        if (index > NUM_CONDITIONS) {
            System.err.println("Error");
        }
        //sets condition true if environment matches
        switch (index) {
            case 0:
                if (unit.getDistance(target) > MAXRANGE) {
                    condition = true;
                    action = 1;
                }
                break;
            case 1:
                if (unit.getDistance(target) <= ENEMYRANGE) {
                    condition = true;
                    action = 0;
                }
                break;
            case 2:
                if(unit.isAttackFrame()){
                    condition = true;
                    action = 1;
                }
                break;
            case 3:
                if(unit.isUnderAttack()){
                    condition = true;
                    action = 0;
                }
                break;
        }
    }

    public static void selectAction(int action, Unit unit, Unit target) {

        //move away from enemy
        if (action == 0) {
            unit.move(new Position(target.getPosition().getPX() - MAXRANGE, target.getPosition().getPY() - MAXRANGE), false);
        }

        //attack enemy
        if (action == 1) {
            unit.attack(target, false);
        }

    }

    public void update(double[] predictionarray) throws FileNotFoundException, UnsupportedEncodingException {
    	
    	
    	
        //update precision
        precision = precision + BETA * (reward-precision);

        //update error
        error = error + BETA * (Math.abs(reward-precision)-error);

        //update fitness
        fitness = fitness + BETA *(1.0/error - fitness);

        //update reward
        setReward();

        double tmp = Double.NEGATIVE_INFINITY;
        for(int i=0; i<predictionarray.length;i++){
            if(tmp != Math.max(tmp,predictionarray[i]))
                tmp = Math.max(tmp,predictionarray[i]);
        }
        reward = reward + DISCOUNT_FACTOR * tmp;

        //prepare condition parameter for next iteration
        condition = false;
        
        
    }


    /**
     * TODO: setReward unnötig??
     */
    private void setReward(){

        if(unit.isAttackFrame())
            reward += +2.5;

        if(unit.isUnderAttack())
            reward += -10;

        if(unit.getDistance(target)<ENEMYRANGE)
            reward += -5;

        if(unit.attack(target,false)){
            reward += +5;
        }
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
