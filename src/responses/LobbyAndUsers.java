package responses;

import java.util.ArrayList;

public class LobbyAndUsers {
    private int lobbyId;
    private int maxPlayers;
    private ArrayList<String> users = new ArrayList<>();

    public int getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(int lobbyId) {
        this.lobbyId = lobbyId;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void addUser(String nickname) {
        users.add(nickname);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
