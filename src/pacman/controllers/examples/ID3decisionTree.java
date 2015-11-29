package pacman.controllers.examples;

import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.DM;
import pacman.game.Game;

import java.util.*;
import java.io.*;

public class ID3decisionTree extends Controller<MOVE>{
    String examplesFileName = "myData/id3_examples.txt";
    String delim = ",";
//    ID3node treeTextBook = new ID3node();
    ID3node upTree = new ID3node();
    ID3node downTree = new ID3node();
    ID3node leftTree = new ID3node();
    ID3node rightTree = new ID3node();
    ID3node neutralTree = new ID3node();
    Controller<EnumMap<GHOST, MOVE>> ghostController;
    List<String> attributes = Arrays.asList(
//            "alternate", "bar", "friday", "hungry", "patrons", "price", "rain", "reservation", "type", "estimate"
            "at_junction", "move_to_nearest_pill", "move_to_nearest_power_pill", "move_to_nearest_ghost", "move_to_nearest_edible_ghost", "is_ghost_edible"
    );
    Map<String,List<String>> attrToValues = new HashMap<String,List<String>>();

    List<List<String>> examples = new ArrayList<List<String>>();
    int currentGoalIndex = 6;  // 10 for textbook, start at 6 for actual project

    public ID3decisionTree(Controller<EnumMap<GHOST, MOVE>> ghostController) {
//        attrToValues.put("alternate", Arrays.asList("yes", "no"));
//        attrToValues.put("bar", Arrays.asList("yes", "no"));
//        attrToValues.put("friday", Arrays.asList("yes", "no"));
//        attrToValues.put("hungry", Arrays.asList("yes", "no"));
//        attrToValues.put("patrons", Arrays.asList("some", "full", "none"));
//        attrToValues.put("price", Arrays.asList("$","$$","$$$"));
//        attrToValues.put("rain", Arrays.asList("yes", "no"));
//        attrToValues.put("reservation", Arrays.asList("yes", "no"));
//        attrToValues.put("type", Arrays.asList("french", "thai", "burger", "italian"));
//        attrToValues.put("estimate", Arrays.asList("0-10", "10-30", "30-60", ">60"));
        attrToValues.put("at_junction", Arrays.asList("yes", "no"));
        attrToValues.put("move_to_nearest_pill", Arrays.asList("UP", "DOWN", "LEFT", "RIGHT", "none"));
        attrToValues.put("move_to_nearest_power_pill", Arrays.asList("UP", "DOWN", "LEFT", "RIGHT", "none"));
        attrToValues.put("move_to_nearest_ghost", Arrays.asList("UP", "DOWN", "LEFT", "RIGHT", "none"));
        attrToValues.put("move_to_nearest_edible_ghost", Arrays.asList("UP", "DOWN", "LEFT", "RIGHT", "none"));
        attrToValues.put("is_ghost_edible", Arrays.asList("yes", "no"));
        buildTrees();
        this.ghostController = ghostController;
    }

    public MOVE getMove(Game game, long timeDue) {
        return id3Move(game);
    }

    public void buildTrees() {
        List<List<String>> examples = getExamplesFromFile();
//        treeTextBook = decisionTreeLearning(examples, attributes, null);
//        treeTextBook.print();
//        System.exit(0);
        upTree = decisionTreeLearning(examples, attributes, null);  // upTree.print();
        currentGoalIndex++;
        downTree = decisionTreeLearning(examples, attributes, null);  // downTree.print();
        currentGoalIndex++;
        leftTree = decisionTreeLearning(examples, attributes, null);  // leftTree.print();
        currentGoalIndex++;
        rightTree = decisionTreeLearning(examples, attributes, null);  // rightTree.print();
        currentGoalIndex++;
        neutralTree = decisionTreeLearning(examples, attributes, null);  // neutralTree.print();
    }

