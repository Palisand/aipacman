package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

import java.util.*;
import java.io.*;

public class kNearestNeighbor extends Controller<MOVE> {

    Controller<EnumMap<GHOST, MOVE>> ghostController;
    int k = 5;
    String delim = ",";
    String trainerFile = "myData/kNN_trainer.txt";
    int distIgnore = 999999;

    public kNearestNeighbor(Controller<EnumMap<GHOST, MOVE>> ghostController){
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue) {
        return kNNMove(game);
    }

    public MOVE kNNMove(Game game) {
        String line;
        String[] data;
        List<Neighbor> neighbors = new ArrayList<Neighbor>();
        try {
            BufferedReader trainerReader = getTrainerReader();
            while ((line = trainerReader.readLine()) != null) {
                data = line.split(delim);
                MOVE currMove = strtom(data[6]);
                int currDist = (int) getDistance(game, data);

                if (neighbors.size() < k) {
                    neighbors.add(new Neighbor(currDist, currMove));
                }
                else if (currDist < Collections.max(neighbors, new NeighborComp()).distance) {
                    neighbors.remove(Collections.max(neighbors, new NeighborComp()));
                    neighbors.add(new Neighbor(currDist, currMove));
                }
            }
            trainerReader.close();
        }
        catch (IOException ex) {
            System.out.println("Error reading file '" + trainerFile + "'");
            ex.printStackTrace();
            System.exit(1);
        }

        return Collections.min(neighbors, new NeighborComp()).move;
    }

    private double getDistance(Game game, String[] attrData) {
        if (strtom(attrData[0]) != game.getPacmanLastMoveMade()) {
            return distIgnore;
        }

        int shortestPathDistanceToNonEdibleGhost = game.getNearestGhostDistance(false);
        double ghostNonEdibleDistance = Math.pow(shortestPathDistanceToNonEdibleGhost - Integer.parseInt(attrData[1]), 2);
        int shortestPathDistanceToEdibleGhost = game.getNearestGhostDistance(true);
        double ghostEdibleDistance = Math.pow(shortestPathDistanceToEdibleGhost - Integer.parseInt(attrData[2]), 2);
        int shortestPathDistanceToPill = game.getNearestPillDistance();
        double pillDistance = Math.pow(shortestPathDistanceToPill - Integer.parseInt(attrData[3]), 2);
        int shortestPathDistanceToPowerPill = game.getNearestPowerPillDistance();
        double powerPillDistance = Math.pow(shortestPathDistanceToPowerPill - Integer.parseInt(attrData[4]), 2);
        int shortestPathDistanceToNode = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), Integer.parseInt(attrData[5]));
        double nodeDistance = Math.pow(shortestPathDistanceToNode, 2);

        return Math.sqrt(ghostNonEdibleDistance + ghostEdibleDistance + pillDistance + powerPillDistance + nodeDistance);
    }

    private BufferedReader getTrainerReader() {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(trainerFile);
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + trainerFile + "'");
            System.exit(1);
        }
        return new BufferedReader(fileReader);
    }

    private MOVE strtom(String move) {
        MOVE m;
        switch(move) {
            case "UP":
                m = MOVE.UP;
                break;
            case "DOWN":
                m = MOVE.DOWN;
                break;
            case "LEFT":
                m = MOVE.LEFT;
                break;
            case "RIGHT":
                m = MOVE.RIGHT;
                break;
            case "NEUTRAL":
                m = MOVE.NEUTRAL;
                break;
            default:
                throw new IllegalArgumentException("Invalid MOVE: " + move);
        }
        return m;
    }

    private class Neighbor {
        public Integer distance;
        public MOVE move;
        Neighbor(int d, MOVE m) {
            distance = d;
            move = m;
        }
        public String toString() {
            return distance + ":" + move;
        }
    }

    private class NeighborComp implements Comparator<Neighbor> {
        public int compare(Neighbor n1, Neighbor n2) {
            return n1.distance.compareTo(n2.distance);
        }
    }
}
