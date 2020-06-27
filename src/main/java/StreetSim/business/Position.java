package StreetSim.business;

import java.util.Objects;

/**
 * Abbildung der Position von den X und Y-Koordinaten
 */
public class Position {

    private int positionX;
    private int positionY;

    public Position() {

    }

    // TODO: Anpassung der equals und hash Methoden
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return positionX == position.positionX &&
                positionY == position.positionY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionX, positionY);
    }

}
