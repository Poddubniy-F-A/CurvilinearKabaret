package project.model;

import project.model.node.Point;

public record Vector(double x, double y, double length) {

    public static Vector create(Point start, Point end) {
        return new Vector(end.x() - start.x(), end.y() - start.y());
    }

    public static Vector getAverage(Vector v1, Vector v2) {
        return new Vector((v1.x() + v2.x()) / 2, (v1.y() + v2.y()) / 2);
    }

    public static boolean areCoDirectional(Vector v1, Vector v2) {
        return scalarMultiply(v1, v2) > 0;
    }

    public static double scalarMultiply(Vector v1, Vector v2) {
        return v1.x() * v2.x() + v1.y() * v2.y();
    }

    public Vector(double x, double y) {
        this(x, y, Math.sqrt(x * x + y * y));
    }

    public Vector getNormal() {
        return new Vector(-y, x);
    }

    public Vector getNormalized() {
        return new Vector(x / length, y / length);
    }

    public Vector getInverted() {
        return new Vector(-x, -y);
    }
}
