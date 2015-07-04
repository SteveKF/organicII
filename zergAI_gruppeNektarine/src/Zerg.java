
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


public class Zerg {

    final private JNIBWAPI bwapi;
    private final HashSet<Unit> enemyUnits;
    final private Unit unit;
    private ZergAI ai;
    private int id;

    private ArrayList<Zerg> column1;
    private ArrayList<Zerg> column2;
    private ArrayList<Zerg> row1;
    private ArrayList<Zerg> row2;

    private int fitness;
    private int previousDistance;

    //change between learnt weights through genetic algorithm or unlearnt
    boolean learnt = false;
    double weight1 = 0.5;
    double weight2 = 0;
    double weight3 = 0.5;
    double weight4 = 0;
    int num;
    private final int NUM_NEIGHBOURS = 4;
    private Zerg[] neighbours;

    public Zerg(Unit unit, JNIBWAPI bwapi, HashSet<Unit> enemyUnits, int id) throws Exception {
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
        neighbours = new Zerg[NUM_NEIGHBOURS];
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

    public void step(ZergAI ai) {
        this.ai = ai;

        Unit target = getClosestEnemy();

        if (unit.getOrderID() != 10 && !unit.isAttackFrame() && !unit.isStartingAttack() && !unit.isAttacking() && target != null) {
            if (bwapi.getWeaponType(WeaponType.WeaponTypes.Gauss_Rifle.getID()).getMaxRange() > getDistance(target) - ai.COLUMN_HEIGHT / 2.0) {
                //before attacking (and dying) writes weights to a file only 1 time(==num)
                /*if(num==0) {
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
                }*/
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
        //bwapi.move(unit.getID(),target.getX(),target.getY());
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
        for(int i=1;i<=neighbours.length;i++){
            neighbours[i-1] = getNNearestNeighbour(ai,i);
        }
        int sumX = 0;
        int sumY = 0;

        for(int i=0;i<neighbours.length;i++){
            sumX += neighbours[i].getX();
            sumY += neighbours[i].getY();
        }

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

        int x = 0,y = 0;

        for(int i=1;i<=neighbours.length;i++){
            neighbours[i-1] = getNNearestNeighbour(ai,i);
        }

        for(int i=0;i<neighbours.length;i++){
            x += (neighbours[i].getX() - getX());
            y += (neighbours[i].getY() - getY());
        }

        x *= -1;
        y *= -1;



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
    public Zerg getNNearestNeighbour(ZergAI ai, int n) {
        Zerg result = null;
        double[] check_distance = new double[n];
        for(int i=0; i<n;i++) {
            double minDistance = Double.POSITIVE_INFINITY;
            for (Zerg zerg : ai.getMarines()) {
                int counter = 0;
                double distance = getDistance(zerg.getUnit());
                if(i==0){
                    if (distance < minDistance && zerg != this) {
                        minDistance = distance;
                        result = zerg;
                    }
                }else {
                    for(int j=1;j<n;j++){
                        if(check_distance[j-1]!=distance){
                            counter++;
                        }
                    }
                    if (distance < minDistance && (counter+1)==n && zerg != this) {
                        minDistance = distance;
                        result = zerg;
                    }
                }
            }
            check_distance[i]= minDistance;
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