package stupidMarineAI;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.WeaponType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Stefan Rudolph on 18.02.14.
 */
public class Marine {

    final private JNIBWAPI bwapi;
    private final HashSet<Unit> enemyUnits;
    final private Unit unit;
    private StupidMarineAI ai;
    private int id;

    private ArrayList<Marine> column1;
    private ArrayList<Marine> column2;
    private ArrayList<Marine> row1;
    private ArrayList<Marine> row2;

    private int fitness;
    private int previousDistance;

    //change between learnt weights through genetic algorithm or unlearnt
    boolean learnt = true;
    double weight1 = 0.6;
    double weight2 = 0;
    double weight3 = 0.4;
    double weight4 = 0;
    int num;

    public Marine(Unit unit, JNIBWAPI bwapi, HashSet<Unit> enemyUnits, int id) throws Exception {
        this.unit = unit;
        this.bwapi = bwapi;
        this.enemyUnits = enemyUnits;
        this.id = id;
        num = 0;
        previousDistance = 0;
        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
        fitness = 0;
        //reads weights and fitness from last run game
        if (learnt) {
            String[] parameterArray = new String[5];
            int i = 0;
            BufferedReader br = new BufferedReader(new FileReader("data/parameters"
                    + id + ".txt"));
            String line;
            while ((line = br.readLine()) != null) {
                parameterArray[i] = line;
                i++;
            }
            weight1 = Double.parseDouble(parameterArray[0]);
            weight2 = Double.parseDouble(parameterArray[1]);
            weight3 = Double.parseDouble(parameterArray[2]);
            weight4 = Double.parseDouble(parameterArray[3]);
            fitness = Integer.parseInt(parameterArray[4]);
            br.close();
        }
    }

