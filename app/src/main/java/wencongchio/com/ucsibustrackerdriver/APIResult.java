package wencongchio.com.ucsibustrackerdriver;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class APIResult implements Serializable {

    @SerializedName("destination_addresses")
    private List<String> destinationAddress;

    @SerializedName("origin_addresses")
    private List<String> originAddress;

    @SerializedName("rows")
    private List<Row> row;

    public List<String> getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(List<String> destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public List<String> getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(List<String> originAddress) {
        this.originAddress = originAddress;
    }

    public List<Row> getRow() {
        return row;
    }

    public void setRow(List<Row> row) {
        this.row = row;
    }


}

class Row implements Serializable {

    @SerializedName("elements")
    private List<Element> element;

    public List<Element> getElement() {
        return element;
    }

    public void setElement(List<Element> element) {
        this.element = element;
    }

}

class Element implements Serializable {

    @SerializedName("distance")
    private Distance distance;

    @SerializedName("duration")
    private Duration duration;

    @SerializedName("duration_in_traffic")
    private DurationInTraffic duration_in_traffic;

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public DurationInTraffic getDuration_in_traffic() {
        return duration_in_traffic;
    }

    public void setDuration_in_traffic(DurationInTraffic duration_in_traffic) {
        this.duration_in_traffic = duration_in_traffic;
    }
}

class Distance implements Serializable {

    @SerializedName("value")
    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

class Duration implements Serializable {

    @SerializedName("value")
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

class DurationInTraffic implements Serializable {

    @SerializedName("value")
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
