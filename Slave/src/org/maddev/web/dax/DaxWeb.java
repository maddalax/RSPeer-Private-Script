package org.maddev.web.dax;

import com.acuitybotting.common.utils.ExecutorUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.rspeer.runetek.api.movement.path.PredefinedPath;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DaxWeb {

    public static final Gson g = new Gson();
    private static String key;
    private static String secret;
    private static HashMap<Position, PathCache> cache = new HashMap<>();
    private static ScheduledExecutorService cachePool = ExecutorUtil.newScheduledExecutorPool(1);

    public static void initialize(String key, String secret) {
        DaxWeb.key = key;
        DaxWeb.secret = secret;
        cachePool.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            for (Map.Entry<Position, PathCache> cache : cache.entrySet()) {
                if ((now - cache.getValue().lastUpdate) > TimeUnit.SECONDS.toMillis(90)) {
                    Log.fine("Removing path form cache after 90 seconds.");
                    DaxWeb.cache.remove(cache.getKey());
                }
            }
        }, 0,60, TimeUnit.SECONDS);
    }

    public static PredefinedPath build(Position destination) {
        JsonObject pathRequest = new JsonObject();
        pathRequest.add("start",new Point3D(Players.getLocal().getPosition()).toJson());
        pathRequest.add("end", new Point3D(destination).toJson());
        pathRequest.add("player", new PlayerDetails().toJson());
        String json = g.toJson(pathRequest);

        System.out.println(json);

        if(cache.containsKey(destination)) {
            System.out.println("Getting path from cache.");
            Log.fine("Getting path from cache.");
            return PredefinedPath.build(cache.get(destination).path);
        }

        try {
            HttpResponse<String> response = Unirest.post("https://api.dax.cloud/walker/generatePath")
                    .header("key", DaxWeb.key)
                    .header("secret", DaxWeb.secret)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(pathRequest.toString()).asString();

            System.out.println(response.getBody());
            DaxPathResult result = g.fromJson(response.getBody(), DaxPathResult.class);

            if(result.getPathStatus() != DaxPathStatus.SUCCESS) {
                Log.severe("Failed to generate path with Dax Web. " + result.getPathStatus() + " " + response.getBody());
                return null;
            }

            List<Position> path = result.toPositionPath();
            path.add(destination);
            Position[] arr = path.toArray(new Position[0]);
            PredefinedPath predefined = PredefinedPath.build(arr);
            cache.put(destination, new PathCache(arr));
            return predefined;

        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class PathCache {

        private Position[] path;
        private long lastUpdate;

        PathCache(Position[] path) {
            this.path = path;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
}
