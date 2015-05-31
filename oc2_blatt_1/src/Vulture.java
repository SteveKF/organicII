import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitCommandType;
import jnibwapi.types.UnitType;
import jnibwapi.types.WeaponType;

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

        //new classifier of our vulture unit

        classifier = new VultureClassifier();


    }


    public void printStuff(int frame){
        if(frame % 500==0) {
            String s1 = Integer.toString(frame);
            StringBuffer s2 = new StringBuffer("");
            for(Unit x: enemyUnits){
                s2.append(x.getHitPoints());
                s2.append(",");
                s2.append(x.getShields());
                s2.append(",");
            }

            String s3 = Double.toString(unit.getHitPoints());
            System.out.printf("%s,%s%s\n",s1,s2.toString(),s3);
        }
    }

    public void step() {

        Unit target = getClosestEnemy();


        //updates environment
        classifier.initializeEnvironment(unit, target);

    }

    /**
     * TODO: Unnötig geworden?
     */
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
}
