import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;


public class VultureAI  implements BWAPIEventListener, Runnable {

    private final JNIBWAPI bwapi;

    private Vulture vulture;

    private HashSet<Unit> enemyUnits;

    private int frame;

    public VultureAI() {
        System.out.println("This is the VultureAI! :)");

        bwapi = new JNIBWAPI(this, false);
    }

    public static void main(String[] args) {
        new VultureAI().run();
    }

    @Override
    public void matchStart() {
        enemyUnits = new HashSet<Unit>();

        frame = 0;

        bwapi.enablePerfectInformation();
        bwapi.enableUserInput();
        bwapi.setGameSpeed(0);
    }

    @Override
    public void matchFrame() {

        vulture.step();


        /*if (frame % 1000 == 0) {
            System.out.println("Frame: " + frame);
        }*/
        frame++;

    }

    @Override
    public void unitDiscover(int unitID) {
        Unit unit = bwapi.getUnit(unitID);
        UnitType type = unit.getType();

        if (type == UnitType.UnitTypes.Terran_Vulture) {
            if (unit.getPlayer() == bwapi.getSelf()) {
                this.vulture = new Vulture(unit, bwapi, enemyUnits);
            }
        } else if (type == UnitType.UnitTypes.Protoss_Zealot) {
            if (unit.getPlayer() != bwapi.getSelf()) {
                enemyUnits.add(unit);
            }
        }
    }

    @Override
    public void unitDestroy(int unitID) {
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

        //deletes all parameter files from directory data -> no useless files
        File file = new File("data");
        String[] myFiles;
        if(file.isDirectory()){
            myFiles = file.list();
            for (int i=0; i<myFiles.length; i++) {
                File myFile = new File(file, myFiles[i]);
                myFile.delete();
            }
        }

        //at the end of every game it saves the classifier parameters and the number of classifiers into seperated files
        ArrayList<Classifier> classifier = vulture.getVultureClassifier().getClassifier();

        //saves parameters for each classifier
        for (int i = 0; i < classifier.size(); i++) {
            try {
                vulture.getVultureClassifier().writeParameters(i);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        //saves number of classifiers in file general.txt
        try {
            PrintWriter writer2 = new PrintWriter("general.txt", "UTF-8");
            writer2.println(Classifier.NUM_CLASSIFIERS);
            writer2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
    public void nukeDetect(Position position) {

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
}
