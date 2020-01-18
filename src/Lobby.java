import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Lobby {

    /**
     * Contains user id and stream to write in game
     */
    public HashMap<Integer, ObjectOutputStream> playerStreams = new HashMap<>();

}
