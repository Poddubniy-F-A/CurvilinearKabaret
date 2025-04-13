package project;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.chart.factories.SwingChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Rectangle;
import org.jzy3d.plot3d.builder.concrete.OrthonormalTessellator;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import project.extensions.ElementHandler;
import project.init.borders.*;
import project.init.*;
import project.init.conditions.*;
import project.model.Cell;
import project.model.Node;
import project.model.Point;
import project.model.Side;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static project.extensions.Constants.*;
import static project.model.Point.getCenter;

public class Main {

    // сетка
    private static final int X_CELLS_COUNT = 50, Y_CELLS_COUNT = 50;
    private static final double
            LENGTH = 1, STEP_X = LENGTH / X_CELLS_COUNT,
            HEIGHT = 1, STEP_Y = HEIGHT / Y_CELLS_COUNT,
            ANGLE = PI / 2.,
            H0 = 1,
            R0 = 3 * sqrt(pow(STEP_X, 2) + pow(STEP_Y, 2));

    // время
    private static final double MIN_TAU = 0.1, TIME = 1; //200 * (2 * PI * R0) / ALPHA;
    private static double curTime = 0;

    // обработчики начальных и граничных условий
    private static final InitialConditions conditions = new VortexInitialConditions((LENGTH + HEIGHT / tan(ANGLE)) / 2, HEIGHT / 2, R0, H0); //ConstantInitialConditions(1, 0, 1);//
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

    // вывод
    private static final String pathToResults = "./res";
    private static final String format = "png";

    private static final int SCREEN_WIDTH = 1920, SCREEN_HEIGHT = 1200;
    private static final AffineTransform transform = new AffineTransform();
    static {
        transform.scale(SCREEN_HEIGHT, -SCREEN_HEIGHT);
        transform.translate(0, -HEIGHT);
    }

    public static void main() throws IOException {
        cells.forEach(initializer::initValuesIn);
        sides.forEach(initializer::initValuesIn);

        savePicOf(Cell::getH);

        while (curTime < TIME) {
            double tau = CFL * min(MIN_TAU, cells.stream().mapToDouble(Cell::getOptimalTau).min().orElseThrow());

            cells.forEach(cell -> cell.updateValues(tau / 2));
            sides.forEach(Side::setAsNotUpdated);
            sides.forEach(side -> side.updateValues(tau));
            cells.forEach(cell -> cell.updateValues(tau / 2));

            curTime += tau;

            //curTime = TIME;
        }

        savePicOf(Cell::getH);
    }

    private static void savePicOf(ToDoubleFunction<Cell> function) throws IOException {
        BufferedImage image = new BufferedImage(
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = image.createGraphics();
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.transform(transform);

        ColorMapper colorMapper = new ColorMapper(
                new ColorMapRainbow(),
                cells.stream().mapToDouble(function).min().orElseThrow(),
                cells.stream().mapToDouble(function).max().orElseThrow(),
                new Color(1, 1, 1, .5f)
        );

        cells.forEach(cell -> {
            Color color = colorMapper.getColor(function.applyAsDouble(cell));
            g2d.setColor(new java.awt.Color(color.r, color.g, color.b));

            List<Point> points = cell.getPoints();
            Path2D.Double parallelogram = new Path2D.Double();
            parallelogram.moveTo(points.getFirst().x(), points.getFirst().y());
            for (int i = 1; i < points.size(); i++) {
                parallelogram.lineTo(points.get(i).x(), points.get(i).y());
            }
            parallelogram.closePath();

            g2d.fill(parallelogram);
        });

        g2d.dispose();

        Path dir = Path.of(pathToResults + "/" + (ANGLE / PI));
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
        String filename = dir + "/" + curTime + "." + format;
        ImageIO.write(image, format, new File(filename));

        if (ANGLE == PI / 2) {
            Shape surface = (Shape) new OrthonormalTessellator().build(cells.stream().map(cell -> {
                Point point = cell.getCenterPoint();
                return new Coord3d(point.x(), point.y(), function.applyAsDouble(cell));
            }).collect(Collectors.toList()));
            surface.setColorMapper(colorMapper);
            surface.setFaceDisplayed(true);
            surface.setWireframeDisplayed(false);
            surface.setWireframeColor(Color.BLACK);

            Chart chart = new SwingChartComponentFactory().newChart(Quality.Intermediate, "swing");
            chart.getScene().getGraph().add(surface);
            ChartLauncher.openChart(chart, new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT), "t = " + curTime);
        }
    }
}
