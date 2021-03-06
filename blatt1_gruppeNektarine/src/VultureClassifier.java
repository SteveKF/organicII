import com.sun.org.apache.xpath.internal.SourceTree;
import jnibwapi.Position;
import jnibwapi.Unit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

public class VultureClassifier{
    private Classifier[] classifier;
    private int action;
    private boolean[] check_action;


    private Position previousTargetPosition;

    public VultureClassifier(){

        // initialize classifier
        classifier = new Classifier[Classifier.NUM_CONDITIONS];
        // classifier that uses the saved parameters
        for (int i = 0; i < classifier.length; i++) {
            try {
                classifier[i] = new Classifier(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void initializeEnvironment(Unit unit, Unit target) {
        if(target == null){
            //moves to previous target Position(+ random number of map size) if no target is found
            Random random = new Random();
            unit.move(new Position(previousTargetPosition.getPX()+random.nextInt(4096),
                    previousTargetPosition.getPY()+random.nextInt(3072)),false);
        }else {

            //saves Position of previous target
            previousTargetPosition = target.getPosition();

            //if environment matches classifiers it will set the condition of classifier to true
            for (int i = 0; i < classifier.length; i++) {
                classifier[i].setCondition(i, unit, target);
            }
            //generate matchset (all classifiers with condition = true)
            ArrayList<Classifier> matchset = generateMatchSet(classifier);

            //generate prediciton array
            double[] predictionarray = generatePredictionArray(matchset);

            //generate actionset
            ArrayList<Classifier> actionset = generateActionSet(predictionarray, matchset);


            //runs best action
            Classifier.selectAction(action, unit, target);

            //updates parameters
            updateParameters(actionset, predictionarray);


            //writes new parameters in files
            for(int i = 0; i < classifier.length; i++){
                try {
                    writeParameters(i);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
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

        check_action = new boolean[Classifier.NUM_ACTIONS];

        for(int i=0;i<check_action.length;i++){
            for(int j=0;j<matchset.size();j++){
                if(matchset.get(j).getAction()==i){
                    check_action[i] = true;
                }
            }
        }


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
        for(int i=0;i<predictionarray.length;i++){
        }

        double tmp = Double.NEGATIVE_INFINITY;
        int index = 0;

        for (int i = 0; i < predictionarray.length; i++) {
            if (tmp != Math.max(tmp, predictionarray[i]) && check_action[i]) {
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
                    try {
                        classifier[j].update(predictionarray);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void writeParameters(int index) throws FileNotFoundException, UnsupportedEncodingException{

        PrintWriter writer = new PrintWriter("parameters"+index+".txt", "UTF-8");


        writer.println(classifier[index].getPrecision()); // 0
        writer.println(classifier[index].getError());		// 1
        writer.println(classifier[index].getFitness());	// 2
        writer.println(classifier[index].getReward());		// 3

        writer.close();
    }
}