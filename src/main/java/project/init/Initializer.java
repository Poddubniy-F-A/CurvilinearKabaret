package project.init;

import project.model.Cell;
import project.model.Side;

public interface Initializer {

    void initValuesIn(Cell cell);
    void initValuesIn(Side side);
}
