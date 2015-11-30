package pacman.controllers.examples;

import pacman.game.Constants;
import pacman.game.Game;

import java.io.*;

/**
 * Created by Sam on 11/30/2015.
 */
public class NeuralNetwork {
    double threshold = 0.5;
    double learning_rate = 0.1;
    String delim = ",";
    String weightsFilePath, trainerFilePath;
    static String[] weights = new String[4];

    public NeuralNetwork() {
        this.weightsFilePath = "myData/weights.txt";
        this.trainerFilePath = "myData/training.txt";
    }

    public Constants.MOVE move(Game game){
        return null;
    }

    public double processCurrentState(Game game, Constants.MOVE move){
        StringBuilder sb = new StringBuilder();

        int steps = 5;

        sb.append("1.0,");
        sb.append(Perceptron.moveHasScore(game, move, steps));
        sb.append(Perceptron.moveIsCloserToTarget(game, move, steps));
        sb.append(Perceptron.moveHasDanger(game, move, steps));

        String currentState = sb.toString();
        String[] stateValues = currentState.split(delim);

        double sum = 0.0;
        for (int i = 0; i < stateValues.length; i++) {
            sum += Double.parseDouble(stateValues[i]) * Double.parseDouble(weights[i]);
        }
        return sum > threshold ? 1.0 : 0.0;
    }

    public void initWeights(boolean doTraining){
        if(doTraining){
            for(int i = 0; i < weights.length; i++){
                weights[i] = "0.0";
            }
        } else {
            String line;
            String[] data;
            try {
                BufferedReader weightsFile = getTrainerReader(weightsFilePath);
                while ((line = weightsFile.readLine()) != null) {
                    data = line.split(delim);
                    for( int i = 0; i < weights.length; i++){
                        weights[i] = data[i];
                    }
                }
                weightsFile.close();
            } catch(Exception ex) {
                System.out.println("Error reading file '" + trainerFilePath + "'");
                ex.printStackTrace();
            }
        }
    }

    private BufferedReader getTrainerReader(String trainerFilePath) {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(trainerFilePath);
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + trainerFilePath + "'");
            System.exit(1);
        }
        return new BufferedReader(fileReader);
    }

    public double dotProduct(String[] inputValues, String[] weights) {
        double sum = 0.0;
        for (int i = 0; i < inputValues.length - 1; i++) {
            sum += Double.parseDouble(inputValues[i]) * Double.parseDouble(weights[i]);
        }
        return sum;
    }

    public void trainNetwork() {
        int sentinel = 100000;
        while (sentinel > 0) {
            sentinel--;
            int errorCount = 0;
            String line;
            String[] data;
            try {
                BufferedReader trainerReader = getTrainerReader(trainerFilePath);
                while ((line = trainerReader.readLine()) != null) {
                    data = line.split(delim);
                    Double expectedResult = Double.parseDouble(data[data.length - 1]);
                    double result = dotProduct(data, weights) > threshold ? 1.0 : 0.0;
                    double error = expectedResult - result;
                    if (error != 0) {
                        errorCount++;
                        for (int i = 0; i < weights.length; i++) {
                            double currVal = Double.parseDouble(weights[i]);
                            double additionalChange = learning_rate * error * Double.parseDouble(data[i]);
                            String total = String.valueOf(currVal + additionalChange);
                            weights[i] = total;
                        }
                    }
                }
                trainerReader.close();
                if (errorCount == 0) {
                    break;
                }
            } catch (IOException ex) {
                System.out.println("Error reading file '" + trainerFilePath + "'");
                ex.printStackTrace();
                System.exit(1);
            }
        }

        BufferedWriter bw = null;

        try {
            File weightsFile = new File(weightsFilePath);
            bw = new BufferedWriter(new FileWriter(weightsFile));
            for( int i = 0; i < weights.length; i++ ){
                bw.write(weights[i]);
                if( i < weights.length - 1){
                    bw.write(',');
                }
            }
        } catch (Exception e) {
        } finally{
            try{
                bw.close();
            } catch (Exception e){

            }
        }
    }
}

class UpNetwork extends NeuralNetwork {
    Constants.MOVE upMove = Constants.MOVE.UP;

    public UpNetwork() {
        super();
    }

    @Override
    public Constants.MOVE move(Game game){
        if( super.processCurrentState(game, upMove) == 1.0){
            return upMove;
        }
        return null;
    }
}

class DownNetwork extends NeuralNetwork {
    Constants.MOVE downMove = Constants.MOVE.DOWN;

    public DownNetwork() {
        super();
    }

    @Override
    public Constants.MOVE move(Game game){
        if( super.processCurrentState(game, downMove ) == 1.0){
            return downMove;
        }
        return null;
    }
}

class LeftNetwork extends NeuralNetwork {
    Constants.MOVE leftMove = Constants.MOVE.LEFT;

    public LeftNetwork() {
        super();
    }

    @Override
    public Constants.MOVE move(Game game){
        if( super.processCurrentState(game, leftMove ) == 1.0){
            return leftMove;
        }
        return null;
    }
}

class RightNetwork extends NeuralNetwork {
    Constants.MOVE rightMove = Constants.MOVE.RIGHT;

    public RightNetwork() {
        super();
    }

    @Override
    public Constants.MOVE move(Game game){
        if( super.processCurrentState(game, rightMove ) == 1.0){
            return rightMove;
        }
        return null;
    }
}

class NeutralNetwork extends NeuralNetwork {
    Constants.MOVE neutralMove = Constants.MOVE.NEUTRAL;

    public NeutralNetwork() {
        super();
    }

    @Override
    public Constants.MOVE move(Game game){
        if( super.processCurrentState(game, neutralMove) == 1.0){
            return neutralMove;
        }
        return null;
    }
}