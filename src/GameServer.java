import responses.LobbyAndUsers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameServer {

    private ServerSocket serverSocket;
    private int port = 8080;

    public static ArrayList<ObjectInputStream> inputConnections = new ArrayList<>();
    public static ArrayList<ObjectOutputStream> outputConnections = new ArrayList<>();//TODO delete if not needed

    static HashMap<Integer, Lobby> lobbys = new HashMap<>();

    public GameServer() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception ex) {

        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.handleConnections();
    }

    private void handleConnections() {
        System.out.println("Waiting for client message...");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(300000);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                inputConnections.add(ois);
                outputConnections.add(oos);
                new ConnectionHandler(oos, ois);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

class ConnectionHandler implements Runnable {
    static final int GET_FRIENDS = 1001;
    static final int GET_SUBSCRIBERS = 1002;
    static final int GET_STRANGERS = 1003;
    static final int SEND_FRIEND_REQUEST = 1004;
    static final int ACCEPT_FRIEND = 1005;
    static final int DENY_FRIEND = 1006;
    static final int REMOVE_FRIEND = 1007;
    static final int CREATE_LOBBY = 1008;
    static final int FIND_LOBBY = 1009;
    static final int GET_FRIEND_LOBBY = 1010;
    static final int ADD_USER_IN_LOBBY = 1011;
    static final int REMOVE_USER_FROM_LOBBY = 1012;
    static final int START_GAME = 1013;
    static final int CHANGE_AVATAR = 1014;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    ConnectionHandler(ObjectOutputStream oos, ObjectInputStream ois) {
        this.oos = oos;
        this.ois = ois;
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        int userId = 0;
        try {
            userId = ois.readInt();
            DBWorker.setOnline(userId);
            System.out.println("socket added");
            while (true) {
                int code = ois.readInt();
                System.out.println(code);
                switch (code) {
                    case GET_FRIENDS: {
                        ArrayList<Friend> friends = DBWorker.checkFriends(userId);
                        oos.writeInt(friends.size());
                        for (Friend friend : friends) {
                            oos.writeObject(friend.getNickname());
                            oos.writeBoolean(friend.isOnline());
                        }
                        oos.flush();
                        break;
                    }
                    case GET_SUBSCRIBERS: {
                        ArrayList<Friend> subscribers = DBWorker.getSubscribers(userId);
                        oos.writeInt(subscribers.size());
                        for (Friend friend : subscribers) {
                            oos.writeObject(friend.getNickname());
                        }
                        oos.flush();
                        break;
                    }
                    case GET_STRANGERS: {
                        String query = (String) ois.readObject();
                        ArrayList<Friend> strangers = DBWorker.getStrangers(userId, query);
                        oos.writeInt(strangers.size());
                        for (Friend friend : strangers) {
                            oos.writeObject(friend.getNickname());
                            oos.writeBoolean(friend.isOnline());
                        }
                        oos.flush();
                        break;
                    }
                    case SEND_FRIEND_REQUEST: {
                        String nickname = (String) ois.readObject();
                        DBWorker.sendFriendRequest(userId, nickname);
                        break;
                    }
                    case ACCEPT_FRIEND: {
                        String nickname = (String) ois.readObject();
                        DBWorker.acceptFriend(userId, nickname);
                        break;
                    }
                    case DENY_FRIEND: {
                        String nickname = (String) ois.readObject();
                        DBWorker.denyFriend(userId, nickname);
                        break;
                    }
                    case REMOVE_FRIEND: {
                        String nickname = (String) ois.readObject();
                        DBWorker.removeFriend(userId, nickname);
                        break;
                    }
                    case CREATE_LOBBY: {
                        int players = ois.readInt();
                        boolean friendsOnly = ois.readBoolean();
                        int lobbyId = DBWorker.createLobby(userId, players, friendsOnly);
                        Lobby lobby = new Lobby();
                        lobby.playerStreams.put(userId, oos);
                        GameServer.lobbys.put(lobbyId, lobby);
                        oos.writeInt(lobbyId);
                        oos.flush();
                        break;
                    }
                    case FIND_LOBBY: {
                        String nickname = (String) ois.readObject();
                        LobbyAndUsers result = DBWorker.findLobby(userId);
                        oos.writeInt(result.getLobbyId());
                        if (result.getLobbyId() != 0) {
                            for (Map.Entry<Integer, ObjectOutputStream> entry :
                                    GameServer.lobbys.get(result.getLobbyId()).playerStreams.entrySet()) {
                                try {
                                    ObjectOutputStream out = entry.getValue();
                                    out.writeInt(userId);
                                    out.writeObject(nickname);
                                    out.flush();
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }
                            oos.writeInt(result.getMaxPlayers());
                            oos.writeInt(result.getUsers().size());
                            for (String player : result.getUsers()) {
                                oos.writeObject(player);
                            }
                            GameServer.lobbys.get(result.getLobbyId()).playerStreams.put(userId, oos);
                            break;
                        }
                        oos.flush();
                        break;
                    }

                    case GET_FRIEND_LOBBY: {
                        break;
                    }

                    case REMOVE_USER_FROM_LOBBY: {
                        break;
                    }

                    case START_GAME: {
                        break;
                    }

                    case CHANGE_AVATAR: {
                        int avatarId = ois.readInt();
                        DBWorker.changeAvatar(userId,  avatarId);
                        break;
                    }


                }
            }
        } catch (Exception ex) {
            System.out.println("socket removed");
            System.out.println(ex.getMessage());
            DBWorker.setOffline(userId);
            DBWorker.removeLobbyIfExists(userId);
            GameServer.outputConnections.remove(oos);
            GameServer.inputConnections.remove(ois);
        }
    }
}
