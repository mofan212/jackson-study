package indi.mofan.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author mofan
 * @date 2023/12/7 16:37
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private String imdbId;
    private String director;
    private List<ActorGson> actors;
}

