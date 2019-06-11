package org.maddev.web.dax;

import com.acuitybotting.common.utils.ExecutorUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.maddev.helpers.time.TimeHelper;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.path.PredefinedPath;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.maddev.helpers.log.Logger;
import org.maddev.helpers.log.Logger;

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
                    DaxWeb.cache.remove(cache.getKey());
                }
            }
        }, 0,60, TimeUnit.SECONDS);
    }

    public static PredefinedPath build(Position destination) {
        int tries = 0;
        return build(destination, tries);
    }

    private static PredefinedPath build(Position destination, int tries) {
        if(tries > 20) {
            Logger.fine("Failed to build path after 20 tries.");
            return null;
        }
        if(tries > 1) {
            Logger.fine("Dax Path Building Try: " + tries);
        }
        JsonObject pathRequest = new JsonObject();
        pathRequest.add("start",new Point3D(Players.getLocal().getPosition()).toJson());
        pathRequest.add("end", new Point3D(destination).toJson());
        pathRequest.add("player", new PlayerDetails().toJson());

        if(cache.containsKey(destination)) {
            return PredefinedPath.build(cache.get(destination).path);
        }
        try {
            HttpResponse<String> response = Unirest.post("https://api.dax.cloud/walker/generatePath")
                    .header("key", DaxWeb.key)
                    .header("secret", DaxWeb.secret)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(pathRequest.toString()).asString();

            DaxPathResult result = g.fromJson(response.getBody(), DaxPathResult.class);

            if(result.getPathStatus() == DaxPathStatus.BLOCKED) {
                int attempts = 0;
                boolean found = false;
                while (attempts < 1000) {
                    if(destination.isPositionWalkable()) {
                        found = true;
                        break;
                    }
                    if(Movement.getDebug().isToggled()) {
                        Logger.info("Attempting to find walkable tile: " + attempts);
                    }
                    destination = destination.randomize(5);
                    attempts++;
                }
                if(found) {
                    Logger.fine("Found walkable tile at: " + destination);
                }
                TimeHelper.sleep(100, 150);
                return build(destination, tries + 1);
            }

            if(result.getPathStatus() != DaxPathStatus.SUCCESS) {
                Logger.severe("Failed to generate path with Dax Web. " + result.getPathStatus() + " " + response.getBody());
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
            Logger.severe(e);
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
