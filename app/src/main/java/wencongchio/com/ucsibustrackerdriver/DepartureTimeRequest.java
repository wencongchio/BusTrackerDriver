package wencongchio.com.ucsibustrackerdriver;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DepartureTimeRequest extends StringRequest {

    private static final String REQUEST_URL = "https://easeworks.000webhostapp.com/shuttletracker/departure_time.php";
    private Map<String, String> params;

    public DepartureTimeRequest(int interval, String bus, String type, String destination, Response.Listener<String> listener) {
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("interval", interval + "");
        params.put("bus", bus);
        params.put("type", type);
        params.put("destination", destination);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
