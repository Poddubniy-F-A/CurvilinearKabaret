package project.model;

import java.util.List;

public record Point(double x, double y) {

    public static Point getCenter(List<Point> points) {
        return new Point(
                points.stream().mapToDouble(Point::x).average().orElseThrow(),
                points.stream().mapToDouble(Point::y).average().orElseThrow()
        );
    }

    public static double getSquare(List<Point> points) {
        int size = points.size();
        double[]
                nodesX = points.stream().mapToDouble(Point::x).toArray(),
                nodesY = points.stream().mapToDouble(Point::y).toArray();

        double res = 0;
        for (int i = 0; i < size - 1; i++) {
            res += nodesX[i] * nodesY[i + 1] - nodesX[i + 1] * nodesY[i];
        }
        return Math.abs(res + nodesX[size - 1] * nodesY[0] - nodesX[0] * nodesY[size - 1]) / 2;
    }
}
