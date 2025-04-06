package project.model;

public class Node {
    private final Point point;
    private Values oldValues, values;

    public Node(Point point) {
        this.point = point;
    }

    public void updateValues(Values newValues) {
        oldValues = values;
        values = newValues;
    }

    public Point getPoint() {
        return point;
    }

    public Values getOldValues() {
        return oldValues;
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
