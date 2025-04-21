package project.model.side;

import project.extensions.Constants;
import project.model.Vector;
import project.model.side.extrapolation.ExtrapolationProperties;
import project.model.side.extrapolation.Extrapolator;
import project.model.node.Node;
import project.model.node.Point;
import project.model.node.Values;

import java.util.HashMap;

import static java.lang.Math.*;
import static project.extensions.Constants.G;
import static project.model.Vector.*;

public class Side {
    private final Node center;
    private final double length;

    private final HashMap<Node, Vector> normalByCell = new HashMap<>();

    private final ExtrapolationProperties prop1, prop2;
    private final Extrapolator e11, e12, e21, e22;

    public Side(Point point1, Point point2, Node center, Node node1, Node nodeC1, Node nodeC2, Node node2) {
        this.center = center;

        Vector vector = create(point1, point2);
        length = vector.length();

        Vector normal = vector.getNormalized().getNormal(), invertedNormal = normal.getInverted();
        normalByCell.put(nodeC1, areCoDirectional(normal, create(node1.getPoint(), getCenterPoint())) ? normal : invertedNormal);
        normalByCell.put(nodeC2, areCoDirectional(normal, create(node2.getPoint(), getCenterPoint())) ? normal : invertedNormal);

        prop1 = new ExtrapolationProperties(center, nodeC1, node1);
        prop2 = new ExtrapolationProperties(center, nodeC2, node2);

        e11 = new Extrapolator(prop1, true);
        e12 = new Extrapolator(prop1, false);
        e21 = new Extrapolator(prop2, true);
        e22 = new Extrapolator(prop2, false);
    }

    public Point getCenterPoint() {
        return center.getPoint();
    }

    public void updateValues(double tau) {
        boolean flag = areCoDirectional(center.getW(), getNormalFor(prop1.centralNode()));
        double
                I1 = e11.limitedExtrapolationBy(tau),
                I2 = e21.limitedExtrapolationBy(tau),
                I3 = flag ? e12.limitedExtrapolationBy(tau) : e22.limitedExtrapolationBy(tau),

                x1 = prop1.tangentialVector().x(),
                y1 = prop1.tangentialVector().y(),
                x2 = prop2.tangentialVector().x(),
                y2 = prop2.tangentialVector().y(),
                x3 = flag ? prop1.normalVector().x() : prop2.normalVector().x(),
                y3 = flag ? prop1.normalVector().y() : prop2.normalVector().y(),

                G1 = sqrt(Constants.G / prop1.centralNode().getH()),
                G2 = sqrt(Constants.G / prop2.centralNode().getH()),

                divider = G1 * (x2 * y3 - y2 * x3) + G2 * (y1 * x3 - x1 * y3),
                h = (I1 * (x2 * y3 - y2 * x3) + I2 * (y1 * x3 - x1 * y3) + I3 * (x1 * y2 - y1 * x2)) / divider,
                u = (-I1 * G2 * y3 + I2 * G1 * y3 + I3 * (G2 * y1 - G1 * y2)) / divider,
                v = (I1 * G2 * x3 - I2 * G1 * x3 + I3 * (G1 * x2 - G2 * x1)) / divider;

        setValues(new Values(h, new Vector(u, v)));

        /*if (getCenterPoint().x() == 0.52 && getCenterPoint().y() == 0.51) {
            System.out.println(center.getOldValues());
            System.out.println(center.getValues());
        }*/
    }

    public void setValues(Values values) {
        center.updateValues(values);
    }

    public void setAsNotUpdated() {
        center.setAsNotUpdated();
    }

    public double getIntegralH(Node node) {
        return length * getH() * scalarMultiply(getW(), getNormalFor(node));
    }

    public double getIntegralWx(Node node) {
        double h = getH();
        Vector w = getW(), normal = getNormalFor(node);

        return length * (h * w.x() * scalarMultiply(w, normal) + G * pow(h, 2) * normal.x() / 2);
    }

    public double getIntegralWy(Node node) {
        double h = getH();
        Vector w = getW(), normal = getNormalFor(node);

        return length * (h * w.y() * scalarMultiply(w, normal) + G * pow(h, 2) * normal.y() / 2);
    }

    private double getH() {
        return center.getH();
    }

    private Vector getW() {
        return center.getW();
    }

    private Vector getNormalFor(Node node) {
        return normalByCell.get(node);
    }

    public Node getCell1Center() {
        return prop1.centralNode();
    }

    public Node getCell2Center() {
        return prop2.centralNode();
    }
}
