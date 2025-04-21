package project.init.borders;

import project.init.conditions.InitialConditions;
import project.model.node.Node;
import project.model.node.Values;

public class DefaultBordersHandler implements BordersHandler {

    private final InitialConditions initialConditions;

    public DefaultBordersHandler(InitialConditions initialConditions) {
        this.initialConditions = initialConditions;
    }

    @Override
    public void setValuesIn(Node node) {
        Values values = initialConditions.getValuesIn(node.getPoint());

        node.updateValues(values);
        node.updateValues(values);
    }
}
