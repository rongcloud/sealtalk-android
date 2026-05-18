package cn.rongcloud.im.openclaw.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class OpenClawRobotRegistry {
    private static final String BOT_ID_PREFIX = "Claw_";
    private static final String DEFAULT_PORTRAIT_URI =
            "https://static.rongcloud.cn/avatar/claw.png";
    private static final String PREF_NAME = "openclaw_robot_tokens";
    private static final String PREF_KEY_ROBOTS = "robots";
    private static final Map<String, OpenClawRobotInfo> ROBOTS = new ConcurrentHashMap<>();
    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

    private OpenClawRobotRegistry() {}

    public static boolean isOpenClawRobotId(String targetId) {
        return !TextUtils.isEmpty(targetId)
                && (targetId.startsWith(BOT_ID_PREFIX) || ROBOTS.containsKey(targetId));
    }

    public static boolean shouldUseDefaultPortrait(String portraitUri) {
        return TextUtils.isEmpty(portraitUri) || DEFAULT_PORTRAIT_URI.equals(portraitUri);
    }

    public static void register(OpenClawRobotInfo robot) {
        if (robot == null || TextUtils.isEmpty(robot.getBotId())) {
            return;
        }
        ROBOTS.put(robot.getBotId(), robot);
        if (robot instanceof OpenClawRobotTokenResult) {
            registerToken(robot.getBotId(), ((OpenClawRobotTokenResult) robot).getToken());
        }
    }

    public static void register(Context context, OpenClawRobotInfo robot) {
        register(robot);
        persistRobots(context);
    }

    public static void registerToken(String botId, String token) {
        if (TextUtils.isEmpty(botId) || TextUtils.isEmpty(token)) {
            return;
        }
        TOKENS.put(botId, token);
    }

    public static void registerToken(Context context, String botId, String token) {
        registerToken(botId, token);
        if (context == null || TextUtils.isEmpty(botId) || TextUtils.isEmpty(token)) {
            return;
        }
        getTokenPreferences(context).edit().putString(botId, token).apply();
    }

    public static String getToken(String botId) {
        return TextUtils.isEmpty(botId) ? null : TOKENS.get(botId);
    }

    public static String getToken(Context context, String botId) {
        String token = getToken(botId);
        if (!TextUtils.isEmpty(token) || context == null || TextUtils.isEmpty(botId)) {
            return token;
        }
        token = getTokenPreferences(context).getString(botId, null);
        if (!TextUtils.isEmpty(token)) {
            TOKENS.put(botId, token);
        }
        return token;
    }

    private static SharedPreferences getTokenPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void registerAll(List<OpenClawRobotInfo> robots) {
        if (robots == null) {
            return;
        }
        for (OpenClawRobotInfo robot : robots) {
            register(robot);
        }
    }

    public static void registerAll(Context context, List<OpenClawRobotInfo> robots) {
        registerAll(robots);
        persistRobots(context);
    }

    public static List<OpenClawRobotInfo> getRegisteredRobots(Context context) {
        loadRobots(context);
        return new ArrayList<>(ROBOTS.values());
    }

    private static void persistRobots(Context context) {
        if (context == null || ROBOTS.isEmpty()) {
            return;
        }
        JSONArray array = new JSONArray();
        for (OpenClawRobotInfo robot : ROBOTS.values()) {
            if (robot == null || TextUtils.isEmpty(robot.getBotId())) {
                continue;
            }
            JSONObject object = new JSONObject();
            try {
                object.put("botId", robot.getBotId());
                object.put("name", robot.getName());
                object.put("portraitUri", robot.getPortraitUri());
                array.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getTokenPreferences(context).edit().putString(PREF_KEY_ROBOTS, array.toString()).apply();
    }

    private static void loadRobots(Context context) {
        if (context == null || !ROBOTS.isEmpty()) {
            return;
        }
        String robotsJson = getTokenPreferences(context).getString(PREF_KEY_ROBOTS, null);
        if (TextUtils.isEmpty(robotsJson)) {
            return;
        }
        try {
            JSONArray array = new JSONArray(robotsJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                OpenClawRobotInfo robot = new OpenClawRobotInfo();
                robot.setBotId(object.optString("botId"));
                robot.setName(object.optString("name"));
                robot.setPortraitUri(object.optString("portraitUri"));
                register(robot);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static OpenClawRobotInfo get(String botId) {
        return TextUtils.isEmpty(botId) ? null : ROBOTS.get(botId);
    }

    public static OpenClawRobotInfo getOrCreate(String botId, String name) {
        OpenClawRobotInfo robot = get(botId);
        if (robot != null) {
            return robot;
        }
        if (!isOpenClawRobotId(botId)) {
            return null;
        }
        robot = new OpenClawRobotInfo();
        robot.setBotId(botId);
        robot.setName(TextUtils.isEmpty(name) ? "OpenClaw Robot" : name);
        register(robot);
        return robot;
    }
}
