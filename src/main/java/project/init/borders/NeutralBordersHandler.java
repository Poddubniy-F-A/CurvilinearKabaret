package project.init.borders;

import project.init.Initializer;
import project.model.Cell;
import project.model.Values;
import project.model.Vector;
import project.model.side.Side;

public class NeutralBordersHandler implements Initializer {

    private final Values values;

    public NeutralBordersHandler(double h) {
        values = new Values(h, new Vector(0, 0));
    }

    @Override
    public Values getValuesIn(Cell cell) {
        return values;
    }

    @Override
    public Values getValuesIn(Side side) {
        return values;
    }
}
