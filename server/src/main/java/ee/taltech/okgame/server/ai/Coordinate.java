package ee.taltech.okgame.server.ai;

import java.util.ArrayList;
import java.util.List;

import static ee.taltech.okgame.server.GameServer.DECIDER;
import static ee.taltech.okgame.server.GameServer.LEFT_PLAYER;
import static ee.taltech.okgame.server.GameServer.RIGHT_PLAYER;

public enum Coordinate {

    // First three rounds left player
    LeftSlot0(100, 825),
    LeftSlot2(100, 598),
    RightSlot4(1060, 372),

    // First three rounds right player
    RightSlot0(1060, 825),
    RightSlot2(1060, 598),
    LeftSlot4(100, 372),

    // Modifier slots
    LeftSlot1(559, 825),
    LeftSlot3(559, 598),
    LeftSlot5(559, 372),
    RightSlot1(1519, 825),
    RightSlot3(1519, 598),
    RightSlot5(1519, 372);

    private final int x;
    private final int y;

    Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getName() {
        return x;
    }

    public static List<Coordinate> getCoordinatesByRoleAndRound(String role, int round) {
        List<Coordinate> emptyResultlist = new ArrayList<>();
        if (role.equals(DECIDER)) {
            return emptyResultlist;
        }
        if (round == 1 ) {
            if (role.equals(LEFT_PLAYER)) {
                return List.of(LeftSlot0);
            }
            if (role.equals(RIGHT_PLAYER)) {
                return List.of(RightSlot0);
            }
        }
        if (round == 2) {
            if (role.equals(LEFT_PLAYER)) {
                return List.of(LeftSlot2);
            }
            if (role.equals(RIGHT_PLAYER)) {
                return List.of(RightSlot2);
            }
        }
        if (round == 3) {
            if (role.equals(LEFT_PLAYER)) {
                return List.of(RightSlot4);
            }
            if (role.equals(RIGHT_PLAYER)) {
                return List.of(LeftSlot4);
            }
        }
        // Spot for negative card
        if (round == 4 && role.equals(RIGHT_PLAYER)) {
            return List.of(LeftSlot1, LeftSlot3, LeftSlot5);
        }
        if (round == 4 && role.equals(LEFT_PLAYER)) {
            return List.of(RightSlot1, RightSlot3, RightSlot5);
        }
        // Spot for positive card
        if (round == 5 && role.equals(LEFT_PLAYER)) {
            return List.of(LeftSlot1, LeftSlot3, LeftSlot5);
        }
        if (round == 5 && role.equals(RIGHT_PLAYER)) {
            return List.of(RightSlot1, RightSlot3, RightSlot5);
        }
        return emptyResultlist;
    }

    }
