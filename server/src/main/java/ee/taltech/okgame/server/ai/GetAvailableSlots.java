package ee.taltech.okgame.server.ai;

import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class GetAvailableSlots extends LeafTask<Blackboard> {

    @TaskAttribute(required = true)
    private static final Logger logger = Logger.getLogger("AI");
    private Blackboard blackboard;
    private AI playerAI;

    @Override
    public Task.Status execute() {
        try {
            // Access the blackboard object within the run() method
            blackboard = getObject();
            playerAI = blackboard.getAI();
            List<Coordinate> availableSlots = Coordinate.getCoordinatesByRoleAndRound(playerAI.getRole(), blackboard.getCurrentRound());
            List<Coordinate> slotsToChooseFrom = new ArrayList<>();
            for (Coordinate availableSlot : availableSlots) {
                if (!blackboard.getOccupiedSlots().contains(availableSlot.name())) {
                    slotsToChooseFrom.add(availableSlot);
                }
            }
            // For testing get random slot
            Random random = new Random();
            int randomIndex = random.nextInt(slotsToChooseFrom.size());
            Coordinate randomSlot = slotsToChooseFrom.get(randomIndex);
            playerAI.setCoordinateToPlay(randomSlot);
            return Task.Status.SUCCEEDED;
        } catch (Exception e) {
            logger.info("::: [AI Slots Task] failed: " + e);
            return Task.Status.FAILED;
        }
    }

    @Override
    protected Task<Blackboard> copyTo(Task<Blackboard> task) {
        return null;
    }
}
