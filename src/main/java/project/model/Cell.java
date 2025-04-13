package project.model;

import java.util.*;

import static project.extensions.Constants.G;

public class Cell {

    private final List<Point> points;

    private final Node center;
    private final double square, stepX, stepY;

    private final Set<Side> sides;

    public Cell(List<Point> points, Node center, Side bottom, Side right, Side top, Side left) {
        this.points = points;

        this.center = center;

        square = Point.getSquare(points);

        stepX = Vector.create(left.getCenterPoint(), right.getCenterPoint()).length();
        stepY = Vector.create(bottom.getCenterPoint(), top.getCenterPoint()).length();

        sides = new HashSet<>(Arrays.asList(left, right, bottom, top));
    }

    public void updateValues(double tau) {
        Vector w = center.getW();
        double h = center.getH(), u = w.x(), v = w.y(),
                newH = h - tau / square * sides.stream()
                        .mapToDouble(side -> side.getIntegralH(center))
                        .reduce(0, Double::sum),
                newU = (u * h - tau / square * sides.stream()
                        .mapToDouble(side -> side.getIntegralWx(center))
                        .reduce(0, Double::sum)
                ) / newH,
                newV = (v * h - tau / square * sides.stream()
                        .mapToDouble(side -> side.getIntegralWy(center))
                        .reduce(0, Double::sum)
                ) / newH;

        setValues(new Values(newH, new Vector(newU, newV)));

        /*if (getCenterPoint().x() == 0.51 && getCenterPoint().y() == 0.51) {
            System.out.println();
            System.out.println(center.getOldValues());
            System.out.println(center.getValues());
            System.out.println();
        }*/
    }

    public void setValues(Values values) {
        center.updateValues(values);
    }

    public double getOptimalTau() {
        return Math.min(
                stepX / (Math.sqrt(G * getH()) + Math.abs(getW().x())),
                stepY / (Math.sqrt(G * getH()) + Math.abs(getW().y()))
        );
    }

    public Point getCenterPoint() {
        return center.getPoint();
    }

    public double getH() {
        return center.getH();
    }

    public double getU() {
        return getW().x();
    }

    public double getV() {
        return getW().y();
    }

    private Vector getW() {
        return center.getW();
    }

    public List<Point> getPoints() {
        return points;
    }
}
