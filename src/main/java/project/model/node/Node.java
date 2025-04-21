package project.model.node;

import project.model.Vector;

public class Node {
    private final Point point;
    private Values oldValues, values;

    private boolean wasUpdated = false;

    public Node(Point point) {
        this.point = point;
    }

    public void updateValues(Values newValues) {
        oldValues = values;
        values = newValues;

        wasUpdated = true;
    }

    public void setAsNotUpdated() {
        wasUpdated = false;
    }

    public Point getPoint() {
        return point;
    }

    public Values getOldValues() {
        return oldValues;
    }

    public Values getNotUpdatedValues() {
        return wasUpdated ? oldValues : values;
    }

    public Values getValues() {
        return values;
    }

    public double getH() {
        return values.h();
    }

    public Vector getW() {
        return values.w();
    }
}
