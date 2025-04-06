package project.model.side;

import project.Constants;
import project.model.Initializable;
import project.init.Initializer;
import project.model.*;
import project.model.Cell;

import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Math.*;
import static project.Constants.G;
import static project.model.Vector.*;

public class Side implements Initializable {
    private final Point point1, point2;//
    private final Node center;

    private final boolean isHorizontal;//

    private final double length;

    private final Vector tangentialVector;//
    private final HashMap<Cell, Vector> normalByCell = new HashMap<>();

    private InvariantProperties prop1, prop2;

    private boolean wasUpdated = false;

    public Side(Point point1, Point point2, boolean isHorizontal) {
        this.point1 = point1;
        this.point2 = point2;

        center = new Node(Geometry.getCenter(Arrays.asList(point1, point2)));

        this.isHorizontal = isHorizontal;

        Vector vector = create(point1, point2);
        length = vector.length();
        tangentialVector = vector.getNormalized();
    }

    public void addCell(Cell cell) {
        Vector midline = create(cell.getOppositeSide(this).getCenterPoint(), getCenterPoint());

        Vector normal = tangentialVector.getNormal();
        normalByCell.put(cell, areCoDirectional(normal, midline) ? normal : normal.getInverse());

        InvariantProperties properties = new InvariantProperties(cell, this);
        if (areCoDirectional(midline, isHorizontal ? new Vector(0, 1) : new Vector(1, 0))) {
            prop1 = properties;
        } else {
            prop2 = properties;
        }
    }

    public Point getCenterPoint() {
        return center.getPoint();
    }

    @Override
    public void init(Initializer initializer) {
        center.updateValues(initializer.getValuesIn(this));
    }

    public void updateValues(double tau) {
        boolean flag = areCoDirectional(center.getW(), getNormalFor(prop1.cell()));
        double
                I1 = limitedExtrapolationBy(prop1, prop1.mainInvariant(), tau),
                I2 = limitedExtrapolationBy(prop2, prop2.mainInvariant(), tau),
                I3 = flag ?
                        limitedExtrapolationBy(prop1, prop1.optionalInvariant(), tau) :
                        limitedExtrapolationBy(prop2, prop2.optionalInvariant(), tau),

                x1 = prop1.tangentialVector().x(),
                y1 = prop1.tangentialVector().y(),
                x2 = prop2.tangentialVector().x(),
                y2 = prop2.tangentialVector().y(),
                x3 = flag ? prop1.normalVector().x() : prop2.normalVector().x(),
                y3 = flag ? prop1.normalVector().y() : prop2.normalVector().y(),

                G1 = sqrt(Constants.G / prop1.cell().getH()),
                G2 = sqrt(Constants.G / prop2.cell().getH()),

                divider = G1 * (x2 * y3 - y2 * x3) + G2 * (y1 * x3 - x1 * y3),
                h = (I1 * (x2 * y3 - y2 * x3) + I2 * (y1 * x3 - x1 * y3) + I3 * (x1 * y2 - y1 * x2)) / divider,
                u = (-I1 * G2 * y3 + I2 * G1 * y3 + I3 * (G2 * y1 - G1 * y2)) / divider,
                v = (I1 * G2 * x3 - I2 * G1 * x3 + I3 * (G1 * x2 - G2 * x1)) / divider;

        /*if (Double.isNaN(u) || Double.isNaN(v)) {
            System.out.println(x1 + " " + y1 + " " + x2 + " " + y2 + " " + G1 + " " + G2);
            System.out.println(I1 + " " + I2);
            System.out.println(isHorizontal + " " + flag);
            System.out.println(I3);
            System.out.println(prop1);
            System.out.println(prop2);

            System.out.println(h + " " + u + " " + v);
        }*/
        //if (u != getW().x() || v != getW().y() || h != getH()) System.out.println("?");
        center.updateValues(new Values(h, new Vector(u, v)));

        wasUpdated = true;
    }

    public void setAsNotUpdated() {
        wasUpdated = false;
    }

    protected double limitedExtrapolationBy(InvariantProperties properties, Invariant invariant, double tau) {
        Cell cell = properties.cell();
        Values
                oppositeSideOldValues = properties.oppositeSide().getValues(),
                oldValuesC = cell.getOldValues(),
                valuesC = cell.getValues(),
                curValues = center.getValues();

        double add = 2 * (invariant.I(valuesC) - invariant.I(oldValuesC)) +
                invariant.I(valuesC) * (invariant.I(curValues) - invariant.I(oppositeSideOldValues)) * tau / properties.step(),
                limiter1 = invariant.I(oppositeSideOldValues),
                limiterC = invariant.I(oldValuesC),
                limiter2 = invariant.I(curValues),
                extrapolation = 2 * invariant.I(valuesC) - invariant.I(oppositeSideOldValues);

        return min(
                max(limiter1, max(limiterC, limiter2)) + add,
                max(
                        min(limiter1, min(limiterC, limiter2)) + add,
                        extrapolation
                )
        );
    }

    private Values getValues() {
        return wasUpdated ? center.getOldValues() : center.getValues();
    }

    public Point getPoint1() {
        return point1;
    }

    public Point getPoint2() {
        return point2;
    }

    public double getIntegralH(Cell cell) {
        return length * getH() * scalarMultiply(getW(), getNormalFor(cell));
    }

    public double getIntegralWx(Cell cell) {
        double h = getH();
        Vector w = getW(), normal = getNormalFor(cell);

        return length * (h * w.x() * scalarMultiply(w, normal) + G * pow(h, 2) * normal.x() / 2);
    }

    public double getIntegralWy(Cell cell) {
        double h = getH();
        Vector w = getW(), normal = getNormalFor(cell);

        return length * (h * w.y() * scalarMultiply(w, normal) + G * pow(h, 2) * normal.y() / 2);
    }

    private double getH() {
        return center.getH();
    }

    private Vector getW() {
        return center.getW();
    }

    private Vector getNormalFor(Cell cell) {
        return normalByCell.get(cell);
    }
}
