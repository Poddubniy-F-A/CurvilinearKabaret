package project.model.cell;

import java.util.function.ToDoubleFunction;

public enum CellProperty {

    H("Height", Cell::getH), U("Horizontal speed", Cell::getU), V("Vertical speed", Cell::getV);

    private final String name;
    private final ToDoubleFunction<Cell> mapper;

    CellProperty(String name, ToDoubleFunction<Cell> mapper) {
        this.name = name;
        this.mapper = mapper;
    }

    public String getName() {
        return name;
    }

    public ToDoubleFunction<Cell> getMapper() {
        return mapper;
    }
}
