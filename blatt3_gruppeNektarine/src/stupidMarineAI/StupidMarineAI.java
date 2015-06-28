package stupidMarineAI;

import com.sun.rowset.internal.Row;
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Stefan Rudolph on 17.02.14.
 */
public class StupidMarineAI implements BWAPIEventListener, Runnable {

    private final JNIBWAPI bwapi;

    private HashSet<Marine> marines;

    private HashSet<Unit> enemyUnits;

    private int frame;
    private int marineID;

    public final int COLUMN_WIDTH = 40;
    public final int COLUMN_HEIGHT = 100;
    public final int ROW_WIDTH = 100;
    public final int ROW_HEIGHT = 40;

    ArrayList<Marine> column1;
    ArrayList<Marine> column2;
    ArrayList<Marine> row1;
    ArrayList<Marine> row2;

    public StupidMarineAI() {
        System.out.println("This is the StupidMarineAI! :)");

        bwapi = new JNIBWAPI(this, false);
    }

    public static void main(String[] args) {
        new StupidMarineAI().run();
    }

    @Override
    public void matchStart() {
        marines = new HashSet<>();
        enemyUnits = new HashSet<>();

        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();

        frame = 0;
        marineID = 0;

        bwapi.enablePerfectInformation();
        bwapi.enableUserInput();
        bwapi.setGameSpeed(0);
    }

    @Override
    public void matchFrame() {
        for (Marine m : marines) {
            m.step(this);
        }
        if(marines.size()>=5) {


            //add positive fitness if formation is good and unit moves towards enemy else add negative fitness
            for (Marine x : marines) {
                for (Marine y : marines) {
                    if (x.getDistance(y.getUnit()) <= 30 && x.getDistance(y.getUnit()) <= 10) {
                        x.setFitness(x.getFitness() + 1);
                    } else {
                        x.setFitness(x.getFitness() - 1);
                    }
                }
                if(x.getClosestEnemy()!=null) {
                    if (x.getDistance(x.getClosestEnemy()) < x.getPreviousDistance()) {
                        x.setFitness(x.getFitness() + 1);
                    } else {
                        x.setFitness(x.getFitness() - 1);
                        x.setPreviousDistance((int) (x.getDistance(x.getClosestEnemy())));
                    }
                }
            }
            Random rn = new Random();

            //runs genetic algorithm with prob of 0.35
            int num1 = 350; //probability 0.35
            double random = rn.nextDouble() * 1000;
            random = Math.round(random);
            int num2 = (int) random;

            if ((num1 - num2) > 0) {
                runGeneticAlgorithm();
            }
        }

            if (frame % 1000 == 0) {
                System.out.println("Frame: " + frame);
            }
            frame++;
        }

        @Override
        public void unitDiscover ( int unitID){
            Unit unit = bwapi.getUnit(unitID);
            int typeID = unit.getTypeID();

            if (typeID == UnitType.UnitTypes.Terran_Marine.getID()) {
                if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                    try {
                        marines.add(new Marine(unit, bwapi, enemyUnits, marineID));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    marineID++;
                } else {
                    enemyUnits.add(unit);
                }
            } else if (typeID == UnitType.UnitTypes.Terran_Vulture.getID()) {
                if (unit.getPlayerID() != bwapi.getSelf().getID()) {
                    enemyUnits.add(unit);
                }
            }
        }

        @Override
        public void unitDestroy ( int unitID){

            Marine rm = null;
            for (Marine marine : marines) {
                if (marine.getID() == unitID) {
                    rm = marine;
                    break;
                }
            }
            marines.remove(rm);

            Unit rmUnit = null;
            for (Unit u : enemyUnits) {
                if (u.getID() == unitID) {
                    rmUnit = u;
                    break;
                }
            }
            enemyUnits.remove(rmUnit);
        }

        @Override
        public void connected () {
            System.out.println("Connected");
        }

        @Override
        public void matchEnd ( boolean winner){
        }

        @Override
        public void keyPressed ( int keyCode){

        }

        @Override
        public void sendText (String text){

        }

        @Override
        public void receiveText (String text){

        }

        @Override
        public void playerLeft ( int playerID){

        }

        @Override
        public void nukeDetect ( int x, int y){

        }

        @Override
        public void nukeDetect () {

        }

        @Override
        public void unitEvade ( int unitID){

        }

        @Override
        public void unitShow ( int unitID){

        }

        @Override
        public void unitHide ( int unitID){

        }

        @Override
        public void unitCreate ( int unitID){
        }

        @Override
        public void unitMorph ( int unitID){

        }

        @Override
        public void unitRenegade ( int unitID){

        }

