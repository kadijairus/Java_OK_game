package ee.taltech.okgame.rules;

import com.badlogic.gdx.math.Vector2;
import ee.taltech.okgame.GameClient;
import ee.taltech.okgame.Slot;
import ee.taltech.okgame.cards.Card;
import ee.taltech.okgame.GameStateManager;
import ee.taltech.okgame.SlotInfo;
import ee.taltech.okgame.packet.PacketCardSlotPosition;
import ee.taltech.okgame.packet.PacketSlotOccupancy;


public class RulesRoundOneB {
    public static boolean isDropValid(String areaName, float dropX, float dropY) {
        return "otherTeamArea".equals(areaName);
    }


    public static void performAction(Card card) {
        Vector2 screenSize = GameStateManager.getInstance().getCurrentScreenSize();
        Slot slot = GameStateManager.getInstance().getSlot("LeftSlot0");
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

    private static void moveCardToSlot(Card card, Slot slot) {
        if (slot != null && !slot.isOccupied()) {
            float newX = slot.getGlobalX() + (slot.getWidth() - card.getWidth()) / 2; // Center the card horizontally in the slot
            float newY = slot.getGlobalY() + (slot.getHeight() - card.getHeight()) / 2; // Center the card vertically in the slot
            card.setPosition(newX, newY);
            slot.setOccupied(true);
        }
    }
}
