package io.nub3s;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/scores")
public class ScoresResource {
    @GET
    @Produces("application/json")
    public List<Score> list(){
        return Score.listAllDescending();
    }

    @POST
    @Consumes("application/json")
    public Response create(Score score) {
        score.persist();
        return Response.status(201).build();
    }

    @GET
    @Path("/count")
    @Produces("text/plain")
    public Long count(){
        return Score.count();
    }

    @GET
    @Path("/topten")
    @Produces("application/json")
    public List<Score> topTenList(){
        return Score.findTopTen();
    }

}