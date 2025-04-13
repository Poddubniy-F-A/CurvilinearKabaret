package project.init.borders;

import project.init.conditions.InitialConditions;
import project.model.Node;
import project.model.Values;

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
