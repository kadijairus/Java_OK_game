package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.utils.random.ConstantIntegerDistribution;
import com.badlogic.gdx.ai.utils.random.IntegerDistribution;
import packet.PacketCardPositionInfo;

import java.util.*;
import java.util.logging.Logger;

public abstract class AI {
    private BehaviorTree<Blackboard> behaviorTree;
    private static final Logger logger = Logger.getLogger("AI");
    private Map<Integer, Integer> cardToPlayRoundMap = new HashMap<>();
    private Map<Integer, Coordinate> coordinateToPlayRoundMap = new HashMap<>();
    protected final Blackboard blackboard;
    private String role;

    /**
     * New AI instance is created by server.
     */
    public AI() {
        blackboard = new Blackboard(this);
        this.blackboard.putCardsFromJsonToBlackBoard();
        this.behaviorTree = createBehaviorTree(blackboard);
    }

    /**
     * Left or right player or decider
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Left or right player or decider
     */
    public String getRole() {
        return this.role;
    }

    /**
     * Give blackboard to server / Task to update
     */
    public Blackboard getAiBlackboard() {
        return this.blackboard;
    }

    public BehaviorTree<Blackboard> createBehaviorTree(Blackboard blackboard) {
        // Create behavior tree root node
        Sequence<Blackboard> rootSequence = new Sequence<>();
        // Create sequence for tasks to be executed sequentially
        Sequence<Blackboard> sequenceBefore = new Sequence<>();
        sequenceBefore.addChild(new GetSumPositivity());
        sequenceBefore.addChild(new GetAvailableSlots());

        // Create parallel node for tasks to be executed in parallel
        Parallel<Blackboard> optionTasks = new Parallel<>();
        //Sequence<Blackboard> optionTasks = new Sequence<>();
        optionTasks.addChild(new SumIfCardLeaf(0));
        optionTasks.addChild(new SumIfCardLeaf(1));
        optionTasks.addChild(new SumIfCardLeaf(2));

        // Create sequence to make decision based on parallel task results
        Sequence<Blackboard> decisionSequence = new Sequence<>();
        decisionSequence.addChild(new PlayCard());

        // Add nodes to root sequence
        rootSequence.addChild(sequenceBefore);
        rootSequence.addChild(optionTasks);
        rootSequence.addChild(decisionSequence);

        // Create behavior tree instance
        return new BehaviorTree<>(rootSequence, blackboard);
    }

    public void update() {
        behaviorTree.step();
    }

    /**
     * Set card to be played in current round
     */
    protected void setCardToPlay(int cardID) {
        int currentRound = blackboard.getCurrentRound();
        if (!cardToPlayRoundMap.containsKey(currentRound)) {
            cardToPlayRoundMap.put(currentRound, cardID);
        } else {
            // Log a message indicating that the card for the current round is already set
            logger.warning("::: [Player AI] Card for round " + currentRound + " is already set: " + cardToPlayRoundMap.get(currentRound));
        }
    }

    /**
     * Set card coordinates to be played in current round
     */
    protected void setCoordinateToPlay(Coordinate coordinate) {
        int currentRound = blackboard.getCurrentRound();
        if (!coordinateToPlayRoundMap.containsKey(currentRound)) {
            coordinateToPlayRoundMap.put(currentRound, coordinate);
            logger.info("::: [AI] Round " + currentRound + ": Coordinate to play is set: " + coordinate);
        } else {
            // Log a message indicating that the coordinate for the current round is already set
            logger.warning("::: [AI] Round " + currentRound + ": Coordinate for this round is already set.");
        }
    }

    public PacketCardPositionInfo getCardAndSlotToPlay() {
        PacketCardPositionInfo packetForServer = new PacketCardPositionInfo();
        Coordinate coordinateToPlay = coordinateToPlayRoundMap.get(blackboard.getCurrentRound());
        packetForServer.x = coordinateToPlay.getX();
        packetForServer.y = coordinateToPlay.getY();
        packetForServer.cardID = cardToPlayRoundMap.get(blackboard.getCurrentRound());
        return packetForServer;
    }



}
