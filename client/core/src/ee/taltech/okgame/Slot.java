package ee.taltech.okgame;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Slot extends Table {
    private boolean occupied;
    private String slotName;
    private float globalX;
    private float globalY;

    public Slot(Drawable background, String name) {
        super();
        this.slotName = name;  // Correctly set the slotName
        this.setName(name);
        this.setBackground(background);
        this.occupied = false;
        setTouchable(Touchable.enabled);
        setBounds(getX(), getY(), getWidth(), getHeight());
    }
    public void updateGlobalPosition() {
        Vector2 position = localToStageCoordinates(new Vector2(0, 0));
        this.globalX = position.x;
        this.globalY = position.y;
    }

    public float getGlobalX() {
        return globalX;
    }

    public float getGlobalY() {
        return globalY;
    }

    public boolean isOccupied() {
        return occupied;
    }


    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public String getSlotName() {
        return slotName;
    }

    @Override
    public String toString() {
        return "Slot{" +
                "occupied=" + occupied +
                ", name='" + slotName + '\'' +
                '}';
    }
}