        @Override
        public void saveGame (String gameName){

        }

        @Override
        public void unitComplete ( int unitID){

        }

        @Override
        public void playerDropped ( int playerID){

        }

        @Override
        public void run () {
            bwapi.start();
        }

    //computes center position of all units
    public Position getCenter() {
        int sumX = 0;
        int sumY = 0;
        for (Marine x : marines) {
            sumX += x.getX();
            sumY += x.getY();
        }

        return new Position(sumX / marines.size(), sumY / marines.size());
    }

    //spans rows and columns from center of all units and computes how many are in these columns/rows
    public Position span() {
        Position center = getCenter();
        column1.clear();
        column2.clear();
        row1.clear();
        row2.clear();
        for (Marine x : marines) {
            //column1
            if (x.getX() <= getCenter().getX() && x.getX() >= center.getX() - COLUMN_WIDTH && x.getY() >= center.getY() - COLUMN_HEIGHT / 2
                    && x.getY() <= center.getY() + COLUMN_HEIGHT / 2) {
                column1.add(x);
            }
            //column2
            if (x.getX() >= center.getX() && x.getX() <= center.getX() + COLUMN_WIDTH && x.getY() >= center.getY() - COLUMN_HEIGHT / 2
                    && x.getY() <= center.getY() + COLUMN_HEIGHT / 2) {
                column2.add(x);
            }

            //row1
            if (x.getX() <= center.getX() + ROW_WIDTH / 2 && x.getX() >= center.getX() - ROW_WIDTH / 2 && x.getY() >= center.getY() - ROW_HEIGHT
                    && x.getY() <= center.getY()) {
                row1.add(x);
            }
            //row2
            if (x.getX() <= center.getX() + ROW_WIDTH / 2 && x.getX() >= center.getX() - ROW_WIDTH / 2 && x.getY() <= center.getY() + ROW_HEIGHT
                    && x.getY() >= center.getY()) {
                row2.add(x);
            }

        }
        return center;
    }

    //select one parents with high fitness
    public Marine selectOffspring() {
        int fitnesssum = 0;
        for (Marine x : marines) {
            fitnesssum += x.getFitness();
        }

        Random rn = new Random();
        double choicepoint = rn.nextDouble() * fitnesssum;
        fitnesssum = 0;

        for (Marine x : marines) {
            fitnesssum = fitnesssum + x.getFitness();
            if (fitnesssum >= choicepoint) {
                return x;
            }
        }
        return null;
    }

    //genetic algorithm
    public void runGeneticAlgorithm() {
        Marine parent1 = selectOffspring();
        Marine parent2 = selectOffspring();
        if (parent1 == null || parent2 == null) {
            return;
        }
        Marine child = null;
        int tmp = Integer.MAX_VALUE;
        //lowest (fitness) tuple of weights is chosen as child so that it will improve
        for (Marine x : marines) {
            if (x.getFitness() < tmp) {
                child = x;
                tmp = x.getFitness();
            }
        }
        //applyCrossover child gets weights from each parent with a specific proportion
        if (child != null) {
            Random rn = new Random();
            double prob = rn.nextDouble();
            child.setWeight1(parent1.getWeight1() * prob + parent2.getWeight1() * (1 - prob));
            child.setWeight2(parent1.getWeight2() * prob + parent2.getWeight2() * (1 - prob));
            child.setWeight3(parent1.getWeight3() * prob + parent2.getWeight3() * (1 - prob));
            child.setWeight4(parent1.getWeight4() * prob + parent2.getWeight4() * (1 - prob));
        }else{
            return;
        }

        Random rn = new Random();
        //for each weight run mutation with a prob of 0.1
        for (int i = 1; i < 5; i++) {
            int num1 = 100; //probability 0.1
            double random = rn.nextDouble() * 1000;
            random = Math.round(random);
            int num2 = (int) random;

            if ((num1 - num2) > 0) {
                //mutates Weight between 0 and 1
                switch (i) {
                    case 1:
                        child.setWeight1(rn.nextDouble());
                        break;
                    case 2:
                        child.setWeight2(rn.nextDouble());
                        break;
                    case 3:
                        child.setWeight3(rn.nextDouble());
                        break;
                    case 4:
                        child.setWeight4(rn.nextDouble());
                        break;
                }
            }
        }
    }

    public HashSet<Marine> getMarines() {
        return marines;
    }

    public ArrayList<Marine> getColumn1List() {
        return column1;
    }

    public ArrayList<Marine> getColumn2List() {
        return column2;
    }

    public ArrayList<Marine> getRow1List() {
        return row1;
    }

    public ArrayList<Marine> getRow2List() {
        return row2;
    }

}


