
import javafx.geometry.Pos;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.types.WeaponType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;


public class Zerg {

    final private JNIBWAPI bwapi;
    private HashSet<Unit> enemyUnits;
    private ArrayList<Zerg> zergUnits;
    final private Unit unit;
    private ZergAI ai;
    private int id;
    private Unit target;
    public static int hatcheryX1;
    public static int hatcheryX2;
    public static int hatcheryY1;
    public static int hatcheryY2;
    public static int ownHatcheryX;
    public static int ownHatcheryY;


    private ArrayList<Zerg> column1;
    private ArrayList<Zerg> column2;
    private ArrayList<Zerg> row1;
    private ArrayList<Zerg> row2;
    private ArrayList<Zerg> neighbourhood;
    private final int NEIGHBOURHOOD_RANGE = 50;

    private int fitness;
    private int previousDistance;

    //change between learnt weights through genetic algorithm or unlearnt for boiding
    boolean learnt = true;
    double weight1 = 0.8;
    double weight2 = 0.2;
    double weight3 = 0.5;
    double weight4 = 0;
    private final int NUM_NEIGHBOURS = 2;
    public static boolean destroyed = false;
    public static boolean destroyed2 = false;
    private XCS xcs;

    public Zerg(Unit unit, JNIBWAPI bwapi, HashSet<Unit> enemyUnits, int id) throws Exception {
        this.unit = unit;
        this.bwapi = bwapi;
        this.enemyUnits = enemyUnits;
        this.id = id;
        previousDistance = 0;
        if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
            xcs = new XCS(bwapi);
        }
        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
        zergUnits = new ArrayList<>();
        neighbourhood = new ArrayList<>();
        fitness = 0;
        //reads weights and fitness from last run game
        if (learnt==true && (getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID() || getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID())) {
            String[] parameterArray = new String[5];
            int i = 0;
            BufferedReader br = new BufferedReader(new FileReader("data/parameters"
                    + getID() + ".txt"));
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

    public void step(ZergAI ai, Unit target, HashSet<Unit> enemyUnits, ArrayList<Zerg> zergUnits) {
        this.ai = ai;
        this.target = target;
        this.enemyUnits = enemyUnits;
        this.zergUnits = zergUnits;
        int x = 0, y = 0;
        int maxRange = 0;

        if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
            maxRange = bwapi.getWeaponType(((WeaponType.WeaponTypes.Claws.getID()))).getMaxRange();
        } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
            maxRange = bwapi.getWeaponType(((WeaponType.WeaponTypes.Suicide_Scourge.getID()))).getMaxRange();
        } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
            maxRange = bwapi.getWeaponType(((WeaponType.WeaponTypes.Needle_Spines.getID()))).getMaxRange();
        } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
            maxRange = bwapi.getWeaponType(((WeaponType.WeaponTypes.Kaiser_Blades.getID()))).getMaxRange();
        } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
            maxRange = bwapi.getWeaponType(((WeaponType.WeaponTypes.Ensnare.getID()))).getMaxRange();
        }

        if (enemyUnits.size() < 10) {
            if (this.target == null && destroyed == false) {
                x = hatcheryX1;
                y = hatcheryY1;
            } else if (this.target == null && destroyed == true) {
                x = hatcheryX2;
                y = hatcheryY2;
            } else if (this.target != null) {
                x = this.target.getX();
                y = this.target.getY();
            } else if (target == null && destroyed2 == true) {
                this.target = getClosestEnemy();
                x = this.target.getX();
                y = this.target.getY();
            }
        } else {
            if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                this.target = getClosestEnemyZergling();
            } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                this.target = getClosestEnemyScourge();
            } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                this.target = getClosestEnemyHydralisk();
            } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
                this.target = getClosestEnemyUltralisk();
            } else if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                this.target = getClosestEnemyQueen();
            }
            x = this.target.getX();
            y = this.target.getY();

        }


        Position targetPosition = new Position(x, y);
        if (maxRange >= Math.sqrt(getDistance(targetPosition)) && this.target != null) {

            if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                if(this.target.getTypeID()!=UnitType.UnitTypes.Zerg_Hatchery.getID()) {
                    try {
                        xcs.initializeEnvironment(this, this.target);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                bwapi.attack(unit.getID(), this.target.getID());
            }
        } else {
            if (unit.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID() ||
                    unit.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                int i;
                for(i=0;i<zergUnits.size();i++){
                    if (zergUnits.get(i).getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                        break;
                    }
                }
                if(i!=zergUnits.size()) {
                    bwapi.follow(unit.getID(), zergUnits.get(i).getUnit().getID());
                }else{
                    bwapi.move(unit.getID(),targetPosition.getX(),targetPosition.getY());
                }
            }else {
                move(targetPosition);
            }
        }
        this.target=null;
    }

    private void move(Position targetPosition) {
        //TODO: Implement the flocking behavior in this method.
        setNeighbourhood();
        /*double x = rule1(targetPosition).getX() * weight1 + +rule2().getX() * weight2 + rule3(true, false).getX() * weight3
                + rule3(false, true).getX() * weight4;
        double y = rule1(targetPosition).getY() * weight1 + rule2().getY() * weight2 + rule3(true, false).getY() * weight3
                + rule3(false, true).getY() * weight4;*/

        double x = rule1(targetPosition).getX() * weight1 + +rule2().getX() * weight2 + getCohesion(new Position(getX(), getY())).getX() * weight3;
        double y = rule1(targetPosition).getY() * weight1 + +rule2().getY() * weight2 + getCohesion(new Position(getX(), getY())).getY() * weight3;


        x = x + getX();
        y = y + getY();
        bwapi.move(unit.getID(), (int) x, (int) y);
    }

    public Unit getClosestEnemyZergling() {
        Unit result = null;
        ArrayList<Unit> zerglings = new ArrayList<>();
        ArrayList<Unit> hydralisk = new ArrayList<>();
        ArrayList<Unit> scourge = new ArrayList<>();
        ArrayList<Unit> ultralisk = new ArrayList<>();
        ArrayList<Unit> queen = new ArrayList<>();
        ArrayList<Unit> hatchery = new ArrayList<>();

        for (Unit enemy : enemyUnits) {
            if (!enemy.isBurrowed()) {
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                    hydralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                    zerglings.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
                    ultralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                    queen.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                    scourge.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hatchery.getID()) {
                    hatchery.add(enemy);
                }
            }
        }
        if (zerglings.size() > 0)
            result = getClosestEnemy(zerglings);
        else if (hydralisk.size() > 0)
            result = getClosestEnemy(hydralisk);
        else if (ultralisk.size() > 0)
            result = getClosestEnemy(ultralisk);
        else if (hatchery.size() > 0)
            result = getClosestEnemy(hatchery);
        else if (queen.size() > 0)
            result = getClosestEnemy(queen);
        else if (scourge.size() > 0)
            result = getClosestEnemy(scourge);

        return result;
    }

    public Unit getClosestEnemyUltralisk() {
        Unit result = null;
        ArrayList<Unit> zerglings = new ArrayList<>();
        ArrayList<Unit> hydralisk = new ArrayList<>();
        ArrayList<Unit> scourge = new ArrayList<>();
        ArrayList<Unit> ultralisk = new ArrayList<>();
        ArrayList<Unit> queen = new ArrayList<>();
        ArrayList<Unit> hatchery = new ArrayList<>();

        for (Unit enemy : enemyUnits) {
            if (!enemy.isBurrowed()) {
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                    hydralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                    zerglings.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
                    ultralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                    queen.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                    scourge.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hatchery.getID()) {
                    hatchery.add(enemy);
                }
            }
        }
        if (hydralisk.size() > 0)
            result = getClosestEnemy(hydralisk);
        else if (zerglings.size() > 0)
            result = getClosestEnemy(zerglings);
        else if (ultralisk.size() > 0)
            result = getClosestEnemy(ultralisk);
        else if (hatchery.size() > 0)
            result = getClosestEnemy(hatchery);
        else if (queen.size() > 0)
            result = getClosestEnemy(queen);
        else if (scourge.size() > 0)
            result = getClosestEnemy(scourge);

        return result;
    }

    public Unit getClosestEnemyHydralisk() {
        Unit result = null;
        ArrayList<Unit> zerglings = new ArrayList<>();
        ArrayList<Unit> hydralisk = new ArrayList<>();
        ArrayList<Unit> scourge = new ArrayList<>();
        ArrayList<Unit> ultralisk = new ArrayList<>();
        ArrayList<Unit> queen = new ArrayList<>();
        ArrayList<Unit> hatchery = new ArrayList<>();

        for (Unit enemy : enemyUnits) {
            if (!enemy.isBurrowed()) {
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                    hydralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                    zerglings.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
                    ultralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                    queen.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                    scourge.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hatchery.getID()) {
                    hatchery.add(enemy);
                }
            }
        }
        if (zerglings.size() > 0)
            result = getClosestEnemy(zerglings);
        else if (ultralisk.size() > 0)
            result = getClosestEnemy(ultralisk);
        else if (queen.size() > 0)
            result = getClosestEnemy(queen);
        else if (scourge.size() > 0)
            result = getClosestEnemy(scourge);
        else if (hydralisk.size() > 0)
            result = getClosestEnemy(hydralisk);
        else if (hatchery.size() > 0)
            result = getClosestEnemy(hatchery);

        return result;
    }

    public Unit getClosestEnemyScourge() {
        Unit result = null;
        ArrayList<Unit> zerglings = new ArrayList<>();
        ArrayList<Unit> hydralisk = new ArrayList<>();
        ArrayList<Unit> scourge = new ArrayList<>();
        ArrayList<Unit> ultralisk = new ArrayList<>();
        ArrayList<Unit> queen = new ArrayList<>();
        ArrayList<Unit> hatchery = new ArrayList<>();

        for (Unit enemy : enemyUnits) {
            if (!enemy.isBurrowed()) {
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                    hydralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                    zerglings.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
                    ultralisk.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                    queen.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                    scourge.add(enemy);
                }
                if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hatchery.getID()) {
                    hatchery.add(enemy);
                }
            }
        }
        if (queen.size() > 0)
            result = getClosestEnemy(queen);
        else if (scourge.size() > 0)
                result = getClosestEnemy(scourge);
        else if (hydralisk.size() > 0)
            result = getClosestEnemy(hydralisk);
        else if (zerglings.size() > 0)
            result = getClosestEnemy(zerglings);
        else if (ultralisk.size() > 0)
            result = getClosestEnemy(ultralisk);
        else if (hatchery.size() > 0)
            result = getClosestEnemy(hatchery);

        return result;
    }

    public Unit getClosestEnemyQueen() {
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

    public Unit getClosestEnemy(ArrayList<Unit> enemys) {
        Unit result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (Unit enemy : enemys) {
            double distance = getDistance(enemy);
            if (distance < minDistance) {
                minDistance = distance;
                result = enemy;
            }
        }

        return result;
    }


    public double getDistance(Position enemy) {
        int myX = unit.getX();
        int myY = unit.getY();
        int enemyX;
        int enemyY;

        enemyX = enemy.getX();
        enemyY = enemy.getY();

        int diffX = myX - enemyX;
        int diffY = myY - enemyY;

        double result = Math.pow(diffX, 2) + Math.pow(diffY, 2);

        return Math.sqrt(result);
    }

    public double getDistance(Unit enemy) {
        int myX = unit.getX();
        int myY = unit.getY();
        int enemyX;
        int enemyY;

        enemyX = enemy.getX();
        enemyY = enemy.getY();

        int diffX = myX - enemyX;
        int diffY = myY - enemyY;

        double result = Math.pow(diffX, 2) + Math.pow(diffY, 2);

        return Math.sqrt(result);
    }

    public int getID() {
        return id;
    }


    public void setNeighbourhood() {
        /*for (Zerg zerg : ai.getZergs()) {
            if (getDistance(zerg.getUnit()) < NEIGHBOURHOOD_RANGE && zerg != this) {
                neighbourhood.add(zerg);
            }
        }*/
        for (int i = 1; i <= NUM_NEIGHBOURS; i++) {
            neighbourhood.add(getNNearestNeighbour(ai, i));
        }
    }

    public Position getCohesion(Position pos) {
        int x = 0;
        int y = 0;
        for (int i = 0; i < neighbourhood.size(); i++) {
            x += neighbourhood.get(i).getX();
            y += neighbourhood.get(i).getY();
        }

        x /= neighbourhood.size();
        y /= neighbourhood.size();

        return new Position((x - pos.getX()), (y - pos.getY()));
    }


    //computes Magnitude
    public double getMagnitude(Position pos1) {
        double result = Math.sqrt(Math.pow(pos1.getX(), 2) + Math.pow(pos1.getY(), 2));
        return result;
    }

    //computes rule1 from paper
    public Position rule1(Position enemy) {
        int x = 0;
        int y = 0;

        x += (enemy.getX() - unit.getX());
        y += (enemy.getY() - unit.getY());

        return new Position(x, y);
    }

    //computes rule2 from paper
    public Position rule2() {
        int x = 0;
        int y = 0;

        for (int i = 0; i < neighbourhood.size(); i++) {
            x += (neighbourhood.get(i).getX() - getX());
            y += (neighbourhood.get(i).getY() - getY());
        }

        x /= neighbourhood.size();
        y /= neighbourhood.size();

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
                Position pos = new Position(getX() + cohesion.getX(), getY() + cohesion.getY());
                int num1 = (int) Math.round((column1.size() / getMagnitude(pos)) * 100000000);
                if (tmp1 <= num1) {
                    tmp1 = num1;
                }
            }
            //computes formula from rule3 for column2
            int tmp2 = Integer.MIN_VALUE;
            for (int i = 0; i < column2.size(); i++) {
                Position cohesion = getCohesion(new Position(column2.get(i).getX(), column2.get(i).getY()));
                Position pos = new Position(getX() + cohesion.getX(), getY() + cohesion.getY());
                int num2 = (int) Math.round((column2.size() / getMagnitude(pos)) * 100000000);
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
                Position pos = new Position(getX() + cohesion.getX(), getY() + cohesion.getY());
                int num1 = (int) Math.round((row1.size() / getMagnitude(pos)) * 100000000);
                if (tmp1 <= num1) {
                    tmp1 = num1;
                }
            }
            //computes formula from rule3 for row2
            int tmp2 = Integer.MIN_VALUE;
            for (int i = 0; i < row2.size(); i++) {
                Position cohesion = getCohesion(new Position(row2.get(i).getX(), row2.get(i).getY()));
                Position pos = new Position(getX() + cohesion.getX(), getY() + cohesion.getY());
                int num2 = (int) Math.round((row2.size() / getMagnitude(pos)) * 100000000);
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
        for (int i = 0; i < n; i++) {
            double minDistance = Double.POSITIVE_INFINITY;
            for (Zerg zerg : ai.getZergs()) {
                int counter = 0;
                double distance = getDistance(new Position(zerg.getX(), getY()));
                if (i == 0) {
                    if (distance < minDistance && zerg != this) {
                        minDistance = distance;
                        result = zerg;
                    }
                } else {
                    for (int j = 1; j < n; j++) {
                        if (check_distance[j - 1] != distance && zerg != this) {
                            counter++;
                        }
                    }
                    if (distance < minDistance && (counter + 1) == n && zerg != this) {
                        minDistance = distance;
                        result = zerg;
                    }
                }
            }
            check_distance[i] = minDistance;
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

    public XCS getXCS(){
        return xcs;
    }
}