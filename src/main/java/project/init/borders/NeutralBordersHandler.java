package project.init.borders;

import project.model.Node;
import project.model.Values;
import project.model.Vector;

public class NeutralBordersHandler implements BordersHandler {

    private final Values values;

    public NeutralBordersHandler(double h) {
        values = new Values(h, new Vector(0, 0));
    }

    @Override
    public void setValuesIn(Node node) {
        node.updateValues(values);
        node.updateValues(values);
    }
}
