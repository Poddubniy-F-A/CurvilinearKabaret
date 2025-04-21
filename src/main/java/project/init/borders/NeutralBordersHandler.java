package project.init.borders;

import project.model.node.Node;
import project.model.node.Values;
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
