package project.init;

import project.init.conditions.InitialConditions;
import project.model.Cell;
import project.model.Values;
import project.model.side.Side;

public class DefaultInitializer implements Initializer {
    private final InitialConditions initialConditions;

    public DefaultInitializer(InitialConditions initialConditions) {
        this.initialConditions = initialConditions;
    }

    @Override
    public Values getValuesIn(Side side) {
        return initialConditions.getValuesIn(side.getCenterPoint());
    }

    @Override
    public Values getValuesIn(Cell cell) {
        return initialConditions.getValuesIn(cell.getCenterPoint());
    }
}
