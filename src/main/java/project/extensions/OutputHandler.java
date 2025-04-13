package project.extensions;

import com.opencsv.CSVWriter;
import project.model.Cell;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class OutputHandler implements AutoCloseable {
    private static final String
            pathToOutputDir = "../output/",
            outputH = "outputH.csv", outputU = "outputU.csv", outputV = "outputV.csv",
            outputHLimits = "h.csv",
            config = "config.csv";

    private final CSVWriter hWriter, uWriter, vWriter, hLimitsWriter;
    private final Cell[][] cells;
    private final ArrayList<Double> times;

    public OutputHandler(Cell[][] cells, ArrayList<Double> times) throws IOException {
        hWriter = configuredWriter(outputH);
        uWriter = configuredWriter(outputU);
        vWriter = configuredWriter(outputV);
        hLimitsWriter = configuredWriter(outputHLimits);

        this.cells = cells;
        this.times = times;
    }

    @Override
    public void close() {
        try {
            CSVWriter configWriter = configuredWriter(config);
            configWriter.writeNext(times.stream().map(String::valueOf).toArray(String[]::new));
            configWriter.writeNext(Arrays.stream(cells[0]).map(cell -> String.valueOf(cell.getCenterPoint().x())).toArray(String[]::new));
            configWriter.writeNext(Arrays.stream(cells).map(cellsSlice -> String.valueOf(cellsSlice[0].getCenterPoint().y())).toArray(String[]::new));
            configWriter.close();

            hWriter.close();
            uWriter.close();
            vWriter.close();

            hLimitsWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CSVWriter configuredWriter(String fileName) throws IOException {
        return new CSVWriter(new FileWriter(pathToOutputDir + fileName), ',', '\0');
    }

    public void addRecord() {
        writeValuesBy(hWriter, Cell::getH);
        writeValuesBy(uWriter, Cell::getU);
        writeValuesBy(vWriter, Cell::getV);

        hLimitsWriter.writeNext(new String[]{
                String.valueOf(Arrays.stream(cells).mapToDouble(cellsSlice -> Arrays.stream(cellsSlice).mapToDouble(Cell::getH).min().orElseThrow()).min().orElseThrow()),
                String.valueOf(Arrays.stream(cells).mapToDouble(cellsSlice -> Arrays.stream(cellsSlice).mapToDouble(Cell::getH).max().orElseThrow()).max().orElseThrow())
        });
    }

    private void writeValuesBy(CSVWriter writer, ToDoubleFunction<Cell> function) {
        writer.writeAll(
                Arrays.stream(cells)
                        .map(cellsSlice -> Arrays.stream(cellsSlice)
                                .map(cell -> String.valueOf(function.applyAsDouble(cell)))
                                .toArray(String[]::new))
                        .collect(Collectors.toList())
        );
    }
}
