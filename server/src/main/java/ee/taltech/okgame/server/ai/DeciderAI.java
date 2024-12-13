package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.branch.Sequence;

public class DeciderAI extends AI {

    @Override
    public BehaviorTree<Blackboard> createBehaviorTree(Blackboard blackboard) {
        // Create behavior tree root node
        Sequence<Blackboard> rootSequence = new Sequence<>();
        // Create sequence for tasks to be executed sequentially
        Sequence<Blackboard> sequenceBefore = new Sequence<>();
        sequenceBefore.addChild(new GetSumPositivity());

        // Create sequence to make decision based on parallel task results
        Sequence<Blackboard> decisionSequence = new Sequence<>();
        decisionSequence.addChild(new DecideWinner());

        // Add nodes to root sequence
        rootSequence.addChild(sequenceBefore);
        rootSequence.addChild(decisionSequence);

        // Create behavior tree instance
        return new BehaviorTree<>(rootSequence, blackboard);
    }

    @Override
    public String toString() {
        return "AI decider";
    }

}
