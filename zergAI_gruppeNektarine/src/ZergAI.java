

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;
import jnibwapi.types.UnitType;

import java.util.HashSet;


public class ZergAI implements BWAPIEventListener, Runnable {

    private final JNIBWAPI bwapi;

    private HashSet<Unit> queens;
    private HashSet<Unit> scourges;
    private HashSet<Unit> ultralisks;
    private HashSet<Unit> hydralisks;
    private HashSet<Unit> zerglings;
    private HashSet<Unit> enemyUnits;

    private HashSet<Unit> units;

    private int frame;
    private int queenID = 0;
    private int scourgeID = 0;
    private int ultraliskID = 0;
    private int hydraliskID = 0;
    private int zerglingID = 0;

    public ZergAI() {
        System.out.println("This is the StupidMarineAI! :)");

        bwapi = new JNIBWAPI(this, false);
    }

    public static void main(String[] args) {
        new ZergAI().run();
    }

    @Override
    public void matchStart() {
        queens = new HashSet<>();
        scourges = new HashSet<>();
        ultralisks = new HashSet<>();
        hydralisks = new HashSet<>();
        zerglings = new HashSet<>();
        enemyUnits = new HashSet<>();

        frame = 0;

        bwapi.enablePerfectInformation();
        bwapi.enableUserInput();
        bwapi.setGameSpeed(0);

        System.out.println("Queen Size"+queens.size());
        System.out.println("Scourge Size" + scourges.size());
        System.out.println("Ultralisk Size" + ultralisks.size());
        System.out.println("Hydralisk Size" + hydralisks.size());
        System.out.println("Zergling Size" + zerglings.size());
    }

    @Override
    public void matchFrame() {

        /*for (Marine m : marines) {
            m.step();
        }*/

         Unit target = getClosestEnemy(queens.iterator().next());

        for(Unit x: queens){
            bwapi.move(x.getID(),target.getX(),target.getY());
        }
        for(Unit x: scourges){
            bwapi.move(x.getID(),target.getX(),target.getY());
        }
        for(Unit x: ultralisks){
            bwapi.move(x.getID(),target.getX(),target.getY());
        }
        for(Unit x: hydralisks){
            bwapi.move(x.getID(),target.getX(),target.getY());
        }
        for(Unit x: zerglings){
            bwapi.move(x.getID(),target.getX(),target.getY());
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

        if (typeID == UnitType.UnitTypes.Zerg_Queen.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                queens.add(unit);
                queenID++;
            } else {
                enemyUnits.add(unit);
            }
        } else if (typeID == UnitType.UnitTypes.Zerg_Scourge.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                scourges.add(unit);
                scourgeID++;
            } else {
                enemyUnits.add(unit);
            }
        } else if (typeID == UnitType.UnitTypes.Zerg_Ultralisk.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                ultralisks.add(unit);
                ultraliskID++;
            } else {
                enemyUnits.add(unit);
            }
        } else if (typeID == UnitType.UnitTypes.Zerg_Hydralisk.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                hydralisks.add(unit);
                hydraliskID++;
            } else {
                enemyUnits.add(unit);
            }
        } else if (typeID == UnitType.UnitTypes.Zerg_Zergling.getID()) {
            if (unit.getPlayerID() == bwapi.getSelf().getID()) {
                zerglings.add(unit);
                zerglingID++;
            } else {
                enemyUnits.add(unit);
            }
        }
    }

    @Override
    public void unitDestroy(int unitID) {
        Unit rmQueen = null;
        for (Unit queen : queens) {
            if (queen.getID() == unitID) {
                rmQueen = queen;
                break;
            }
        }
        queens.remove(rmQueen);

        Unit rmScourge = null;
        for (Unit scourge : scourges) {
            if (scourge.getID() == unitID) {
                rmScourge = scourge;
                break;
            }
        }
        scourges.remove(rmScourge);

        Unit rmUltralisk = null;
        for (Unit ultralisk : ultralisks) {
            if (ultralisk.getID() == unitID) {
                rmUltralisk = ultralisk;
                break;
            }
        }
        ultralisks.remove(rmUltralisk);

        Unit rmHydralisk = null;
        for (Unit hydralisk : hydralisks) {
            if (hydralisk.getID() == unitID) {
                rmHydralisk = hydralisk;
                break;
            }
        }
        hydralisks.remove(rmHydralisk);

        Unit rmZergling = null;
        for (Unit zergling : zerglings) {
            if (zergling.getID() == unitID) {
                rmZergling = zergling;
                break;
            }
        }
        zerglings.remove(rmZergling);

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
        System.out.println("Queen Size"+queens.size());
        System.out.println("Scourge Size" + scourges.size());
        System.out.println("Ultralisk Size" + ultralisks.size());
        System.out.println("Hydralisk Size" + hydralisks.size());
        System.out.println("Zergling Size" + zerglings.size());
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

    private Unit getClosestEnemy(Unit unit) {
        Unit result = null;
        double minDistance = Double.POSITIVE_INFINITY;
        for (Unit enemy : enemyUnits) {
            double distance = getDistance(enemy,unit);
            if (distance < minDistance) {
                minDistance = distance;
                result = enemy;
            }
        }

        return result;
    }

    private double getDistance(Unit enemy, Unit unit) {
        int myX = unit.getX();
        int myY = unit.getY();

        int enemyX = enemy.getX();
        int enemyY = enemy.getY();

        int diffX = myX - enemyX;
        int diffY = myY - enemyY;

        double result = Math.pow(diffX, 2) + Math.pow(diffY, 2);

        return Math.sqrt(result);
    }
}


