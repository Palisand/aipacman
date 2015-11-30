package pacman.controllers.examples;

import pacman.Executor;
import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Game;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Created by Sam on 11/29/2015.
 */
public class Perceptron extends Controller<Constants.MOVE> {
    ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
    UpNetwork up = new UpNetwork();
    DownNetwork down = new DownNetwork();
    LeftNetwork left = new LeftNetwork();
    RightNetwork right = new RightNetwork();
    NeutralNetwork neutral = new NeutralNetwork();
    Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController;

    public Perceptron(boolean makeTrainingFile, boolean doTraining, Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController ){

        this.ghostController = ghostController;

        networks.add(up);
        networks.add(down);
        networks.add(left);
        networks.add(right);
        networks.add(neutral);

        for(NeuralNetwork network : networks){
            if(makeTrainingFile){
                network.makeTrainingFile();
            }
            network.initWeights(doTraining);
            if(doTraining){
                network.trainNetwork();
            }
        }
    }

    public Constants.MOVE getMove(Game game, long timeDue) {
        for(NeuralNetwork network : networks){
            Constants.MOVE move = network.move(game);
            System.out.println(move);
            if( move != null ){
                return move;
            }
        }
        return Constants.MOVE.NEUTRAL;
    }

    public static String possibleMovesString(Game game){
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        boolean down = false;
        boolean left = false;
        boolean right = false;
        Constants.MOVE[] moves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        for( Constants.MOVE move : moves ){
            if( move == Constants.MOVE.UP ){
                up = true;
            } else if (move == Constants.MOVE.DOWN) {
                down = true;
            } else if (move == Constants.MOVE.LEFT) {
                left = true;
            } else if (move == Constants.MOVE.RIGHT) {
                right = true;
            }
        }
        sb.append( up ? "1.0," : "0.0," );
        sb.append( down ? "1.0," : "0.0," );
        sb.append( left ? "1.0," : "0.0," );
        sb.append( right ? "1.0," : "0.0," );
        return sb.toString();
    }

