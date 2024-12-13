package ee.taltech.okgame.cards;

public interface CardInteractionListener {
    void onAreaNamesSet(String personalAreaName, String myTeamAreaName, String otherTeamAreaName);
}