package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import packet.PacketLeftPlayerWins;
import packet.PacketRightPlayerWins;

import java.io.IOException;
import java.util.logging.Logger;

import static ee.taltech.okgame.server.GameServer.LEFT_PLAYER;
import static ee.taltech.okgame.server.GameServer.RIGHT_PLAYER;

public class DecideWinner extends LeafTask<Blackboard> {

    @TaskAttribute(required = true)
    private static final Logger logger = Logger.getLogger("AI");
    @Override
    public Task.Status execute() {
        Blackboard blackboard = getObject();
        int round = blackboard.getCurrentRound();
        if (round == 6) {
            double left = blackboard.getSumOfLeftCards();
            double right = blackboard.getSumOfRightCards();
            if (left > right) {
                logger.info("::: [AI Winner task] Left player wins!");
                blackboard.setWinner(LEFT_PLAYER);
            } else {
                // If equal, also right player wins, because life is unfair
                logger.info("::: [AI Winner task] Right player wins!");
                blackboard.setWinner(RIGHT_PLAYER);
            }
            return Task.Status.SUCCEEDED;
        } else {
            logger.info("::: [AI Winner task] Not last row yet!");
            return Task.Status.FAILED;
        }
    }

    @Override
    protected Task<Blackboard> copyTo(Task<Blackboard> task) {
        return null;
    }
}
