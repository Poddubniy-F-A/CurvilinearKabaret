package project.init.conditions;

import project.model.Point;
import project.model.Values;
import project.model.Vector;

import static project.extensions.Constants.*;

public class VortexInitialConditions implements InitialConditions {
    private final double x0, y0, r0, h0;

    public VortexInitialConditions(double x0, double y0, double r0, double h0) {
        this.x0 = x0;
        this.y0 = y0;
        this.r0 = r0;
        this.h0 = h0;
    }

    @Override
    public Values getValuesIn(Point point) {
        return new Values(
                h0 - Math.pow(ALPHA, 2) * Math.exp(2 * BETA * (1 - Math.pow(r(point) / r0, 2))) / (4 * G * BETA),
                new Vector(
                        ALPHA * Math.exp(BETA * (1 - Math.pow(r(point) / r0, 2))) * (point.y() - y0) / r0,
                        -ALPHA * Math.exp(BETA * (1 - Math.pow(r(point) / r0, 2))) * (point.x() - x0) / r0
                )
        );
    }

    private double r(Point point) {
        return Math.sqrt(Math.pow(point.x() - x0, 2) + Math.pow(point.y() - y0, 2));
    }
}
