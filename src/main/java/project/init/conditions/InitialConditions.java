package project.init.conditions;

import project.model.node.Point;
import project.model.node.Values;

public interface InitialConditions {

    Values getValuesIn(Point point);
}
