package project.model;

import project.init.Initializer;
import project.model.side.Side;

import java.util.*;

import static project.Constants.G;

public class Cell implements Initializable {

    private final double stepX, stepY;

    private final Node center;
    private final double square;

    private final HashMap<Side, Side> oppositeSides = new HashMap<>();

    public Cell(Side[] sides) {
        stepX = Vector.create(sides[1].getCenterPoint(), sides[3].getCenterPoint()).length();
        stepY = Vector.create(sides[0].getCenterPoint(), sides[2].getCenterPoint()).length();

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < sides.length; i++) {
            Side side = sides[i];

            points.add(side.getPoint1());
            points.add(side.getPoint2());

            oppositeSides.put(side, sides[(i + 2) % sides.length]);
        }

        center = new Node(Geometry.getCenter(points));
        square = Geometry.getSquare(points);

        for (Side side : sides) {
            side.addCell(this);
        }
    }

    @Override
    public void init(Initializer initializer) {
        center.updateValues(initializer.getValuesIn(this));
    }

    public void updateValues(double tau) {
        Vector w = center.getW();
        double h = center.getH(), u = w.x(), v = w.y(),
                newH = h - tau / square * oppositeSides.keySet().stream()
                        .mapToDouble(side -> side.getIntegralH(this))
                        .reduce(0, Double::sum),
                newU = (u * h - tau / square * oppositeSides.keySet().stream()
                        .mapToDouble(side -> side.getIntegralWx(this))
                        .reduce(0, Double::sum)
                ) / newH,
                newV = (v * h - tau / square * oppositeSides.keySet().stream()
                        .mapToDouble(side -> side.getIntegralWy(this))
                        .reduce(0, Double::sum)
                ) / newH;

        //if (newH != h || newU != u || newV != v) System.out.println("!");
        center.updateValues(new Values(newH, new Vector(newU, newV)));
    }

    public Side getOppositeSide(Side side) {
        return oppositeSides.get(side);
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

    public Values getOldValues() {
        return center.getOldValues();
    }

    public Values getValues() {
        return center.getValues();
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
}
