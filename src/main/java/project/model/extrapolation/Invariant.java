package project.model.extrapolation;

import project.model.Values;

@FunctionalInterface
public interface Invariant {

    double I(Values values);
}
