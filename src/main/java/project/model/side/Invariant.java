package project.model.side;

import project.model.Values;

@FunctionalInterface
public interface Invariant {

    double I(Values values);
}
