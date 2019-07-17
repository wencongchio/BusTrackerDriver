package wencongchio.com.ucsibustrackerdriver;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/maps/api/distancematrix/json")
    Call<APIResult> getDistanceMatrix(@Query("origins") String origin, @Query("destinations") String destination, @Query("departure_time") String departure_time, @Query("key") String key);
}
