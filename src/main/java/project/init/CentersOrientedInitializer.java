package project.init;

import project.init.conditions.InitialConditions;
import project.model.Cell;
import project.model.Side;
import project.model.Values;

public class CentersOrientedInitializer implements Initializer {

    private final InitialConditions conditions;

    public CentersOrientedInitializer(InitialConditions conditions) {
        this.conditions = conditions;
    }

    @Override
    public void initValuesIn(Cell cell) {
        cell.setValues(conditions.getValuesIn(cell.getCenterPoint()));
    }

    @Override
    public void initValuesIn(Side side) {
        side.setValues(Values.getAverage(
                conditions.getValuesIn(side.getCell1Center().getPoint()),
                conditions.getValuesIn(side.getCell2Center().getPoint())
        ));
    }
}
