package stupidMarineAI;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Stefan Rudolph on 17.02.14.
 */
public class StupidMarineAI implements BWAPIEventListener, Runnable {

    private final JNIBWAPI bwapi;

    private HashSet<Marine> marines;

    private HashSet<Unit> enemyUnits;

    private int frame;
    private int marineID = 0;

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

        frame = 0;

        bwapi.enablePerfectInformation();
        bwapi.enableUserInput();
        bwapi.setGameSpeed(0);
    }

    @Override
    public void matchFrame() {

        for (Marine m : marines) {
            m.step();
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

    public ArrayList<ArrayList> span(){
        ArrayList<ArrayList> list = new ArrayList<>();
        ArrayList<Marine> column1 = new ArrayList<>();
        ArrayList<Marine> column2 = new ArrayList<>();
        ArrayList<Marine> row1 = new ArrayList<>();
        ArrayList<Marine> row2 = new ArrayList<>();
        for(Marine x: marines){
            //columns
            if(x.getX()<getCenter().getX()) {
                if(x.getX() < getCenter().getX()+50 && x.getX() > getCenter().getX()-50
                        && x.getY()<getCenter().getY()-0 && x.getY()>getCenter().getY()+40) {
                    column1.add(x);
                }
            }
            else {
                if(x.getX() < getCenter().getX()+50 && x.getX() > getCenter().getX()-50
                        && x.getY()<getCenter().getY()-40 && x.getY()>getCenter().getY()+0) {
                    column2.add(x);
                }
            }

            //rows
            if(x.getX()<getCenter().getX()) {
                if(x.getX() < getCenter().getX()+40 && x.getX() > getCenter().getX()-0
                        && x.getY()<getCenter().getY()+50 && x.getY()>getCenter().getY()-50) {
                    row1.add(x);
                }
            }
            else {
                if(x.getX() < getCenter().getX()+0 && x.getX() > getCenter().getX()-40
                        && x.getY()<getCenter().getY()+50 && x.getY()>getCenter().getY()-50) {
                    row2.add(x);
                }
            }
        }
        ArrayList<Position> pos = new ArrayList<>();
        pos.add(new Position(getCenter().getX(),getCenter().getY()));

        list.add(column1); //0
        list.add(column2); //1
        list.add(row1); //2
        list.add(row2); //3
        list.add(pos); //4
        return list;
    }

    public static Marine getNearestNeighbour(){
        return ;
    }

    public static Marine getSecNearestNeighbour(){
        return;
    }
}


