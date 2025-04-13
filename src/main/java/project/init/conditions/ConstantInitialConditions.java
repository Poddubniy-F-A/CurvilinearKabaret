package project.init.conditions;

import project.model.Point;
import project.model.Values;
import project.model.Vector;

public class ConstantInitialConditions implements InitialConditions {
    private final double h0, u0, v0;

    public ConstantInitialConditions(double h, double u, double v) {
        h0 = h;
        u0 = u;
        v0 = v;
    }

    @Override
    public Values getValuesIn(Point point) {
        return new Values(h0, new Vector(u0, v0));
    }
}
