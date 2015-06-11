import com.sun.org.apache.xpath.internal.SourceTree;
import jnibwapi.Position;
import jnibwapi.Unit;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class VultureClassifier {
    private ArrayList<Classifier> classifier;
    private int action;
    private boolean[] check_action;


    private Position previousTargetPosition;

    public VultureClassifier() throws Exception{

        //reads parameter NUM_CONDITION from file
        if(Classifier.unlearnt == false) {
            String parameter = "";
            BufferedReader br = new BufferedReader(new FileReader("general.txt"));
            String line;
            if ((line = br.readLine()) != null)
                parameter = line;
            // Initialize with established start parameters
            Classifier.setNumConditions(Integer.parseInt(parameter));
        }

        // initialize classifier
        classifier = new ArrayList<>();
        // classifier that uses the saved parameters
        for (int i = 0; i < Classifier.NUM_CONDITIONS; i++) {
            try {
                classifier.add(new Classifier(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void initializeEnvironment(Unit unit, Unit target) throws Exception{
        if (target == null) {
            //moves to previous target Position(+ random number of map size) if no target is found
            Random random = new Random();
            unit.move(new Position(previousTargetPosition.getPX() + random.nextInt(4096),
                    previousTargetPosition.getPY() + random.nextInt(3072)), false);
        } else {

            //saves Position of previous target
            previousTargetPosition = target.getPosition();

            //if environment matches classifiers it will set the condition of classifier to true
            for (int i = 0; i < classifier.size(); i++) {
                classifier.get(i).setCondition(i, unit, target);
            }
            //generate matchset (all classifiers with condition = true)
            ArrayList<Classifier> matchset = generateMatchSet(classifier);

            //generate prediciton array
            double[] predictionarray = generatePredictionArray(matchset);

            //generate actionset
            ArrayList<Classifier> actionset = generateActionSet(predictionarray, matchset);


            //run genetic algorithm
            /**
             * TODO: uncomment if implementation complete
             */
            //runGeneticAlgorithm(actionset);

            //runs best action
            Classifier.selectAction(action, unit, target);

            //updates parameters
            updateParameters(actionset, predictionarray);


            //writes new parameters in files
            for (int i = 0; i < classifier.size(); i++) {
                try {
                    writeParameters(i);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            //write NUM_CONDITIONS to a file
            PrintWriter writer2 = new PrintWriter("general.txt", "UTF-8");
            writer2.println(Classifier.NUM_CONDITIONS);
            writer2.close();
        }
    }

    private ArrayList<Classifier> generateMatchSet(ArrayList<Classifier> cl) {
        ArrayList<Classifier> matchset = new ArrayList<>();
        for (int i = 0; i < cl.size(); i++) {
            if (cl.get(i).getCondition()) {
                matchset.add(cl.get(i));
            }
        }
        return matchset;
    }

    private double[] generatePredictionArray(ArrayList<Classifier> matchset) {

        check_action = new boolean[Classifier.NUM_ACTIONS];

        for (int i = 0; i < check_action.length; i++) {
            for (int j = 0; j < matchset.size(); j++) {
                if (matchset.get(j).getAction() == i) {
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
    private ArrayList<Classifier> generateActionSet(double[] predictionarray, ArrayList<Classifier> matchset) {
        for (int i = 0; i < predictionarray.length; i++) {
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

        for (int i = 0; i < matchset.size(); i++) {
            if (matchset.get(i).getAction() == index) {
                actionset.add(matchset.get(i));
            }
        }
        return actionset;
    }


    public void updateParameters(ArrayList<Classifier> actionset, double[] predictionarray) {


        //update parameters for each classifier which is in actionset
        for (int i = 0; i < actionset.size(); i++) {
            for (int j = 0; j < classifier.size(); j++) {
                if (actionset.get(i) == classifier.get(j)) {
                    try {
                        classifier.get(j).update(predictionarray);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void writeParameters(int index) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = new PrintWriter("parameters" + index + ".txt", "UTF-8");


        writer.println(classifier.get(index).getPrecision()); // 0
        writer.println(classifier.get(index).getError());        // 1
        writer.println(classifier.get(index).getFitness());    // 2
        writer.println(classifier.get(index).getReward());        // 3
        //writer.println(classifier[index].getGeneticArray());		// 4
        for (int j = 0; j < Classifier.NUM_CONDITIONS; j++) {
            writer.print(classifier.get(index).getGeneticArray()[j]);
        }


        writer.close();
    }

    public void runGeneticAlgorithm(ArrayList<Classifier> actionset) {
        /**
         * TODO: Time constraint
         */

        Classifier parent1 = selectOffspring(actionset);
        Classifier parent2 = selectOffspring(actionset);

        if(parent1 == null || parent2 == null)
            return;

        Classifier child[] = new Classifier[2];
        child[0] = parent1;
        child[1] = parent2;

        Random rn = new Random();
        /**
         * TODO: Which probability to choose
         */
        int num1 = 500; //probability 0.5
        double random = rn.nextDouble() * 1000;
        random = Math.round(random);
        int num2 = (int) random;

        if ((num1 - num2) > 0) {
            applyCrossover(child[0], child[1]);

            /**
             * TODO: First multiplication or first division
             */

            child[0].setPrecision((parent1.getPrecision() + parent2.getPrecision()) / 2);
            child[0].setError(0.25 * ((parent1.getError() + parent2.getError()) / 2));
            child[0].setFitness(0.1 * ((parent1.getFitness() + parent2.getFitness()) / 2));

            child[1].setPrecision(child[0].getPrecision());
            child[1].setError(child[0].getError());
            child[1].setFitness(child[0].getFitness());

            for (int i = 0; i < child.length; i++) {
                applyMutation(child[i]);
                if (doGASubsumption()) {
                    if (doesSubsume(parent1, child[i])) {
                        //mach was
                    } else if (doesSubsume(parent2, child[i])) {
                    }
                    //mach was
                    else {
                        insertPopulation(child[i]);
                    }
                } else {
                    insertPopulation(child[i]);
                }
                deletePopulation();
            }


        }
    }

    public Classifier selectOffspring(ArrayList<Classifier> actionset) {
        double fitnesssum = 0;
        for (int i = 0; i < actionset.size(); i++) {
            fitnesssum += actionset.get(i).getFitness();
        }

        Random rn = new Random();
        double choicepoint = rn.nextDouble() * fitnesssum;
        fitnesssum = 0;
        for (int i = 0; i < actionset.size(); i++) {
            fitnesssum += actionset.get(i).getFitness();
            if (fitnesssum > choicepoint)
                return actionset.get(i);
        }
        return null;
    }

    public void applyCrossover(Classifier child1, Classifier child2) {
        Random rn = new Random();
        int x = (int) (rn.nextDouble() * child1.getGeneticArray().length);
        int y = (int) (rn.nextDouble() * child1.getGeneticArray().length);

        if (x > y) {
            int tmp = x;
            x = y;
            y = tmp;
        }
        int i = 0;
        do {
            if (x <= i && i < y) {
                int tmp = child1.getGeneticArray()[i];
                child1.setGeneticArray(i,child2.getGeneticArray()[i]);
                child2.setGeneticArray(i,tmp);
            }
            i++;
        } while (i < y);

    }

    public void applyMutation(Classifier cl) {
        int i = 0;
        do{
            Random rn = new Random();
            /**
             * TODO: Which probability to choose
             */
            int num1 = 350; //probability 0.35
            double random = rn.nextDouble() * 1000;
            random = Math.round(random);
            int num2 = (int) random;

            if ((num1 - num2) > 0) {
                if(cl.getGeneticArray()[i]==1)
                    cl.setGeneticArray(i,0);
                else
                    cl.setGeneticArray(i,1);
            }
        }while(i < cl.getGeneticArray().length);

        /**
         * TODO: Change action with a probability?
         */

    }

    public void insertPopulation(Classifier cl) {
        for(int i=0;i<Classifier.NUM_CONDITIONS;i++){
            if(Arrays.equals(classifier.get(i).getGeneticArray(),cl.getGeneticArray()) && classifier.get(i).getAction() == cl.getAction()){
                return;
            }
            Classifier.NUM_CONDITIONS++;
            classifier.add(cl);
        }
    }

    public void deletePopulation() {
        if(Classifier.NUM_CONDITIONS < 60)
            return;

        double votesum = 0;
        for(int i=0;i<Classifier.NUM_CONDITIONS;i++){
            votesum += deletionVote(classifier.get(i));
        }

        Random rn = new Random();
        double choicepoint = rn.nextDouble() * votesum;
        votesum = 0;
        for(int i=0;i<Classifier.NUM_CONDITIONS;i++){
            votesum += deletionVote(classifier.get(i));
            if((int)votesum >= (int)choicepoint) {
                if (Classifier.NUM_CONDITIONS > 1) {
                    Classifier.NUM_CONDITIONS--;
                } else {
                    classifier.remove(i);
                }
                return;
            }
        }
    }

    public double deletionVote(Classifier cl){
        double vote = Classifier.NUM_ACTIONS * Classifier.NUM_CONDITIONS;
        int fitnesssum = 0;
        for(int i=0;i< Classifier.NUM_CONDITIONS;i++)
            fitnesssum += classifier.get(i).getFitness();
        int averagefitness = fitnesssum / Classifier.NUM_CONDITIONS;
        /**
         * TODO: implement if statement
         */
        if(true)
            vote = vote * averagefitness / (cl.getFitness());
        return vote;
    }

    public boolean doGASubsumption() {
        /**
         * TODO: Implementation
         */
        return true;
    }

    public boolean doesSubsume(Classifier parent, Classifier child) {
        /**
         * TODO: Implementation
         */
        return true;
    }

}