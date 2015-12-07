package uk.gov.register.datatype;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.stream.Stream;

public class PointDatatype extends StringDatatype {
    @Override
    public boolean isValid(JsonNode value) {
        String pointsString = value.textValue().trim();
        return super.isValid(value)
                && pointsString.startsWith("[")
                && pointsString.endsWith("]")
                && validPoints(pointsString);
    }

    private boolean validPoints(String pointsString) {
        pointsString = pointsString.replaceAll("^\\[(.*)\\]$", "$1");
        String[] points = pointsString.split(",");
        return points.length == 2 && Stream.of(points).allMatch(p -> p.trim().matches("[-]?\\d+\\.\\d+"));
    }
}