    public static String dangerDirectionString(Game game,  Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController){
        boolean dangerUp = false;
        boolean dangerDown = false;
        boolean dangerLeft = false;
        boolean dangerRight = false;
        int i = 5;
        int lives = game.getPacmanNumberOfLivesRemaining();
        for(Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
            Game copy = game.copy();
            while( i > 0 ){
                copy.advanceGame(move, ghostController.getMove(copy, -1));
                i--;
                if (copy.getPacmanNumberOfLivesRemaining() < lives) {
                    if( move == Constants.MOVE.UP ){
                        dangerUp = true;
                    } else if (move == Constants.MOVE.DOWN) {
                        dangerDown = true;
                    } else if (move == Constants.MOVE.LEFT) {
                        dangerLeft = true;
                    } else if (move == Constants.MOVE.RIGHT) {
                        dangerRight = true;
                    }
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append( dangerUp ? "1.0," : "0.0," );
        sb.append( dangerDown ? "1.0," : "0.0," );
        sb.append( dangerLeft ? "1.0," : "0.0," );
        sb.append( dangerRight ? "1.0," : "0.0," );
        return sb.toString();
    }

    public static String scoreDirectionString(Game game,  Controller<EnumMap<Constants.GHOST, Constants.MOVE>> ghostController){
        boolean scoreUp = false;
        boolean scoreDown = false;
        boolean scoreLeft = false;
        boolean scoreRight = false;
        int i = 5;
        int lives = game.getPacmanNumberOfLivesRemaining();
        for(Constants.MOVE move : game.getPossibleMoves(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade())) {
            Game copy = game.copy();
            int maxScore = copy.getScore();
            while( i > 0 ){
                copy.advanceGame(move, ghostController.getMove(copy, -1));
                i--;
                if (copy.getScore() > maxScore) {
                    if( move == Constants.MOVE.UP ){
                        scoreUp = true;
                    } else if (move == Constants.MOVE.DOWN) {
                        scoreDown = true;
                    } else if (move == Constants.MOVE.LEFT) {
                        scoreLeft = true;
                    } else if (move == Constants.MOVE.RIGHT) {
                        scoreRight = true;
                    }
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append( scoreUp ? "1.0," : "0.0," );
        sb.append( scoreDown ? "1.0," : "0.0," );
        sb.append( scoreLeft ? "1.0," : "0.0," );
        sb.append( scoreRight ? "1.0" : "0.0" );
        return sb.toString();
    }

    class NeuralNetwork {
        double threshold = 0.5;
        double learning_rate = 0.01;
        double moveReplaceVal;
        double bias = 1.0;
        String delim = ",";
        String weightsFilePath, trainerFilePath, perceptronTrainingPath;
        String[] weights = new String[13];

        public NeuralNetwork(String weightsFilePath, String trainerFilePath, double moveReplaceVal) {
            this.weightsFilePath = weightsFilePath;
            this.trainerFilePath = trainerFilePath;
            perceptronTrainingPath = "myData/perceptronTrainer.txt";
            this.moveReplaceVal = moveReplaceVal;
        }

        public Constants.MOVE move(Game game){
            return null;
        }

        public double processCurrentState(Game game){
            StringBuilder sb = new StringBuilder();

            sb.append(possibleMovesString(game));
            sb.append(dangerDirectionString(game, ghostController));
            sb.append(scoreDirectionString(game, ghostController));

            String[] stateValues = sb.toString().split(delim);
            double sum = 0.0;
            for(int i = 0; i < stateValues.length; i++){
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

        public void makeTrainingFile(){
            String line;
            String[] data;
            try {
                BufferedReader perceptronTrainer = getTrainerReader(perceptronTrainingPath);
                File binaryTraining = new File(trainerFilePath);
                BufferedWriter bw = new BufferedWriter(new FileWriter(binaryTraining));
                while ((line = perceptronTrainer.readLine()) != null) {
                    data = line.split(delim);
                    data[data.length - 1] = Double.parseDouble(data[data.length - 1]) == moveReplaceVal ? "1.0" : "0.0";
                    StringBuilder sb = new StringBuilder();
                    sb.append("1.0,"); //bias
                    for(int i = 0; i < data.length; i++){
                        sb.append(data[i]);
                        if( i < data.length - 1){
                            sb.append(",");
                        }
                    }
                    sb.append('\n');
                    String dataStringified = sb.toString();
                    bw.write(dataStringified);
                }
                perceptronTrainer.close();
                bw.close();
            } catch(Exception ex) {
                System.out.println("Error reading file '" + trainerFilePath + "'");
                ex.printStackTrace();
                System.exit(1);
            }
        }

        public void trainNetwork() {
            int sentinel = 100;
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
                            for (int i = 0; i < weights.length - 1; i++) {
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
        public UpNetwork() {
            super("myData/upWeights.txt", "myData/upTraining.txt", 1.0);
        }

        @Override
        public Constants.MOVE move(Game game){
            if( super.processCurrentState(game) == 1.0){
                return Constants.MOVE.UP;
            }
            return null;
        }
    }

    class DownNetwork extends NeuralNetwork {
        public DownNetwork() {
            super("myData/downWeights.txt", "myData/downTraining.txt", 2.0);
        }

        @Override
        public Constants.MOVE move(Game game){
            if( super.processCurrentState(game ) == 1.0){
                return Constants.MOVE.DOWN;
            }
            return null;
        }
    }

    class LeftNetwork extends NeuralNetwork {
        public LeftNetwork() {
            super("myData/leftWeights.txt", "myData/leftTraining.txt", 3.0);
        }

        @Override
        public Constants.MOVE move(Game game){
            if( super.processCurrentState(game ) == 1.0){
                return Constants.MOVE.LEFT;
            }
            return null;
        }
    }

    class RightNetwork extends NeuralNetwork {
        public RightNetwork() {
            super("myData/rightWeights.txt", "myData/rightTraining.txt", 4.0);
        }

        @Override
        public Constants.MOVE move(Game game){
            if( super.processCurrentState(game ) == 1.0){
                return Constants.MOVE.RIGHT;
            }
            return null;
        }
    }

    class NeutralNetwork extends NeuralNetwork {
        public NeutralNetwork() {
            super("myData/neutralWeights.txt", "myData/neutralTraining.txt", 5.0);
        }

        @Override
        public Constants.MOVE move(Game game){
            if( super.processCurrentState(game) == 1.0){
                return Constants.MOVE.NEUTRAL;
            }
            return null;
        }
    }
}