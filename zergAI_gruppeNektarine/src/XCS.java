
import jnibwapi.JNIBWAPI;
import jnibwapi.model.Unit;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class XCS {
    private ArrayList<Classifier> classifier;
    private int action;
    private boolean[] check_action;

    private JNIBWAPI bwapi;
    private int previousTargetX;
    private int previousTargetY;


    public XCS(JNIBWAPI bwapi)  throws Exception{
        this.bwapi = bwapi;
        //reads parameter NUM_CLASSIFIERS from file
        if(Classifier.learnt == true) {
            String parameter = "";
            BufferedReader br = new BufferedReader(new FileReader("classifier/general.txt"));
            String line;
            if ((line = br.readLine()) != null)
                parameter = line;
            // Initialize with established start parameters
            Classifier.setNumClassifier(Integer.parseInt(parameter));
        }else{
            Classifier.NUM_CLASSIFIERS = 92;

        }

        // initialize classifier
        classifier = new ArrayList<>();
        for (int i = 0; i < Classifier.NUM_CLASSIFIERS; i++) {
            try {
                classifier.add(new Classifier(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void initializeEnvironment(Zerg unit, Unit target) throws Exception{

            //if environment matches classifiers it will set the condition of classifier to true
            for (int i = 0; i < classifier.size(); i++) {
                classifier.get(i).setCondition(unit, target, bwapi);
            }
            //generate matchset (all classifiers with condition = true)
            ArrayList<Classifier> matchset = generateMatchSet(classifier);

            //generate prediciton array
            double[] predictionarray = generatePredictionArray(matchset);

            //generate actionset
            ArrayList<Classifier> actionset = generateActionSet(predictionarray, matchset);

            //increases how often classifier is in Actionset
            for(int j = 0; j<actionset.size(); j++){
            	classifier.get(j).increaseCallCounter();
            }
            //run genetic algorithm
            /**
             * TODO: out comment if you don't want to use the genetic algorithm
             */
            runGeneticAlgorithm(actionset);

            //runs best action
            Classifier.selectAction(action, unit, target,bwapi);

            //updates parameters
            updateParameters(actionset, predictionarray);

            //writing the parameters from classifiers into files can be now found in VultureAI in method matchEnd
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

    //generates predictionarray
    private double[] generatePredictionArray(ArrayList<Classifier> matchset) {


        //boolean array so that only action which are in the actionset are checked
        check_action = new boolean[Classifier.NUM_ACTIONS];

        for (int i = 0; i < check_action.length; i++) {
            for (int j = 0; j < matchset.size(); j++) {
                if (matchset.get(j).getAction() == i) {
                    check_action[i] = true;
                }
            }
        }


        //generated precision * fitness and the fitnesssum
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

        //divides precision* fitness by fitnesssum
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

    //writes parameters of classifier[index] to parameter[index].txt file
    public void writeParameters(int index) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = new PrintWriter("classifier/parameters" + index + ".txt", "UTF-8");


        writer.println(classifier.get(index).getPrecision()); // 0
        writer.println(classifier.get(index).getError());        // 1
        writer.println(classifier.get(index).getFitness());    // 2
        writer.println(classifier.get(index).getReward());        // 3
        writer.println(classifier.get(index).getCallCounter()); // 4
        //writer.println(classifier[index].getGeneticArray());		// 5
        for (int j = 0; j < Classifier.NUM_CONDITIONS; j++) {
            writer.print(classifier.get(index).getGeneticArray()[j]);
        }
        


        writer.close();
    }

    public ArrayList<Classifier> getClassifier(){
        return classifier;
    }


    //runs the genetic algorithm
    public void runGeneticAlgorithm(ArrayList<Classifier> actionset) {

        boolean tempThreshold = false;

        //if classifier wasn't at least 100 times in actionset => don't run algorithm
        for(int i = 0; i < actionset.size(); i++){
        	if(actionset.get(i).getCallCounter() >= 100) tempThreshold = true;
        }
        
        if (tempThreshold != true) return;

        //selects the best parents
        Classifier parent1 = selectOffspring(actionset);
        Classifier parent2 = selectOffspring(actionset);

        if(parent1 == null || parent2 == null)
            return;

        //copy parameters from parents to children
        Classifier child[] = new Classifier[2];
        child[0] = parent1.copyOf();
        child[1] = parent2.copyOf();

        Random rn = new Random();

        int num1 = 500; //probability 0.5
        double random = rn.nextDouble() * 1000;
        random = Math.round(random);
        int num2 = (int) random;

        //with a propability of 50% run crossover and change parameters for child1 and child2
        if ((num1 - num2) > 0) {
            applyCrossover(child[0], child[1]);

            child[0].setPrecision((parent1.getPrecision() + parent2.getPrecision()) / 2);
            child[0].setError(0.25 * ((parent1.getError() + parent2.getError()) / 2));
            child[0].setFitness(0.1 * ((parent1.getFitness() + parent2.getFitness()) / 2));

            //precision
            if (child[0].getPrecision() >= 1000000000) {
                child[0].setPrecision(1000000000);
            }
            if (child[0].getPrecision() <= -1000000000) {
                child[0].setPrecision(-1000000000);
            }

            //error
            if (child[0].getError() >= 1000000000) {
                child[0].setError(1000000000);
            }
            if (child[0].getError() <= -1000000000) {
                child[0].setError(-1000000000);
            }

            //fitness
            if (child[0].getFitness() >= 1000000000) {
                child[0].setFitness(1000000000);
            }
            if (child[0].getFitness() <= -1000000000) {
                child[0].setFitness(-1000000000);
            }



            child[1].setPrecision(child[0].getPrecision());
            child[1].setError(child[0].getError());
            child[1].setFitness(child[0].getFitness());

            //precision
            if (child[1].getPrecision() >= 1000000000) {
                child[1].setPrecision(1000000000);
            }
            if (child[1].getPrecision() <= -1000000000) {
                child[1].setPrecision(-1000000000);
            }

            //error
            if (child[1].getError() >= 1000000000) {
                child[1].setError(1000000000);
            }
            if (child[1].getError() <= -1000000000) {
                child[1].setError(-1000000000);
            }

            //fitness
            if (child[1].getFitness() >= 1000000000) {
                child[1].setFitness(1000000000);
            }
            if (child[1].getFitness() <= -1000000000) {
                child[1].setFitness(-1000000000);
            }

            //for every child run Mutation and insert it into the population if some criterions are met
            // or delete some classifier if some criterions are met
            for (int i = 0; i < child.length; i++) {
                applyMutation(child[i]);
                insertPopulation(child[i]);
                deletePopulation();
            }
        }
    }

    //selects best parents
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

    //run 2-point crossover
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

    //runs mutation
    public void applyMutation(Classifier cl) {
        int i = 0;
        do{
            Random rn = new Random();

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
            i++;
        }while(i < cl.getGeneticArray().length);

    }

    //insert a classifier into population if some criterions are met
    public void insertPopulation(Classifier cl) {
        if(Classifier.NUM_CLASSIFIERS > 500) {
            return;
        }
        for(int i=0;i<Classifier.NUM_CLASSIFIERS;i++){
            if(Arrays.equals(classifier.get(i).getGeneticArray(),cl.getGeneticArray()) && classifier.get(i).getAction() == cl.getAction()){
                return;
            }
        }
        Classifier.NUM_CLASSIFIERS++;
        classifier.add(cl);
    }

    //delete a classifier from population if some criterions are met
    public void deletePopulation() {
        if(Classifier.NUM_CLASSIFIERS <= 200) {
            return;
        }
        double votesum = 0;
        for (int i = 0; i < Classifier.NUM_CLASSIFIERS; i++) {
            votesum += deletionVote(classifier.get(i));
        }

        Random rn = new Random();
        double choicepoint = rn.nextDouble() * votesum;
        votesum = 0;
        for (int i = 0; i < Classifier.NUM_CLASSIFIERS; i++) {
            votesum += deletionVote(classifier.get(i));
            if ((int) votesum >= (int) choicepoint) {
                Classifier.NUM_CLASSIFIERS--;
                classifier.remove(i);
                return;
            }
        }
    }

    public double deletionVote(Classifier cl){
        double vote = Classifier.NUM_ACTIONS * Classifier.NUM_CLASSIFIERS;
        int fitnesssum = 0;
        for(int i=0;i< Classifier.NUM_CLASSIFIERS;i++)
            fitnesssum += classifier.get(i).getFitness();
        int averagefitness = fitnesssum / Classifier.NUM_CLASSIFIERS;

        if(cl.getCallCounter() >= 20 && ((int) 1000 * (cl.getFitness() / Classifier.NUM_CLASSIFIERS)) < ((int) 1000* (0.8 * averagefitness)))
            vote = vote * averagefitness / (cl.getFitness());
        return vote;
    }

}