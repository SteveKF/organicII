import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;


import java.util.HashSet;

public class Vulture {

    final private JNIBWAPI bwapi;
    private final HashSet<Unit> enemyUnits;
    final private Unit unit;

    private VultureClassifier classifier;

    public Vulture(Unit unit, JNIBWAPI bwapi, HashSet<Unit> enemyUnits) {
        this.unit = unit;
        this.bwapi = bwapi;
        this.enemyUnits = enemyUnits;

        //new overall classifier of our vulture unit which does all the xcs work (not a real classifier itself)
        try {
            classifier = new VultureClassifier();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void step() {

        Unit target = getClosestEnemy();


        //updates environment every frame
        try {
            classifier.initializeEnvironment(unit, target);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void move(Unit target) {
        unit.move(new Position(target.getPosition().getPX(), target.getPosition().getPY()), false);
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
        int myX = unit.getPosition().getPX();
        int myY = unit.getPosition().getPY();

        int enemyX = enemy.getPosition().getPX();
        int enemyY = enemy.getPosition().getPY();

        int diffX = myX - enemyX;
        int diffY = myY - enemyY;

        double result = Math.pow(diffX, 2) + Math.pow(diffY, 2);

        return Math.sqrt(result);
    }

    //return the vultureclassifier
    public VultureClassifier getVultureClassifier(){
        return classifier;
    }
}
