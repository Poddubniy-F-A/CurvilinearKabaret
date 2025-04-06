package project;

import project.init.borders.NeutralBordersHandler;
import project.init.DefaultInitializer;
import project.init.Initializer;
import project.init.conditions.VortexInitialConditions;
import project.model.Cell;
import project.model.Point;
import project.model.side.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static project.Constants.CFL;

public class Main {
    private static final int X_CELLS_COUNT = 100, Y_CELLS_COUNT = 100;
    private static final double
            TIME = 1.0, MIN_TAU = 0.1,
            LENGTH = 1, HEIGHT = 1,
            STEP_X = LENGTH / X_CELLS_COUNT, STEP_Y = HEIGHT / Y_CELLS_COUNT,
            H0 = 1, R0 = 3 * Math.sqrt(Math.pow(STEP_X, 2) + Math.pow(STEP_Y, 2));

    private static final Initializer
            initializer = new DefaultInitializer(new VortexInitialConditions(LENGTH / 2, HEIGHT / 2, R0, H0)),
            bordersHandler = new NeutralBordersHandler(H0);

    private static final HashSet<Side> sides = new HashSet<>();
    private static final Cell[][] cells = new Cell[Y_CELLS_COUNT][X_CELLS_COUNT];

    public static void main() {
        final int
                xCellsCount = X_CELLS_COUNT + 2, yCellsCount = Y_CELLS_COUNT + 2,
                xPointsCount = xCellsCount + 1, yPointsCount = yCellsCount + 1;

        Point[][] points = new Point[yPointsCount][xPointsCount];
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[0].length; j++) {
                points[i][j] = new Point((j - 1) * STEP_X, (i - 1) * STEP_Y);
            }
        }

        Side[][] xSides = new Side[yPointsCount][xPointsCount - 1];
        for (int i = 0; i < xSides.length; i++) {
            for (int j = 0; j < xSides[0].length; j++) {
                Side side = new Side(points[i][j], points[i][j + 1], true);
                xSides[i][j] = side;

                if (i != 0 && i != xSides.length - 1 && j != 0 && j != xSides[0].length - 1) {
                    sides.add(side);
                } else {
                    side.init(bordersHandler);
                    side.init(bordersHandler);
                }
            }
        }

        Side[][] ySides = new Side[yPointsCount - 1][xPointsCount];
        for (int i = 0; i < ySides.length; i++) {
            for (int j = 0; j < ySides[0].length; j++) {
                Side side = new Side(points[i][j], points[i + 1][j], false);
                ySides[i][j] = side;

                if (i != 0 && i != ySides.length - 1 && j != 0 && j != ySides[0].length - 1) {
                    sides.add(side);
                } else {
                    side.init(bordersHandler);
                    side.init(bordersHandler);
                }
            }
        }

        for (int i = 0; i < yCellsCount; i++) {
            for (int j = 0; j < xCellsCount; j++) {
                Cell cell = new Cell(
                        new Side[]{
                                xSides[i][j],
                                ySides[i][j + 1],
                                xSides[i + 1][j],
                                ySides[i][j]
                        }
                );

                if (i != 0 && i != yCellsCount - 1 && j != 0 && j != xCellsCount - 1) {
                    cells[i - 1][j - 1] = cell;
                } else {
                    cell.init(bordersHandler);
                    cell.init(bordersHandler);
                }
            }
        }

        Arrays.stream(cells).forEach(cellsSlice -> Arrays.stream(cellsSlice).forEach(cell -> cell.init(initializer)));
        sides.forEach(side -> side.init(initializer));

        ArrayList<Double> times = new ArrayList<>();
        try (OutputHandler outputHandler = new OutputHandler(cells, times)) {
            double curTime = 0;
            times.add(curTime);

            outputHandler.addRecord();

            while (curTime < TIME) {
                double tau = CFL * Math.min(MIN_TAU, Arrays.stream(cells).mapToDouble(cellsSlice -> Arrays.stream(cellsSlice).mapToDouble(Cell::getOptimalTau).min().orElseThrow()).min().orElseThrow());
                curTime += tau;
                times.add(curTime);

                performCellPhase(tau / 2);
                performSidesPhase(tau);
                performCellPhase(tau / 2);

                outputHandler.addRecord();
                //curTime = TIME;
            }
        } catch (IOException e) {
            System.err.println("Проверьте корректность путей к выходным файлам");
            throw new RuntimeException(e);
        }
    }

    private static void performCellPhase(double t) {
        Arrays.stream(cells).forEach(cellsSlice -> Arrays.stream(cellsSlice).forEach(cell -> cell.updateValues(t)));
    }

    private static void performSidesPhase(double t) {
        sides.forEach(Side::setAsNotUpdated);
        sides.forEach(side -> side.updateValues(t));
    }
}
