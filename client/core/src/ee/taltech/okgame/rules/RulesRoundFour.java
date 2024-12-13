package ee.taltech.okgame.rules;

import ee.taltech.okgame.GameClient;
import ee.taltech.okgame.Slot;
import ee.taltech.okgame.GameStateManager;
import ee.taltech.okgame.cards.Card;
import ee.taltech.okgame.packet.PacketSlotOccupancy;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class RulesRoundFour {
    private static final Set<String> validSlots = new HashSet<>();
    static {
        validSlots.add("LeftSlot1");
        validSlots.add("LeftSlot3");
        validSlots.add("LeftSlot5");
        validSlots.add("RightSlot1");
        validSlots.add("RightSlot3");
        validSlots.add("RightSlot5");
    }

    public static Slot determineSlot(float dropX, float dropY) {
        for (Slot slot : GameStateManager.getInstance().getAllSlots().values()) {
            slot.updateGlobalPosition();
            if (dropX >= slot.getGlobalX() && dropX <= slot.getGlobalX() + slot.getWidth() &&
                    dropY >= slot.getGlobalY() && dropY <= slot.getGlobalY() + slot.getHeight()) {
                return slot;
            }
        }
        return null;
    }

    public static void checkDropValidity(Slot slot, Consumer<Boolean> callback) {
        if (slot != null && validSlots.contains(slot.getSlotName()) && !slot.isOccupied()) {
            GameClient.getInstance().requestSlotOccupancy(slot.getSlotName(), isServerOccupied -> {
                if (!isServerOccupied) {
                    callback.accept(true);
                    GameStateManager.getInstance().setSlotOccupancy(slot.getSlotName(), true); // Optimistically update local state
                } else {
                    callback.accept(false);
                    GameStateManager.getInstance().setSlotOccupancy(slot.getSlotName(), true); // Sync with server state
                }
            });
        } else {
            callback.accept(false);
        }
    }

    public static void performAction(Card card, Slot slot) {
        card.setPositionToMatchSlot(slot);
        slot.setOccupied(true);
        GameClient.getInstance().sendTCP(new PacketSlotOccupancy(slot.getSlotName(), true));
    }
}
