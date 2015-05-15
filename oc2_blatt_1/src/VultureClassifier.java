import jnibwapi.Position;
import jnibwapi.Unit;
import java.util.ArrayList;
import java.util.Random;

public class VultureClassifier {
    private Classifier[] classifier;
    private int action;
    private Position previousTargetPosition;

    public VultureClassifier() {
        //initialize classifier
        classifier = new Classifier[Classifier.NUM_CONDITIONS];
        for(int i=0;i<classifier.length;i++) {
            classifier[i] = new Classifier();
        }
    }

    public void initializeEnvironment(Unit unit, Unit target) {
        if(target == null){
            //moves to previous target Position(+ random number of map size) if no target is found
            Random random = new Random();
            unit.move(new Position(previousTargetPosition.getPX()+random.nextInt(4096),
                    previousTargetPosition.getPY()+random.nextInt(3072)),false);
        }else {
            previousTargetPosition = target.getPosition();
            //initialize condition for classifiers for this environment(unit, target)
            for (int i = 0; i < classifier.length; i++) {
                classifier[i].setCondition(i, unit, target);
            }
            //generate matchset
            ArrayList<Classifier> matchset = generateMatchSet(classifier);

            //generate prediciton array
            double[] predictionarray = generatePredictionArray(matchset);

            //generate actionset
            ArrayList<Classifier> actionset = generateActionSet(predictionarray, matchset);


            //runs best action
            Classifier.selectAction(action, unit, target);

            //updates parameters
            updateParameters(actionset, predictionarray);
        }
    }

    private ArrayList<Classifier> generateMatchSet(Classifier[] classifier) {
        ArrayList<Classifier> matchset = new ArrayList<>();
        for (int i = 0; i < classifier.length; i++) {
            if (classifier[i].getCondition()) {
                matchset.add(classifier[i]);
            }
        }
        return matchset;
    }

    private double[] generatePredictionArray(ArrayList<Classifier> matchset) {
        double[] predictionarray = new double[Classifier.NUM_ACTIONS];
        double[] fitnesssum = new double[Classifier.NUM_ACTIONS];
        int j = 0;
        while (j < matchset.size()) {
            for (int i = 0; i < Classifier.NUM_ACTIONS; i++) {
                if (matchset.get(j).getAction() == i) {
                    predictionarray[i] = predictionarray[i] + matchset.get(j).getPrecision() * matchset.get(j).getFitness();
                    fitnesssum[i] += matchset.get(j).getFitness();
                }
            }
            j++;
        }

        for (int i = 0; i < predictionarray.length; i++) {
            if (fitnesssum[i] == 0)
                predictionarray[i] = 0;
            else
                predictionarray[i] /= fitnesssum[i];
        }
        return predictionarray;
    }

    //returns Actionset and sets action
    private ArrayList<Classifier> generateActionSet(double[] predictionarray,ArrayList<Classifier> matchset){
        double tmp = Double.NEGATIVE_INFINITY;
        int index = 0;
        for (int i = 0; i < predictionarray.length; i++) {
            if (tmp != Math.max(tmp, predictionarray[i])) {
                tmp = predictionarray[i];
                index = i;
            }
        }

        action = index;

        ArrayList<Classifier> actionset = new ArrayList<>();

        for(int i=0;i<matchset.size();i++){
            if(matchset.get(i).getAction()==index){
                actionset.add(matchset.get(i));
            }
        }
        return actionset;
    }


    public void updateParameters(ArrayList<Classifier> actionset,double[] predictionarray){

        //update parameters for each classifier which is in actionset
        for(int i=0;i<actionset.size();i++){
            for(int j=0;j<classifier.length;j++){
                if(actionset.get(i)==classifier[j]){
                    classifier[j].update(predictionarray);
                }
            }
        }
    }
}

