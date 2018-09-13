package services;

import bean.Item;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface XIVAPIService {
    @GET("Item/{item}")
    Call<Item> getItem(@Path("item") String item, @Query("key") String key);
}