import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;


public class ZergAI implements BWAPIEventListener, Runnable {

    private final JNIBWAPI bwapi;

    private HashSet<Unit> enemyUnits;

    public final int COLUMN_WIDTH = 100;
    public final int COLUMN_HEIGHT = 300;
    public final int ROW_WIDTH = 300;
    public final int ROW_HEIGHT = 100;

    ArrayList<Zerg> column1;
    ArrayList<Zerg> column2;
    ArrayList<Zerg> row1;
    ArrayList<Zerg> row2;

    private ArrayList<Zerg> zergs;
    private ArrayList<Unit> zergs2;
    private ArrayList<Unit> ultralisks;
    private XCS xcs;

    private int frame;
    private int zergID;

    public ZergAI() {

        bwapi = new JNIBWAPI(this, false);

    }

    public static void main(String[] args) {
        new ZergAI().run();
    }

    @Override
    public void matchStart() {
        enemyUnits = new HashSet<>();
        zergs = new ArrayList<>();
        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
        zergs2 = new ArrayList<>();
        ultralisks = new ArrayList<>();

        if (bwapi.getSelf().getColor() == 111) {
            Zerg.hatcheryX1 = 3520;
            Zerg.hatcheryY1 = 368;
            Zerg.hatcheryX2 = 3520;
            Zerg.hatcheryY2 = 2704;
            Zerg.ownHatcheryX = 576;
            Zerg.ownHatcheryY = 2704;
        } else if (bwapi.getSelf().getColor() == 165) {
            Zerg.hatcheryX1 = 576;
            Zerg.hatcheryY1 = 368;
            Zerg.hatcheryX2 = 576;
            Zerg.hatcheryY2 = 2704;
            Zerg.ownHatcheryX = 3520;
            Zerg.ownHatcheryY = 2704;
        }




        Zerg.destroyed = false;
        Zerg.destroyed2 = false;

        frame = 0;
        zergID = 0;

        //bwapi.enablePerfectInformation();
        bwapi.enableUserInput();
        bwapi.setGameSpeed(0);
    }

