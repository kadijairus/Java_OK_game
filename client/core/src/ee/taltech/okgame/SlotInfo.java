package ee.taltech.okgame;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class SlotInfo {
    private Table slot;
    private float x, y, width, height;

    public SlotInfo(Table slot) {
        this.slot = slot;
        this.x = slot.getX();
        this.y = slot.getY();
        this.width = slot.getWidth();
        this.height = slot.getHeight();
    }

    // Getters
    public Table getSlot() {
        return slot;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    // Method to check if a point is within this slot
    public boolean contains(float dropX, float dropY) {
        return dropX >= x && dropX <= x + width && dropY >= y && dropY <= y + height;
    }
}