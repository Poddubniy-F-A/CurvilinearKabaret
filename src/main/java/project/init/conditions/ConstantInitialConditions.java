package project.init.conditions;

import project.model.Point;
import project.model.Values;
import project.model.Vector;

public class ConstantInitialConditions implements InitialConditions {
    private final double h0;

    public ConstantInitialConditions(double h0) {
        this.h0 = h0;
    }

    @Override
    public Values getValuesIn(Point point) {
        return new Values(h0, new Vector(0, 0));
    }
}