    @Override
    public void matchFrame() {

        if(zergs.size()>=5) {


            //add positive fitness if formation is good and unit moves towards enemy else add negative fitness
            for (Zerg x : zergs) {
                for (Zerg y : zergs) {
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
            int num1 = 1000; //probability 0.35
            double random = rn.nextDouble() * 1000;
            random = Math.round(random);
            int num2 = (int) random;

            if ((num1 - num2) > 0) {
                runGeneticAlgorithm();
            }
        }


        Unit target = getClosestEnemy();



        for (int i = 0; i < zergs.size(); i++) {
            if (zergs.get(i).getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Queen.getID()) {
                this.xcs = zergs.get(i).getXCS();
            }
            zergs.get(i).step(this, target, enemyUnits, zergs);
        }

        for(int i=0;i<zergs2.size();i++){
            if(target != null && target.getX()==Zerg.hatcheryX2 && target.getY()==Zerg.hatcheryY2 && Zerg.destroyed2==false){
                bwapi.attack(zergs2.get(i).getID(),target.getID());
            }else if(target == null && Zerg.destroyed2==false) {
                bwapi.move(zergs2.get(i).getID(), Zerg.hatcheryX2, Zerg.hatcheryY2);
            }else if(target == null && Zerg.destroyed2==true){
                bwapi.move(zergs2.get(i).getID(), Zerg.hatcheryX1, Zerg.hatcheryY1);
            }else if(target!=null && target.getX()==Zerg.hatcheryX1 && target.getY()==Zerg.hatcheryY1 && Zerg.destroyed2==true){
                bwapi.move(zergs2.get(i).getID(), Zerg.hatcheryX2, Zerg.hatcheryY2);
            }
        }

        for(int i=0;i<ultralisks.size();i++){
            if(target != null && target.getX()==Zerg.hatcheryX2 && target.getY()==Zerg.hatcheryY2 && Zerg.destroyed2==false){
                bwapi.attack(ultralisks.get(i).getID(),target.getID());
            }else if(target == null && Zerg.destroyed2==false) {
                bwapi.move(ultralisks.get(i).getID(), Zerg.hatcheryX2, Zerg.hatcheryY2);
            }else if(target == null && Zerg.destroyed2==true){
                bwapi.move(ultralisks.get(i).getID(), Zerg.hatcheryX1, Zerg.hatcheryY1);
            }else if(target!=null && target.getX()==Zerg.hatcheryX1 && target.getY()==Zerg.hatcheryY1 && Zerg.destroyed2==true){
                bwapi.move(ultralisks.get(i).getID(), Zerg.hatcheryX2, Zerg.hatcheryY2);
            }
        }

        if (frame % 1000 == 0) {
            System.out.println("Frame: " + frame);
        }
        frame++;
    }

    public double getDistance(Unit enemy, Unit unit) {
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

    @Override
    public void unitDiscover(int unitID) {

        Unit unit = bwapi.getUnit(unitID);
        int typeID = unit.getTypeID();
        try {
            if (typeID == UnitType.UnitTypes.Zerg_Zergling.getID()) {
                if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                    if (frame < 10) {
                        zergs.add(new Zerg(unit, bwapi, enemyUnits, zergID));
                        zergID++;
                    } else {
                        zergs2.add(unit);
                    }
                } else {
                    enemyUnits.add(unit);
                }
            }

            if (typeID == UnitType.UnitTypes.Zerg_Queen.getID()) {
                if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                    zergs.add(new Zerg(unit, bwapi, enemyUnits, zergID));
                    //zergID++;
                } else {
                    enemyUnits.add(unit);
                }
            }

            if (typeID == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                    zergs.add(new Zerg(unit, bwapi, enemyUnits, zergID));
                    zergID++;
                } else {
                    enemyUnits.add(unit);
                }
            }

            if (typeID == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
                if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                    ultralisks.add(unit);
                    //zergID++;
                } else {
                    enemyUnits.add(unit);
                }
            }

            if (typeID == UnitType.UnitTypes.Zerg_Scourge.getID()) {
                if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                    zergs.add(new Zerg(unit, bwapi, enemyUnits, zergID));
                    //zergID++;
                } else {
                    enemyUnits.add(unit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (typeID == UnitType.UnitTypes.Zerg_Hatchery.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                //zergs.add(new Zerg(unit, bwapi, enemyUnits, zergID));
                //zergID++;
            } else {
                enemyUnits.add(unit);
            }
        }
    }

    @Override
    public void unitDestroy(int unitID) {

        Zerg rm = null;
        for (Zerg zerg : zergs) {
            if (zerg.getUnit().getID() == unitID) {
                if (zerg.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID() || zerg.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                    try {
                        PrintWriter writer = new PrintWriter("data/parameters" + zerg.getID() + ".txt", "UTF-8");

                        writer.println(zerg.getWeight1()); // 0
                        writer.println(zerg.getWeight2());  // 1
                        writer.println(zerg.getWeight3());    // 2
                        writer.println(zerg.getWeight4());        // 3
                        writer.println(zerg.getFitness()); // 4
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                rm = zerg;
                break;
            }
        }

        zergs.remove(rm);

        Unit rmUnit = null;
        for (Unit u : enemyUnits) {
            if (u.getID() == unitID) {
                rmUnit = u;
                break;
            }
        }
        enemyUnits.remove(rmUnit);
        if (rmUnit != null) {
            if (rmUnit.getX() == Zerg.hatcheryX1 && rmUnit.getY() == Zerg.hatcheryY1) {
                System.out.println("DESTROYED!");
                Zerg.destroyed = true;
            } else if (rmUnit.getX() == Zerg.hatcheryX2 && rmUnit.getY() == Zerg.hatcheryY2) {
                System.out.println("DESTROYED2!");
                Zerg.destroyed2 = true;
            }
        }
    }

    @Override
    public void connected() {
        System.out.println("Connected");
    }

    @Override
    public void matchEnd(boolean winner) {

        //deletes all parameter files from directory data -> no useless files
        File file = new File("classifier");
        String[] myFiles;
        if (file.isDirectory()) {
            myFiles = file.list();
            for (int i = 0; i < myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);
                myFile.delete();
            }
        }

        //at the end of every game it saves the classifier parameters and the number of classifiers into seperated files
        ArrayList<Classifier> classifier = xcs.getClassifier();

        //saves parameters for each classifier
        for (int i = 0; i < classifier.size(); i++) {
            try {
                xcs.writeParameters(i);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //saves number of classifiers in file general.txt
        try {
            PrintWriter writer2 = new PrintWriter("classifier/general.txt", "UTF-8");
            writer2.println(Classifier.NUM_CLASSIFIERS);
            writer2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        for (Zerg zerg : zergs) {
            if (zerg.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID() || zerg.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                try {
                    PrintWriter writer = new PrintWriter("data/parameters" + zerg.getID() + ".txt", "UTF-8");

                    writer.println(zerg.getWeight1()); // 0
                    writer.println(zerg.getWeight2());        // 1
                    writer.println(zerg.getWeight3());    // 2
                    writer.println(zerg.getWeight4());        // 3
                    writer.println(zerg.getFitness()); // 4
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void sendText(String text) {

    }

    @Override
    public void receiveText(String text) {

    }

    @Override
    public void playerLeft(int playerID) {

    }

    @Override
    public void nukeDetect(int x, int y) {

    }

    @Override
    public void nukeDetect() {

    }

    @Override
    public void unitEvade(int unitID) {

    }

    @Override
    public void unitShow(int unitID) {

    }

    @Override
    public void unitHide(int unitID) {

    }

    @Override
    public void unitCreate(int unitID) {
    }

    @Override
    public void unitMorph(int unitID) {

    }

    @Override
    public void unitRenegade(int unitID) {

    }

    @Override
    public void saveGame(String gameName) {

    }

    @Override
    public void unitComplete(int unitID) {

    }

    @Override
    public void playerDropped(int playerID) {

    }

    @Override
    public void run() {
        bwapi.start();
    }


    public Position getCenter() {
        int sumX = 0;
        int sumY = 0;
        for (Zerg x : zergs) {
            sumX += x.getX();
            sumY += x.getY();
        }

        return new Position(sumX / zergs.size(), sumY / zergs.size());
    }

    //spans rows and columns from center of all units and computes how many are in these columns/rows
    public Position span() {
        Position center = getCenter();
        column1.clear();
        column2.clear();
        row1.clear();
        row2.clear();
        for (Zerg x : zergs) {
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
    public Zerg selectOffspring() {
        int fitnesssum = 0;
        for (Zerg x : zergs) {
            if(x.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID() || x.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                fitnesssum += x.getFitness();
            }
        }

        Random rn = new Random();
        double choicepoint = rn.nextDouble() * fitnesssum;
        fitnesssum = 0;

        for (Zerg x : zergs) {
            if(x.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID() || x.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                fitnesssum = fitnesssum + x.getFitness();
            }
            if (fitnesssum >= choicepoint) {
                return x;
            }
        }
        return null;
    }

    //genetic algorithm
    public void runGeneticAlgorithm() {
        Zerg parent1 = selectOffspring();
        Zerg parent2 = selectOffspring();
        if (parent1 == null || parent2 == null) {
            return;
        }
        Zerg child = null;
        int tmp = Integer.MAX_VALUE;
        //lowest (fitness) tuple of weights is chosen as child so that it will improve
        for (Zerg x : zergs) {
            if(x.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Zergling.getID() || x.getUnit().getTypeID() == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
                if (x.getFitness() < tmp) {
                    child = x;
                    tmp = x.getFitness();
                }
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
        } else {
            return;
        }

        Random rn = new Random();
        //for each weight run mutation with a prob of 0.1
        for (int i = 1; i < 5; i++) {
            int num1 = 1000; //probability 0.1
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


    public Unit getClosestEnemy() {
        Unit[] result = new Unit[2];
        result[0]=null;
        result[1]=null;
        double[] distance= new double[2];
        distance[0]=0;
        distance[1]=0;
        int i=0;
        for (Unit enemy : enemyUnits) {
            if (enemy.getTypeID() == UnitType.UnitTypes.Zerg_Hatchery.getID()) {
                for(int j=0;j<zergs.size();j++) {
                    distance[i] += getDistance(enemy,zergs.get(j).getUnit());
                    result[i] = enemy;
                }
                i++;
            }
        }
        if(result[0]==null && result[1]==null){
            return null;
        }
        else if(distance[0]>=distance[1] && result[1]!=null) {
            return result[1];
        }else if(distance[0]>=distance[1] && result[1]==null){
            return result[0];
        }else if(distance[0]<distance[1] && result[0]!=null) {
            return result[0];
        }else if(distance[0]<distance[1] && result[0]==null){
            return result[1];
        }else{
            return null;
        }
    }



    public ArrayList<Zerg> getZergs() {
        return zergs;
    }
    public ArrayList<Zerg> getColumn1List() {
        return column1;
    }

    public ArrayList<Zerg> getColumn2List() {
        return column2;
    }

    public ArrayList<Zerg> getRow1List() {
        return row1;
    }

    public ArrayList<Zerg> getRow2List() {
        return row2;
    }

}


