package project;

import static java.lang.Math.*;
import static project.extensions.Constants.ALPHA;

public class GridsParameters {

    public static final int X_CELLS_COUNT = 50, Y_CELLS_COUNT = 50;
    public static final double
            LENGTH = 1., STEP_X = LENGTH / X_CELLS_COUNT,
            HEIGHT = 1., STEP_Y = HEIGHT / Y_CELLS_COUNT,
            ANGLE = PI / 2.25,
            TIME = 10; //40 * (2 * PI * 3 * sqrt(pow(STEP_X, 2) + pow(STEP_Y, 2))) / ALPHA;
}
