package project.model.side.extrapolation;

import project.model.node.Values;

@FunctionalInterface
public interface Invariant {

    double I(Values values);
}
