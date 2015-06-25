package stupidMarineAI;

import javafx.geometry.Pos;
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

    private ArrayList<Marine> column1 = new ArrayList<>();
    private ArrayList<Marine> column2 = new ArrayList<>();
    private ArrayList<Marine> row1 = new ArrayList<>();
    private ArrayList<Marine> row2 = new ArrayList<>();

    public Marine(Unit unit, JNIBWAPI bwapi, HashSet<Unit> enemyUnits, int id) {
        this.unit = unit;
        this.bwapi = bwapi;
        this.enemyUnits = enemyUnits;
        this.id = id;

    }

    public void step() {
        Unit target = getClosestEnemy();

        if (unit.getOrderID() != 10 && !unit.isAttackFrame() && !unit.isStartingAttack() && !unit.isAttacking() && target != null) {
            if (bwapi.getWeaponType(WeaponType.WeaponTypes.Gauss_Rifle.getID()).getMaxRange() > getDistance(target) - 20.0) {
                bwapi.attack(unit.getID(), target.getID());
            } else {
                move(target);
            }
        }
    }

    private void move(Unit target) {
        //TODO: Implement the flocking behavior in this method.
        bwapi.move(unit.getID(), target.getX(), target.getY());
    }

    private Unit getClosestEnemy() {
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

    private double getDistance(Unit enemy) {
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

        int x = target.getX() - unit.getX();
        int y = target.getY() - unit.getY();

        return new Position(x, y);
    }

    public int getX() {
        return unit.getX();
    }

    public int getY() {
        return unit.getY();
    }

    public Position rule3(boolean row, boolean column, StupidMarineAI ai) {
        this.ai = ai;
        ArrayList<ArrayList> list = ai.span();
        column1 = list.get(0);
        column2 = list.get(1);
        row1 = list.get(2);
        row2 = list.get(3);
        Position center = (Position) list.get(4).get(0);
        double num1 = 0.0;
        double num2 = 0.0;
        if (column == true) {
            int x = unit.getX() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getX();
            int y = unit.getY() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getY();
            num1 = column1.size() / Math.sqrt(Math.pow(x, 2) - Math.pow(y, 2));

            int x2 = unit.getX() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getX();
            int y2 = unit.getY() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getY();
            num2 = column2.size() / Math.sqrt(Math.pow(x2, 2) - Math.pow(y2, 2));
            if (num1 > num2) {
                return new Position(center.getX(), center.getY() + 20);
            } else {
                return new Position(center.getX(), center.getY() - 20);
            }
        } else if (row == true) {
            int x = unit.getX() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getX();
            int y = unit.getY() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getY();
            num1 = row1.size() / Math.sqrt(Math.pow(x, 2) - Math.pow(y, 2));

            int x2 = unit.getX() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getX();
            int y2 = unit.getY() + getCohesion(StupidMarineAI.getNearestNeighbour(), StupidMarineAI.getSecNearestNeighbour()).getY();
            num2 = row2.size() / Math.sqrt(Math.pow(x2, 2) - Math.pow(y2, 2));
            if (num1 > num2) {
                return new Position(center.getX() - 20, center.getY());
            } else {
                return new Position(center.getX() + 20, center.getY());

            }
        }
        else{
                System.err.println("Error!");
                return new Position(center.getX(),center.getY());
        }
    }
}