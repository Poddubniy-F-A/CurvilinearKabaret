package project.init.conditions;

import project.model.Point;
import project.model.Values;

@FunctionalInterface
public interface InitialConditions {
    Values getValuesIn(Point point);
}
