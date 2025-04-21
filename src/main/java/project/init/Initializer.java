package project.init;

import project.model.cell.Cell;
import project.model.side.Side;

public interface Initializer {

    void initValuesIn(Cell cell);
    void initValuesIn(Side side);
}
