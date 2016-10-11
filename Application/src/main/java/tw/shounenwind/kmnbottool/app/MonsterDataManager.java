package tw.shounenwind.kmnbottool.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MonsterDataManager {
    private static MonsterDataManager instance;
    private JSONObject player;
    private JSONArray monsters;

    private MonsterDataManager() {
        player = new JSONObject();
        monsters = new JSONArray();
    }

    public static MonsterDataManager getInstance() {
        if (instance == null) {
            synchronized (MonsterDataManager.class) {
                if (instance == null) {
                    instance = new MonsterDataManager();
                }
            }
        }
        return instance;
    }

    public void parse(String json) throws JSONException {
        player = (new JSONObject(json)).getJSONObject("玩家");
        monsters = (new JSONObject(json)).getJSONArray("寵物");
    }

    public JSONObject getPlayer() {
        return player;
    }

    public JSONArray getMonsters() {
        return monsters;
    }
}