    public List<List<String>> getExamplesFromFile() {
        String line;
        List<List<String>> examples = new ArrayList<List<String>>();
        FileReader fileReader;
        try {
            fileReader = new FileReader(examplesFileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                examples.add(Arrays.asList(line.split(delim)));
            }
            fileReader.close();
            bufferedReader.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + examplesFileName + "'");
            System.exit(1);
        }
        catch (IOException ex) {
            System.out.println("Error reading file '" + examplesFileName + "'");
            ex.printStackTrace();
            System.exit(1);
        }
        return examples;
    }

    public ID3node decisionTreeLearning(List<List<String>> examples, List<String> attributes, List<List<String>> parentExamples) {
        if (examples.isEmpty()) {
            return pluralityValue(parentExamples);
        }
        else if (sameClassification(examples)) {
            return new ID3node(examples.get(0).get(currentGoalIndex));  // since all same, just use first
        }
        else if (attributes.isEmpty()) {
            return pluralityValue(examples);
        }
        else {
            String bestAttribute = getBestAttribute(attributes, examples);  // importance
            ID3node tree = new ID3node(bestAttribute);
            for (String label : attrToValues.get(bestAttribute)) {
                List<String> remainingAttributes = new ArrayList<String>(attributes);
                remainingAttributes.remove(bestAttribute);
                List<List<String>> exs = getExamplesByAttrValue(bestAttribute, label, examples);
                ID3node subtree = decisionTreeLearning(exs, remainingAttributes, examples);
                subtree.label = label;
                tree.children.add(subtree);
            }
            return tree;
        }
    }

    public List<List<String>> getExamplesByAttrValue(String attribute, String label, List<List<String>> examples) {
        List<List<String>> exs = new ArrayList<List<String>>();
        int index = attributes.indexOf(attribute);
        for (List<String> example : examples) {
            if (example.get(index).equals(label)) {
                exs.add(example);
            }
        }
        return exs;
    }

    public String getBestAttribute(List<String> attributes, List<List<String>> examples) {
        int p = 0;  // positive examples
        int n = 0;  // negative examples
        for (List<String> example : examples) {
            if (example.get(currentGoalIndex).equals("yes")) { ++p; }
            else if (example.get(currentGoalIndex).equals("no")) { ++n; }
        }
        double b = boolRandVarEntropy((double) p / (p + n));
        double remainder = 0;
        double gain;
        int p_k = 0, n_k = 0;
        String bestAttribute = "";
        double bestGain = -1;
        for (int i = 0; i < attributes.size(); i++) {
            for(String value : attrToValues.get(attributes.get(i))) {
                for (List<String> example : examples) {
                    if (example.get(i).equals(value)) {
                        if (example.get(currentGoalIndex).equals("yes")) { ++p_k; }
                        else if (example.get(currentGoalIndex).equals("no")) { ++n_k; }
                    }
                }
                double addToRemainder = ((double) (p_k + n_k) / (p + n)) * boolRandVarEntropy((double) p_k / (p_k + n_k));
                if (!Double.isNaN(addToRemainder)) {
                    remainder += addToRemainder;
                }
                p_k = 0;
                n_k = 0;
            }
            gain = b - remainder;
            if (gain > bestGain) {
                bestAttribute = attributes.get(i);
                bestGain = gain;
            }
            remainder = 0;
        }
        return bestAttribute;
    }

    static public double boolRandVarEntropy(double q) {
        return -((q * (Math.log(q)/Math.log(2))) + ((1 - q) * (Math.log(1 - q)/Math.log(2))));
    }

    public boolean sameClassification(List<List<String>> examples) {
        String classification = examples.get(0).get(currentGoalIndex);
        for (List<String> example : examples) {
            if (!example.get(currentGoalIndex).equals(classification)) {
                return false;
            }
        }
        return true;
    }

    public ID3node pluralityValue(List<List<String>> examples) {
        int yesCount = 0;
        int noCount = 0;
        for (List<String> example : examples) {
            if (example.get(currentGoalIndex).equals("yes")) {
                yesCount++;
            }
            if (example.get(currentGoalIndex).equals("no")) {
                noCount++;
            }
        }
        if (yesCount > noCount) {
            return new ID3node("yes");
        }
        else if (noCount < yesCount) {
            return new ID3node("no");
        }
        else {
            return (new Random().nextInt(2) == 0) ? new ID3node("yes") : new ID3node("no");
        }
    }

    public MOVE id3Move(Game game) {
        MOVE moves[] = {MOVE.UP, MOVE.DOWN, MOVE.LEFT, MOVE.RIGHT, MOVE.NEUTRAL};
        boolean willMove[] = {false, false, false, false, false};
        MOVE chosenMove = MOVE.NEUTRAL;
        MOVE[] possibleMoves = game.getPossibleMoves(game.getPacmanCurrentNodeIndex());
        for(MOVE move : possibleMoves) {
            switch(move) {
                case UP:
                    willMove[0] = getGoalOutput(upTree, game, MOVE.UP);
                    break;
                case DOWN:
                    willMove[1] = getGoalOutput(downTree, game, MOVE.DOWN);
                    break;
                case LEFT:
                    willMove[2] = getGoalOutput(leftTree, game, MOVE.LEFT);
                    break;
                case RIGHT:
                    willMove[3] = getGoalOutput(rightTree, game, MOVE.RIGHT);
                    break;
                case NEUTRAL:
                    willMove[4] = getGoalOutput(neutralTree, game, MOVE.NEUTRAL);
                    break;
            }
        }
        for (int i = 0; i < moves.length; i++) {
            if (willMove[i]) {
                chosenMove = moves[i];
                break;
            }
        }
        return chosenMove;
    }

    public boolean getGoalOutput(ID3node tree, Game game, MOVE move) {
        Game copy = game.copy();
        game.advanceGame(move, ghostController.getMove(copy, -1));
        ID3node currNode = tree;
        while (!currNode.children.isEmpty()) {
            switch(currNode.value) {
                case "at_junction":
                    String atJunction = copy.isJunction(game.getPacmanCurrentNodeIndex()) ? "yes" : "no";
                    currNode = getMatchingChild(atJunction, currNode);
                    break;
                case "move_to_nearest_pill":
                    String moveToNearestPill = copy.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getNearestPillDistance(), DM.PATH).toString();
                    currNode = getMatchingChild(moveToNearestPill, currNode);
                    break;
                case "move_to_nearest_power_pill":
                    String moveToNearestPowerPill = copy.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getNearestPowerPillDistance(), DM.PATH).toString();
                    currNode = getMatchingChild(moveToNearestPowerPill, currNode);
                    break;
                case "move_to_nearest_ghost":
                    String moveToNearestGhost;
                    try {
                        moveToNearestGhost = copy.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getNearestGhostDistance(false), DM.PATH).toString();
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        moveToNearestGhost = "none";
                    }
                    currNode = getMatchingChild(moveToNearestGhost, currNode);
                    break;
                case "move_to_nearest_edible_ghost":
                    String moveToNearestEdibleGhost;
                    try {
                        moveToNearestEdibleGhost = copy.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getNearestGhostDistance(true), DM.PATH).toString();
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        moveToNearestEdibleGhost = "none";
                    }
                    currNode = getMatchingChild(moveToNearestEdibleGhost, currNode);
                    break;
                case "is_ghost_edible":
                    String isGhostEdible = "no";
                    for (GHOST ghost : Arrays.asList(GHOST.BLINKY, GHOST.INKY, GHOST.PINKY, GHOST.SUE)) {
                        if (game.isGhostEdible(ghost)) {
                            isGhostEdible = "yes";
                            break;
                        }
                    }
                    currNode = getMatchingChild(isGhostEdible, currNode);
                    break;
                default:
                    break;
            }
        }
        return currNode.value.equals("yes");
    }

    public ID3node getMatchingChild(String label, ID3node parent) {
        for (ID3node child : parent.children) {
            if (child.label.equals(label)) {
                return child;
            }
        }
        return null;
    }

    static public class ID3node {
        
        String label = null;
        String value = null;
        ID3node parent = null;
        List<ID3node> children = new ArrayList<ID3node>();

        public ID3node() {}

        public ID3node(String value) {
            this.value = value;
        }

        public void print() {
            print("", true);
        }

        private void print(String prefix, boolean isTail) {
            System.out.println(prefix + (isTail ? "└── " : "├── ") + label + " : " + value);
            for (int i = 0; i < children.size() - 1; i++) {
                children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
            }
            if (children.size() > 0) {
                children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
            }
        }

    }
}

