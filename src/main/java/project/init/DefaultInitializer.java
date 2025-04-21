package project.init;

import project.init.conditions.InitialConditions;
import project.model.cell.Cell;
import project.model.side.Side;

public class DefaultInitializer implements Initializer {
    private final InitialConditions initialConditions;

    public DefaultInitializer(InitialConditions initialConditions) {
        this.initialConditions = initialConditions;
    }

    @Override
    public void initValuesIn(Cell cell) {
        cell.setValues(initialConditions.getValuesIn(cell.getCenterPoint()));
    }

    @Override
    public void initValuesIn(Side side) {
        side.setValues(initialConditions.getValuesIn(side.getCenterPoint()));
    }
}
