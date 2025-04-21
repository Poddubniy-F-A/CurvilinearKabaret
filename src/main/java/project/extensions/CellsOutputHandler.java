package project.extensions;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
import project.model.cell.Cell;
import project.model.node.Point;
import project.model.cell.CellProperty;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static java.lang.Math.PI;
import static org.jfree.chart.plot.PlotOrientation.VERTICAL;
import static project.GridsParameters.*;

public class CellsOutputHandler {

    private static final String pathToResults = "./res";
    private static final Format format = Format.PNG;

    private enum Format {
        PNG, JPEG
    }

    private static final int SCREEN_WIDTH = 1920, SCREEN_HEIGHT = 1200;
    private static final AffineTransform transform = new AffineTransform();
    private static final Polygon cellShape = new Polygon();

    static {
        transform.scale(SCREEN_HEIGHT, -SCREEN_HEIGHT);
        transform.translate(0, -HEIGHT);

        double scale = 0.9 * SCREEN_HEIGHT / HEIGHT;
        int
                width = (int) (scale * STEP_X),
                height = (int) (scale * STEP_Y),
                skew = (int) (scale * STEP_Y / Math.tan(ANGLE));

        cellShape.addPoint(0, 0);
        cellShape.addPoint(width, 0);
        cellShape.addPoint(width + skew, -height);
        cellShape.addPoint(skew, -height);
        cellShape.translate(-(width + skew) / 2, height / 2);
    }

    private final HashMap<CellProperty, ColorMapper> mapperByProperty = new HashMap<>();

    private final Set<Cell> cells;

    public CellsOutputHandler(Set<Cell> cells) {
        this.cells = cells;

        for (CellProperty property : CellProperty.values()) {
            mapperByProperty.put(
                    property,
                    new ColorMapper(
                            new ColorMapRainbow(),
                            cells.stream().mapToDouble(property.getMapper()).min().orElseThrow(),
                            cells.stream().mapToDouble(property.getMapper()).max().orElseThrow(),
                            new Color(1, 1, 1, .5f)
                    )
            );
        }
    }

    public void makeHeightsGraph(XYSeries minSeries, XYSeries maxSeries) throws IOException {
        final float lineWidth = 3.0f;
        final double offset = 0.01;

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(minSeries);
        dataset.addSeries(maxSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Height limits",
                "time",
                "height",
                dataset,
                VERTICAL,
                true,
                true,
                false
        );

        chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(lineWidth));
        chart.getXYPlot().getRenderer().setSeriesStroke(1, new BasicStroke(lineWidth));

        chart.getXYPlot().getRangeAxis().setRange(
                minSeries.getMinY() - offset,
                maxSeries.getMaxY() + offset
        );

        savePicOf(Path.of(pathToResults + "/" + (ANGLE / PI)), chart, "height limits");
    }

    public void makePropertyGraph(CellProperty property, double curTime) throws IOException {
        ToDoubleFunction<Cell> function = property.getMapper();

        HashMap<Double, XYSeries> map = new HashMap<>();
        cells.forEach(cell -> {
            double value = function.applyAsDouble(cell);
            if (!map.containsKey(value)) {
                map.put(value, new XYSeries(String.valueOf(value)));
            }
            map.get(value).add(cell.getCenterPoint().x(), cell.getCenterPoint().y());
        });
        XYSeriesCollection dataset = new XYSeriesCollection();
        map.values().forEach(dataset::addSeries);

        JFreeChart chart = ChartFactory.createScatterPlot(
                property.getName() + " at T = " + curTime,
                "X",
                "Y",
                dataset,
                VERTICAL,
                false,
                true,
                false
        );

        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        r.setDefaultShapesVisible(true);
        r.setDefaultShapesFilled(true);

        ColorMapper colorMapper = mapperByProperty.get(property);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            Color color = colorMapper.getColor(Double.parseDouble(dataset.getSeriesKey(i).toString()));
            r.setSeriesPaint(i, new java.awt.Color(color.r, color.g, color.b));

            r.setSeriesShape(i, cellShape);
        }

        chart.getXYPlot().getDomainAxis().setRange(
                0,
                HEIGHT * 1.05 * SCREEN_WIDTH / SCREEN_HEIGHT
        );
        chart.getXYPlot().getRangeAxis().setRange(
                0,
                HEIGHT
        );

        savePicOf(Path.of(pathToResults + "/" + (ANGLE / PI) + "/" + property.getName()), chart, String.valueOf(curTime));

        /*BufferedImage image = new BufferedImage(
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = image.createGraphics();
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.transform(transform);

        ColorMapper colorMapper = mapperByProperty.get(property);
        ToDoubleFunction<Cell> function = property.getMapper();
        cells.forEach(cell -> {
            Color color = colorMapper.getColor(function.applyAsDouble(cell));
            g2d.setColor(new java.awt.Color(color.r, color.g, color.b));

            Path2D.Double parallelogram = new Path2D.Double();
            List<Point> points = cell.getPoints();
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
        ImageIO.write(image, format.strValue, new File(dir + "/" + curTime + "." + format.strValue));*/
    }

    private static void savePicOf(Path dir, JFreeChart chart, String name) throws IOException {
        if (!Files.exists(dir.getParent())) {
            Files.createDirectory(dir.getParent());
        }
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
        if (format == Format.PNG) {
            ChartUtils.saveChartAsPNG(new File(dir + "/" + name + ".png"), chart, SCREEN_WIDTH, SCREEN_HEIGHT);
        } else if (format == Format.JPEG) {
            ChartUtils.saveChartAsJPEG(new File(dir + "/" + name + ".jpeg"), chart, SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    public void make3DVortex(CellProperty property, double curTime) {
        if (ANGLE == PI / 2) {
            ToDoubleFunction<Cell> func = property.getMapper();
            Shape surface = (Shape) new OrthonormalTessellator().build(cells.stream().map(cell -> {
                Point point = cell.getCenterPoint();
                return new Coord3d(point.x(), point.y(), func.applyAsDouble(cell));
            }).collect(Collectors.toList()));
            surface.setColorMapper(mapperByProperty.get(property));
            surface.setFaceDisplayed(true);
            surface.setWireframeDisplayed(false);
            surface.setWireframeColor(Color.BLACK);

            Chart chart = new SwingChartComponentFactory().newChart(Quality.Intermediate, "swing");
            chart.getScene().getGraph().add(surface);
            ChartLauncher.openChart(chart, new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT), "t = " + curTime);
        }
    }
}
