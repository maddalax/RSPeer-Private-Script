package org.maddev.web.dax;

import com.allatori.annotations.DoNotRename;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.rspeer.runetek.api.movement.position.Position;

@DoNotRename
public class Point3D {

    @DoNotRename
    private int x, y, z;

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public JsonElement toJson() {
        return new Gson().toJsonTree(this);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public Position toPosition() {
        return new Position(x, y, z);
    }

    public Point3D(Position position) {
        x = position.getX();
        y = position.getY();
        z = position.getFloorLevel();
    }

}