    public void step(StupidMarineAI ai) {
        this.ai = ai;
        Unit target = getClosestEnemy();

        if (unit.getOrderID() != 10 && !unit.isAttackFrame() && !unit.isStartingAttack() && !unit.isAttacking() && target != null) {
            if (bwapi.getWeaponType(WeaponType.WeaponTypes.Gauss_Rifle.getID()).getMaxRange() > getDistance(target) - ai.COLUMN_HEIGHT / 2.0) {
                //before attacking (and dying) writes weights to a file only 1 time(==num)
                if(num==0) {
                    try {
                        PrintWriter writer = new PrintWriter("data/parameters" + id + ".txt", "UTF-8");

                        writer.println(getWeight1()); // 0
                        writer.println(getWeight2());        // 1
                        writer.println(getWeight3());    // 2
                        writer.println(getWeight4());        // 3
                        writer.println(getFitness()); // 4
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    num++;
                }
                bwapi.attack(unit.getID(), target.getID());
            } else {
                move(target);
            }
        }
    }

    private void move(Unit target) {
        //TODO: Implement the flocking behavior in this method.
        double x = rule1().getX() * weight1 + + rule2().getX()*weight2+ rule3(true, false).getX()*weight3 + rule3(false, true).getX()*weight4;
        double y = rule1().getY() * weight1 + rule2().getY() * weight2 + rule3(true, false).getY()*weight3 + rule3(false, true).getY()*weight4;
        x = Math.round(x) + getX();
        y = Math.round(y) + getY();
        bwapi.move(unit.getID(), (int) x, (int) y);
    }

    public Unit getClosestEnemy() {
        Unit result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (Unit enemy : enemyUnits) {
            double distance = getDistance(enemy);
            if (distance < minDistance) {
                minDistance = distance;
                result = enemy;
            }
        }

        return result;
    }

    public double getDistance(Unit enemy) {
        int myX = unit.getX();
        int myY = unit.getY();

        int enemyX = enemy.getX();
        int enemyY = enemy.getY();

        int diffX = myX - enemyX;
        int diffY = myY - enemyY;

        double result = Math.pow(diffX, 2) + Math.pow(diffY, 2);

        return Math.sqrt(result);
    }

    public int getID() {
        return unit.getID();
    }


    //computes cohesion
    public Position getCohesion(Position pos) {
        Marine neighbour1 = getNearestNeighbour(ai);
        Marine neighbour2 = getSecondNearestNeighbour(ai);
        int sumX = neighbour1.getX() + neighbour2.getX();
        int sumY = neighbour1.getY() + neighbour2.getY();

        int averageX = 1 / 2 * sumX;
        int averageY = 1 / 2 * sumY;

        return new Position(averageX - pos.getX(), averageY - pos.getY());
    }

    //computes euclideanNorm
    public double euclideanNorm(Position pos1) {
        double result = Math.sqrt(Math.pow(pos1.getX(),2)+Math.pow(pos1.getY(),2));
        return result;
    }

    //computes rule1 from paper
    public Position rule1() {
        Unit target = getClosestEnemy();
        int x = target.getX() - getX();
        int y = target.getY() - getY();

        return new Position(x, y);
    }

    //computes rule2 from paper
    public Position rule2() {
        Marine neighbour1 = getNearestNeighbour(ai);
        Marine neighbour2 = getSecondNearestNeighbour(ai);

        int x = -(neighbour1.getX() - getX()) + (neighbour2.getX() - getX());
        int y = -(neighbour1.getY() - getY()) + (neighbour2.getY() - getY());

        return new Position(x, y);
    }

    //computes rule3 from paper
    public Position rule3(boolean row, boolean column) {
        Position center = ai.span();
        column1 = ai.getColumn1List();
        column2 = ai.getColumn2List();
        row1 = ai.getRow1List();
        row2 = ai.getRow2List();
        if (column == true) {
            //computes formula from rule3 for column1
            int tmp1 = Integer.MIN_VALUE;
            for (int i = 0; i < column1.size(); i++) {
                Position cohesion = getCohesion(new Position(column1.get(i).getX(), column1.get(i).getY()));
                Position pos = new Position(getX()+cohesion.getX(),getY()+cohesion.getY());
                int num1 = (int) Math.round((column1.size() / euclideanNorm(pos)) * 100000000);
                if (tmp1 <= num1) {
                    tmp1 = num1;
                }
            }
            //computes formula from rule3 for column2
            int tmp2 = Integer.MIN_VALUE;
                for (int i = 0; i < column2.size(); i++) {
                    Position cohesion = getCohesion(new Position(column2.get(i).getX(), column2.get(i).getY()));
                    Position pos = new Position(getX()+cohesion.getX(),getY()+cohesion.getY());
                    int num2 = (int) Math.round((column2.size() / euclideanNorm(pos)) * 100000000);
                    if (tmp2 <= num2) {
                        tmp2 = num2;
                    }
                }

            //move unit to middle of column with the highest value of the formula from rule3
            if (tmp1 >= tmp2) {
                return new Position(center.getX() - ai.COLUMN_WIDTH / 2 - getX(), center.getY() - getY());
            } else {
                return new Position(center.getX() + ai.COLUMN_WIDTH / 2 - getX(), center.getY() - getY());
            }

        } else if (row == true) {
            //computes formula from rule3 for row1
            int tmp1 = Integer.MIN_VALUE;
                for (int i = 0; i < row1.size(); i++) {
                    Position cohesion = getCohesion(new Position(row1.get(i).getX(), row1.get(i).getY()));
                    Position pos = new Position(getX()+cohesion.getX(),getY()+cohesion.getY());
                    int num1 = (int) Math.round((row1.size() / euclideanNorm(pos)) * 100000000);
                    if (tmp1 <= num1) {
                        tmp1 = num1;
                    }
                }
            //computes formula from rule3 for row2
            int tmp2 = Integer.MIN_VALUE;
            for (int i = 0; i < row2.size(); i++) {
                    Position cohesion = getCohesion(new Position(row2.get(i).getX(), row2.get(i).getY()));
                    Position pos = new Position(getX()+cohesion.getX(),getY()+cohesion.getY());
                    int num2 = (int) Math.round((row2.size() / euclideanNorm(pos)) * 100000000);
                    if (tmp2 <= num2) {
                        tmp2 = num2;
                    }
                }
            //move unit to the middle of row with the highest value of the formula from rule3
            if (tmp1 >= tmp2) {
                return new Position(center.getX() - getX(), center.getY() - ai.ROW_HEIGHT / 2 - getY());
            } else {
                return new Position(center.getX() - getX(), center.getY() + ai.ROW_HEIGHT / 2 - getY());
            }
        } else {
            System.out.println("Error!");
            return null;
        }
    }

    //computes nearest neighbour
    public Marine getNearestNeighbour(StupidMarineAI ai) {
        Marine result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (Marine marine : ai.getMarines()) {
            double distance = getDistance(marine.getUnit());
            if (distance < minDistance && marine != this) {
                minDistance = distance;
                result = marine;
            }
        }
        return result;
    }


    //computes second nearest neighbour
    public Marine getSecondNearestNeighbour(StupidMarineAI ai) {
        Marine result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (Marine marine : ai.getMarines()) {
            double distance = getDistance(marine.getUnit());
            if (distance < minDistance && marine != this && marine != getNearestNeighbour(ai)) {
                minDistance = distance;
                result = marine;
            }
        }
        return result;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getX() {
        return unit.getX();
    }

    public int getY() {
        return unit.getY();
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int getFitness() {
        return fitness;
    }

    public void setPreviousDistance(int distance) {
        previousDistance = distance;
    }

    public int getPreviousDistance() {
        return previousDistance;
    }

    public double getWeight1() {
        return weight1;
    }

    public double getWeight2() {
        return weight2;
    }

    public double getWeight3() {
        return weight3;
    }

    public double getWeight4() {
        return weight4;
    }

    public void setWeight1(double weight) {
        this.weight1 = weight;
    }

    public void setWeight2(double weight) {
        this.weight2 = weight;
    }

    public void setWeight3(double weight) {
        this.weight3 = weight;
    }

    public void setWeight4(double weight) {
        this.weight4 = weight;
    }

}