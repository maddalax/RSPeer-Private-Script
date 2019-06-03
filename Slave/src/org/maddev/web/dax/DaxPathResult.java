package org.maddev.web.dax;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.rspeer.runetek.api.movement.position.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@DoNotRename
public class DaxPathResult {

    @DoNotRename
    private DaxPathStatus pathStatus;

    @DoNotRename
    private List<Point3D> path;

    @DoNotRename
    private int cost;

    private DaxPathResult() {

    }

    public DaxPathResult(DaxPathStatus pathStatus) {
        this.pathStatus = pathStatus;
    }

    public DaxPathResult(DaxPathStatus pathStatus, List<Point3D> path, int cost) {
        this.pathStatus = pathStatus;
        this.path = path;
        this.cost = cost;
    }

    public DaxPathStatus getPathStatus() {
        return pathStatus;
    }

    public void setPathStatus(DaxPathStatus pathStatus) {
        this.pathStatus = pathStatus;
    }

    public List<Point3D> getPath() {
        return path;
    }

    public void setPath(List<Point3D> path) {
        this.path = path;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public ArrayList<Position> toPositionPath() {
        if (getPath() == null) {
            return new ArrayList<>();
        }
        ArrayList<Position> path = new ArrayList<>();
        for (Point3D point3D : getPath()) {
            path.add(point3D.toPosition().getPosition());
        }
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DaxPathResult that = (DaxPathResult) o;
        return cost == that.cost &&
                pathStatus == that.pathStatus &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathStatus, path, cost);
    }

    public static DaxPathResult fromJson(JsonElement jsonObject) {
        return new Gson().fromJson(jsonObject, DaxPathResult.class);
    }

}
