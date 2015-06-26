package stupidMarineAI;

import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.WeaponType;

import java.util.ArrayList;
import java.util.HashSet;

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

    private int fitness = 0;
    private int previousDistance = 0;

    double weight1 = 0.8;
    double weight2 = 0.2;
    double weight3 = 0;
    double weight4 = 0;

    public Marine(Unit unit, JNIBWAPI bwapi, HashSet<Unit> enemyUnits, int id) {
        this.unit = unit;
        this.bwapi = bwapi;
        this.enemyUnits = enemyUnits;
        this.id = id;

        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();

    }

    public void step(StupidMarineAI ai) {
        this.ai = ai;
        Unit target = getClosestEnemy();

        if (unit.getOrderID() != 10 && !unit.isAttackFrame() && !unit.isStartingAttack() && !unit.isAttacking() && target != null) {
            if (bwapi.getWeaponType(WeaponType.WeaponTypes.Gauss_Rifle.getID()).getMaxRange() > getDistance(target) - ai.COLUMN_HEIGHT/2.0) {
                bwapi.attack(unit.getID(), target.getID());
            } else {
                move(target);
            }
        }
    }

    private void move(Unit target) {
        //TODO: Implement the flocking behavior in this method.
        double x = rule1().getX()*weight1 + rule2().getX()+weight2 + rule3(false,true,ai).getX()*weight3
                + rule3(true,false,ai).getX()*weight4;
        double y = rule1().getY()*weight1 + rule2().getY()*weight2 +rule3(false,true,ai).getY()*weight3
                + rule3(true,false,ai).getX()*weight4;
        x = Math.round(x)+getX();
        y = Math.round(y)+getY();
        bwapi.move(unit.getID(),(int)x,(int)y);
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


    public Position getCohesion(Marine neighbour1, Marine neighbour2) {
        int sumX = neighbour1.getX() + neighbour2.getX();
        int sumY = neighbour1.getY() + neighbour2.getY();

        int averageX = 1 / 2 * sumX;
        int averageY = 1 / 2 * sumY;

        return new Position(averageX - unit.getX(), averageY - unit.getY());
    }

    public Position rule1() {
        Unit target = getClosestEnemy();


        int x = target.getX() - getX();
        int y = target.getY() - getY();

        return new Position(x, y);
    }

    public Position rule2(){
        Marine neighbour1 = getNearestNeighbour(ai);
        Marine neighbour2 = getSecondNearestNeighbour(ai);

        int x = -(neighbour1.getX() - getX()) + (neighbour2.getX() -getX());
        int y = -(neighbour1.getY() - getY()) + (neighbour2.getY() -getY());

        return new Position(x,y);
    }

    public Position rule3(boolean row, boolean column, StupidMarineAI ai) {
        this.ai = ai;
        column1 = ai.getColumn1List();
        column2 = ai.getColumn2List();
        row1 = ai.getRow1List();
        row2 = ai.getRow2List();
        Position center = ai.span();
        double num1 = 0.0;
        double num2 = 0.0;
        if (column == true) {
            int x = unit.getX() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getX();
            int y = unit.getY() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getY();
            num1 = column1.size() / Math.sqrt(Math.pow(x, 2) - Math.pow(y, 2));

            int x2 = unit.getX() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getX();
            int y2 = unit.getY() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getY();
            num2 = column2.size() / Math.sqrt(Math.pow(x2, 2) - Math.pow(y2, 2));

            num1 = Math.round(num1*1000);
            num2 = Math.round(num2*1000);
            if (num1 > num2) {
                return new Position(center.getX(), center.getY() + ai.COLUMN_HEIGHT/2);
            } else {
                return new Position(center.getX(), center.getY() - ai.COLUMN_HEIGHT/2);
            }
        } else if (row == true) {
            int x = unit.getX() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getX();
            int y = unit.getY() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getY();
            num1 = row1.size() / Math.sqrt(Math.pow(x, 2) - Math.pow(y, 2));

            int x2 = unit.getX() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getX();
            int y2 = unit.getY() + getCohesion(getNearestNeighbour(ai), getSecondNearestNeighbour(ai)).getY();
            num2 = row2.size() / Math.sqrt(Math.pow(x2, 2) - Math.pow(y2, 2));

            num1 = Math.round(num1*1000);
            num2 = Math.round(num2*1000);
            if (num1 > num2) {
                return new Position(center.getX() - ai.COLUMN_HEIGHT/2, center.getY());
            } else {
                return new Position(center.getX() + ai.COLUMN_HEIGHT/2, center.getY());

            }
        }
        else{
                System.err.println("Error!");
                return new Position(center.getX(),center.getY());
        }
    }

    public Marine getNearestNeighbour(StupidMarineAI ai){
        Marine result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for(Marine marine: ai.getMarines()){
            double distance = getDistance(marine.getUnit());
            if (distance < minDistance && marine!= this) {
                minDistance = distance;
                result = marine;
            }
        }
        return result;
    }

    public Marine getSecondNearestNeighbour(StupidMarineAI ai){
        Marine result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for(Marine marine: ai.getMarines()){
            double distance = getDistance(marine.getUnit());
            if (distance < minDistance && marine!= this && marine!=getNearestNeighbour(ai)) {
                minDistance = distance;
                result = marine;
            }
        }
        return result;
    }

    public Unit getUnit(){
        return unit;
    }

    public int getX() {
        return unit.getX();
    }

    public int getY() {
        return unit.getY();
    }

    public void setFitness(int fitness){
        this.fitness = fitness;
    }

    public int getFitness(){
        return fitness;
    }

    public void setPreviousDistance(int distance){
        previousDistance = distance;
    }

    public int getPreviousDistance(){
        return previousDistance;
    }

    public double getWeight1(){
        return weight1;
    }

    public double getWeight2(){
        return weight2;
    }

    public double getWeight3(){
        return weight3;
    }

    public double getWeight4(){
        return weight4;
    }

    public void setWeight1(double weight){
        this.weight1 = weight;
    }

    public void setWeight2(double weight){
        this.weight2 = weight;
    }

    public void setWeight3(double weight){
        this.weight3 = weight;
    }

    public void setWeight4(double weight){
        this.weight4 = weight;
    }

}