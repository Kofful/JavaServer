import responses.LobbyAndUsers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DBWorker {

    public static void setOnline(int userId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("update `users` set online = 1 where id = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static void setOffline(int userId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("update `users` set online = 0 where id = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static ArrayList<Friend> checkFriends(int userId) {
        ArrayList<Friend> friends = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select * from `users` where id in (select firstId from `friends` where secondId = "
                    + userId + ") or id in (select secondId from `friends` where firstId = " + userId + ")");
            while (result.next()) {
                Friend temp = new Friend();
                temp.setNickname(result.getString("nickname"));
                temp.setOnline(result.getBoolean("online"));
                friends.add(temp);
            }
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return friends;
    }

    public static ArrayList<Friend> getSubscribers(int userId) {
        ArrayList<Friend> strangers = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select nickname from `users` where id in (select subscriberId from `subscribers` where objectId = " + userId +")");
            while (result.next()) {
                Friend temp = new Friend();
                temp.setNickname(result.getString("nickname"));
                strangers.add(temp);
            }
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return strangers;
    }

    public static ArrayList<Friend> getStrangers(int userId, String query) {
        ArrayList<Friend> strangers = new ArrayList<>();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select nickname, online from `users` where LOCATE('" + query + "', nickname) = 1 and not id = " + userId + " and not id in (select secondId from `friends` where firstId = "+ userId + ") and not id in (select firstId from `friends` where secondId = " + userId + ") LIMIT 30");
            while (result.next()) {
                Friend temp = new Friend();
                temp.setNickname(result.getString("nickname"));
                temp.setOnline(result.getBoolean("online"));
                strangers.add(temp);
            }
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return strangers;
    }


    public static void sendFriendRequest(int userId, String name) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select * from `subscribers` where subscriberId = " + userId + " and objectId in (select id from `users` where nickname = '" + name +"')");
            if(!result.next())
                statement.execute("insert into `subscribers` values (" + userId + ", (select id from `users` where nickname = '" + name + "'))");
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void acceptFriend(int userId, String name) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select id from `users` where nickname = '" + name + "'");
            result.next();
            int friendId = result.getInt("id");
            statement.execute("insert into `friends` values (" + userId + ", " + friendId + ")");
            statement.execute("delete from `subscribers`" +
                    " where subscriberId = " + friendId +
                    " and objectId = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void denyFriend(int userId, String name) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("delete from `subscribers`" +
                    " where subscriberId in (select id from `users` where nickname = '" + name + "')" +
                    " and objectId = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static void removeFriend(int userId, String name) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("delete from `friends` where" +
                    " (firstId in (select id from `users`" +
                    " where nickname = '" + name + "') and secondId = " + userId + ") " +
                    "or (secondId in (select id from `users` where nickname = '" + name + "') and firstId = " + userId + ")");
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static int createLobby(int userId, int players, boolean friendsOnly) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("insert into `lobbies` (creatorId, players, friendsonly) value ("
                    + userId + ", "
                    + players +", "
                    + friendsOnly + ")");
            ResultSet result = statement.executeQuery("select id from `lobbies` where creatorId = " + userId);
            result.next();
            int lobbyId = result.getInt("id");
            statement.execute("insert into `players_in_lobbies` (userId, lobbyId) value ("
                    + userId + ", "
                    + lobbyId + ")");
            con.close();
            return lobbyId;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return 0;
    }

    public static LobbyAndUsers findLobby(int userId) {
        LobbyAndUsers data = new LobbyAndUsers();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select id, players from `lobbies` where players > (select COUNT(*) from `players_in_lobbies` where lobbyId = lobbies.id) and friendsonly = 0");
            if(result.next()) {
                data.setLobbyId(result.getInt("id"));
                data.setMaxPlayers(result.getInt("players"));
                result = statement.executeQuery("select users.avatar, users.nickname from `players_in_lobbies`" +
                        " join `users` on id = userId  where lobbyId = " + data.getLobbyId());
                while(result.next()) {
                    data.addAvatar(result.getInt("avatar"));
                    data.addUser(result.getString("nickname"));
                }
                statement.execute("insert into `players_in_lobbies` (userId, lobbyId) value ("
                        + userId + ", "
                        + data.getLobbyId() + ")");
            }
            con.close();
        } catch (Exception ex) {
            data.setLobbyId(0);
            System.out.println(ex.getMessage());
        }
        return data;
    }

    public static void changeAvatar(int userId, int avatarId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("update `users` set avatar = " + avatarId +  " where id = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static int getAvatar(int userId) {
        int avatarId = 0;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            ResultSet result = statement.executeQuery("select avatar from `users` where id = " + userId);
            result.next();
            avatarId = result.getInt("avatar");
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return avatarId;
    }

    public static void removeUserFromLobby(int userId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("delete from `players_in_lobbies` where userId = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void removeLobby(int lobbyId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("delete from `lobbies` where id = " + lobbyId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void removeLobbyIfExists(int userId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/munchkindb?useUnicode=true&serverTimezone=UTC", "root", "");
            Statement statement = con.createStatement();
            statement.execute("delete from `lobbies` where creatorId = " + userId);
            statement.execute("delete from `players_in_lobbies` where userId = " + userId);
            con.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
