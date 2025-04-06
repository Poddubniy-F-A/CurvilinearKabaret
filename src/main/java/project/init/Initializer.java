package project.init;

import project.model.Cell;
import project.model.Values;
import project.model.side.Side;

public interface Initializer {
    Values getValuesIn(Side side);
    Values getValuesIn(Cell cell);
}
