package stupidMarineAI;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

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
    private int marineID = 0;

    public final int COLUMN_WIDTH = 100;
    public final int COLUMN_HEIGHT = -40;

    ArrayList<Marine> column1;
    ArrayList<Marine> column2;
    ArrayList<Marine> row1;
    ArrayList<Marine> row2;

    public StupidMarineAI() {
        System.out.println("This is the StupidMarineAI! :)");

        bwapi = new JNIBWAPI(this, false);

        column1 = new ArrayList<>();
        column2 = new ArrayList<>();
        row1 = new ArrayList<>();
        row2 = new ArrayList<>();
    }

    public static void main(String[] args) {
        new StupidMarineAI().run();
    }

    @Override
    public void matchStart() {
        marines = new HashSet<>();
        enemyUnits = new HashSet<>();

        frame = 0;

        bwapi.enablePerfectInformation();
        bwapi.enableUserInput();
        bwapi.setGameSpeed(0);
    }

    @Override
    public void matchFrame() {
            for (Marine x : marines) {
                if(marines.size()>=3 && enemyUnits.size()!=0 && marines!=null) {
                    if (x.getNearestNeighbour(this).getDistance(x.getUnit()) < 10) {
                        x.setFitness(x.getFitness() + 10);
                    }
                    if (x.getDistance(x.getClosestEnemy()) < x.getPreviousDistance()) {
                        x.setFitness(x.getFitness() + 10);
                    }
                    x.setPreviousDistance((int) (x.getDistance(x.getClosestEnemy())));
                }
            }
        applyCrossover();
        for (Marine m : marines) {
            m.step(this);
        }

        if (frame % 1000 == 0) {
            System.out.println("Frame: " + frame);
        }
        frame++;
    }

    @Override
    public void unitDiscover(int unitID) {
        Unit unit = bwapi.getUnit(unitID);
        int typeID = unit.getTypeID();

        if (typeID == UnitType.UnitTypes.Terran_Marine.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                marines.add(new Marine(unit, bwapi, enemyUnits, marineID));
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
    public void unitDestroy(int unitID) {
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
    public void connected() {
        System.out.println("Connected");
    }

    @Override
    public void matchEnd(boolean winner) {
        for(Marine x: marines) {
            System.out.printf("Weight1: %s, Weight2: %s, Weight3: %s, Weight4: %s\n",x.getWeight1(),
                    x.getWeight2(),x.getWeight3(),x.getWeight4());
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

    public Position getCenter(){
        int sumX = 0;
        int sumY = 0;
        for(Marine x: marines){
            sumX += x.getX();
            sumY += x.getY();
        }

        return new Position(sumX/marines.size(),sumY/marines.size());
    }

    public Position span(){
        for(Marine x: marines){
            //columns
            if(x.getX()<getCenter().getX()) {
                if(x.getX() < getCenter().getX()+COLUMN_WIDTH/2 && x.getX() > getCenter().getX()-COLUMN_WIDTH/2
                        && x.getY()<getCenter().getY()-0 && x.getY()>getCenter().getY()+COLUMN_HEIGHT) {
                    if(column1.size()<(marines.size()/2)) {
                        column1.add(x);
                    }else{
                        column2.add(x);
                    }
                }
            }
            else {
                if(x.getX() < getCenter().getX()+COLUMN_WIDTH/2 && x.getX() > getCenter().getX()-COLUMN_WIDTH/2
                        && x.getY()<getCenter().getY()-COLUMN_HEIGHT && x.getY()>getCenter().getY()+0) {
                    if(column2.size()<(marines.size()/2)) {
                        column2.add(x);
                    }else{
                        column1.add(x);
                    }
                }
            }

            //rows
            if(x.getX()<getCenter().getX()) {
                if(x.getX() < getCenter().getX()+COLUMN_HEIGHT && x.getX() > getCenter().getX()-0
                        && x.getY()<getCenter().getY()+COLUMN_WIDTH/2 && x.getY()>getCenter().getY()-COLUMN_WIDTH/2) {
                    if(row1.size()<(marines.size()/2)) {
                        row1.add(x);
                    }else{
                        row2.add(x);
                    }
                }
            }
            else {
                if(x.getX() < getCenter().getX()+0 && x.getX() > getCenter().getX()-COLUMN_HEIGHT
                        && x.getY()<getCenter().getY()+COLUMN_WIDTH/2 && x.getY()>getCenter().getY()-COLUMN_WIDTH/2) {
                    if(row2.size()<(marines.size()/2)) {
                        row2.add(x);
                    }else{
                        row1.add(x);
                    }
                }
            }
        }
        return new Position(getCenter().getX(),getCenter().getY());
    }

    public Marine selectOffspring(){
        int fitnesssum = 0;
        for(Marine x: marines){
            fitnesssum += x.getFitness();
        }

        Random rn = new Random();
        double choicepoint = rn.nextDouble() * fitnesssum;
        fitnesssum = 0;

        for(Marine x: marines){
            fitnesssum = fitnesssum + x.getFitness();
            if(fitnesssum >= choicepoint){
                return x;
            }
        }
        return null;
    }

    //TODO: implement prob to run crossover
    public void applyCrossover(){
        Marine parent1 = selectOffspring();
        Marine parent2 = selectOffspring();
        if(parent1==null || parent2==null){
            System.err.println("Error2!\n");
        }
        Marine child = null;
        int tmp = Integer.MAX_VALUE;
        for(Marine x: marines){
            if(x.getFitness()<=tmp){
                child = x;
                tmp = x.getFitness();
            }
            System.out.printf("Fitness: %s, Tmp: %s\n", x.getFitness(), tmp);
        }
        if(child!=null) {
            Random rn = new Random();
            double prob = rn.nextDouble();
            child.setWeight1(parent1.getWeight1() * prob + parent2.getWeight1()* (1-prob));
            child.setWeight2(parent1.getWeight2() * prob + parent2.getWeight2() * (1 - prob));
            child.setWeight3(parent1.getWeight3() * prob + parent2.getWeight3() * (1 - prob));
            child.setWeight4(parent1.getWeight4() * prob + parent2.getWeight4() * (1 - prob));
        }

        Random rn = new Random();

        int num1 = 500; //probability 0.05
        double random = rn.nextDouble() * 1000;
        random = Math.round(random);
        int num2 = (int) random;

        if ((num1 - num2) > 0) {
                System.out.println("Hallo1!");
                child.setWeight1(child.getWeight1()*rn.nextDouble());
                child.setWeight2(child.getWeight2() * rn.nextDouble());
                child.setWeight3(child.getWeight3() * rn.nextDouble());
                child.setWeight4(child.getWeight4() * rn.nextDouble());
        }
    }

    public HashSet<Marine> getMarines(){
        return marines;
    }

    public ArrayList<Marine> getColumn1List(){
        return column1;
    }

    public ArrayList<Marine> getColumn2List(){
        return column2;
    }

    public ArrayList<Marine> getRow1List(){
        return row1;
    }

    public ArrayList<Marine> getRow2List(){
        return row2;
    }

}


