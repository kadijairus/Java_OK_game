package ee.taltech.okgame.rules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import ee.taltech.okgame.GameClient;
import ee.taltech.okgame.Slot;
import ee.taltech.okgame.cards.Card;
import ee.taltech.okgame.GameStateManager;
import ee.taltech.okgame.SlotInfo;
import ee.taltech.okgame.packet.PacketCardSlotPosition;
import ee.taltech.okgame.packet.PacketSlotOccupancy;


public class RulesRoundOne {

    public static boolean isDropValid(String areaName, float dropX, float dropY) {
        return "myTeamArea".equals(areaName);
    }


    public static void performAction(Card card) {
        Vector2 screenSize = GameStateManager.getInstance().getCurrentScreenSize();
        Slot slot = GameStateManager.getInstance().getSlot("RightSlot0");
        float currentWidth = screenSize.x;
        float currentHeight = screenSize.y;

        // Convert relative coordinates back to absolute values based on current screen size

        float absoluteX = slot.getGlobalX();
        float absoluteY = slot.getGlobalY();
        Vector2 globalCoords = new Vector2(absoluteX, absoluteY);

        // If the card group is part of a parent group, convert coordinates
        if (card.getParent() != null) {
            card.setPositionToMatchSlot(slot);
            slot.setOccupied(true);
            GameClient.getInstance().sendTCP(new PacketSlotOccupancy(slot.getSlotName(), true));
            GameClient.getInstance().sendTCP(new PacketCardSlotPosition(card.getCardID(), slot.getSlotName()));
        } else {
            // If directly on the stage, set position directly
            card.setPositionToMatchSlot(slot);
            slot.setOccupied(true);
            GameClient.getInstance().sendTCP(new PacketSlotOccupancy(slot.getSlotName(), true));
            GameClient.getInstance().sendTCP(new PacketCardSlotPosition(card.getCardID(), slot.getSlotName()));
        }
    }

    public static void moveCardToSlot(Card card, Slot slot) {
        // Assuming slot coordinates (globalX, globalY) are already updated
        float x = slot.getGlobalX();
        float y = slot.getGlobalY();

        // Set card position to match slot's global coordinates
        Gdx.app.postRunnable(() -> {
            card.setPosition(x, y);
        });

        // Logging for debug
        System.out.println("Moved card to slot: " + slot.getSlotName() + " at position X: " + x + " Y: " + y);
    }
}
