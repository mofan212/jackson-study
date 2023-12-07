package indi.mofan.pojo;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author mofan
 * @date 2023/12/7 16:42
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieWithNullValue {

    @Expose
    private String imdbId;

    private String director;

    @Expose
    private List<ActorGson> actors;
}
