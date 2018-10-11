package com.example.fairhand.a2048.update;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by FairHand on 2018/10/10.<br />
 */
public interface UpdateAPI {
    
    @GET("{game}/{position}/{updateLog}")
    Call<Update> getUpdate(
            @Path("game") String game,
            @Path("position") String position,
            @Path("updateLog") String updateLog);
}
