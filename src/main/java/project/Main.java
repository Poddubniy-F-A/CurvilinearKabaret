package project;

import org.jfree.data.xy.XYSeries;
import project.extensions.CellsOutputHandler;
import project.init.borders.*;
import project.init.*;
import project.init.conditions.*;
import project.model.cell.Cell;
import project.model.node.Node;
import project.model.node.Point;
import project.model.side.Side;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.*;
import static project.GridsParameters.*;
import static project.model.cell.CellProperty.*;
import static project.extensions.Constants.*;
import static project.model.node.Point.getCenter;

public class Main {

    // обработчики начальных и граничных условий
    private static final InitialConditions conditions = new VortexInitialConditions(
            (LENGTH + HEIGHT / tan(ANGLE)) / 2,
            HEIGHT / 2,
            3 * sqrt(pow(STEP_X, 2) + pow(STEP_Y, 2)),
            1
    );
    private static final Initializer initializer = new DefaultInitializer(conditions);
    private static final BordersHandler bordersHandler = new DefaultBordersHandler(conditions);

    // расчётные структуры
    private static final HashSet<Side> sides = new HashSet<>();
    private static final HashSet<Cell> cells = new HashSet<>();
    static {
        final int xPointsCount = X_CELLS_COUNT + 3, yPointsCount = Y_CELLS_COUNT + 3;

        List<List<Point>> points = fill(
                yPointsCount,
                xPointsCount,
                (i, j) -> new Point((i - 1) * STEP_Y / tan(ANGLE) + (j - 1) * STEP_X, (i - 1) * STEP_Y)
        );

        List<List<Node>> nodesX = fill(
                yPointsCount,
                xPointsCount - 1,
                (i, j) -> new Node(getCenter(Arrays.asList(points.get(i).get(j), points.get(i).get(j + 1))))
        );

        List<List<Node>> nodesY = fill(
                yPointsCount - 1,
                xPointsCount,
                (i, j) -> new Node(getCenter(Arrays.asList(points.get(i).get(j), points.get(i + 1).get(j))))
        );

        List<List<Node>> nodesC = fill(
                yPointsCount - 1,
                xPointsCount - 1,
                (i, j) -> new Node(getCenter(Arrays.asList(points.get(i).get(j), points.get(i).get(j + 1), points.get(i + 1).get(j), points.get(i + 1).get(j + 1))))
        );

        initBorders(nodesX);
        initBorders(nodesY);
        initBorders(nodesC);

        List<List<Side>> xSides = fill(
                Y_CELLS_COUNT + 1,
                X_CELLS_COUNT,
                (i, j) -> new Side(
                        points.get(i + 1).get(j + 1), points.get(i + 1).get(j + 2),
                        nodesX.get(i + 1).get(j + 1),
                        nodesX.get(i).get(j + 1), nodesC.get(i).get(j + 1), nodesC.get(i + 1).get(j + 1), nodesX.get(i + 2).get(j + 1)
                )
        );

        List<List<Side>> ySides = fill(
                Y_CELLS_COUNT,
                X_CELLS_COUNT + 1,
                (i, j) -> new Side(
                        points.get(i + 1).get(j + 1), points.get(i + 2).get(j + 1),
                        nodesY.get(i + 1).get(j + 1),
                        nodesY.get(i + 1).get(j), nodesC.get(i + 1).get(j), nodesC.get(i + 1).get(j + 1), nodesY.get(i + 1).get(j + 2)
                )
        );

        addAll(sides, xSides);
        addAll(sides, ySides);

        List<List<Cell>> cellsArray = fill(
                Y_CELLS_COUNT,
                X_CELLS_COUNT,
                (i, j) -> new Cell(
                        Arrays.asList(points.get(i + 1).get(j + 1), points.get(i + 1).get(j + 2), points.get(i + 2).get(j + 2), points.get(i + 2).get(j + 1)),
                        nodesC.get(i + 1).get(j + 1),
                        xSides.get(i).get(j), ySides.get(i).get(j + 1), xSides.get(i + 1).get(j), ySides.get(i).get(j)
                )
        );

        addAll(cells, cellsArray);
    }

    private static <T> List<List<T>> fill(int slicesNum, int sliceLength, ElementHandler<T> handler) {
        List<List<T>> res = new ArrayList<>(slicesNum);
        for (int i = 0; i < slicesNum; i++) {
            List<T> slice = new ArrayList<>(sliceLength);
            for (int j = 0; j < sliceLength; j++) {
                slice.add(handler.handle(i, j));
            }
            res.add(slice);
        }
        return res;
    }

    @FunctionalInterface
    public interface ElementHandler<T> {

        T handle(int i, int j);
    }

    private static void initBorders(List<List<Node>> nodes) {
        nodes.getFirst().forEach(bordersHandler::setValuesIn);
        nodes.getLast().forEach(bordersHandler::setValuesIn);

        for (int i = 1; i < nodes.size() - 1; i++) {
            List<Node> nodesSlice = nodes.get(i);
            bordersHandler.setValuesIn(nodesSlice.getFirst());
            bordersHandler.setValuesIn(nodesSlice.getLast());
        }
    }

    private static <T> void addAll(Set<T> set, List<List<T>> list) {
        list.forEach(set::addAll);
    }

    public static void main() throws IOException {
        final double minTau = 0.01;

        cells.forEach(initializer::initValuesIn);
        sides.forEach(initializer::initValuesIn);

        CellsOutputHandler outputHandler = new CellsOutputHandler(cells);

        XYSeries minSeries = new XYSeries("Min height"), maxSeries = new XYSeries("Max height");

        double curTime = 0;
        outputHandler.makePropertyGraph(H, curTime);
        while (curTime < TIME) {
            minSeries.add(curTime, cells.stream().mapToDouble(Cell::getH).min().orElseThrow());
            maxSeries.add(curTime, cells.stream().mapToDouble(Cell::getH).max().orElseThrow());

            double tau = CFL * min(minTau, cells.stream().mapToDouble(Cell::getOptimalTau).min().orElseThrow());

            cells.forEach(cell -> cell.updateValues(tau / 2));
            sides.forEach(Side::setAsNotUpdated);
            sides.forEach(side -> side.updateValues(tau));
            cells.forEach(cell -> cell.updateValues(tau / 2));

            curTime += tau;

            //curTime = TIME;
        }
        outputHandler.makePropertyGraph(H, curTime);

        outputHandler.makeHeightsGraph(minSeries, maxSeries);
    }
}
