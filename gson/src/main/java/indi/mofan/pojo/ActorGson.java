package indi.mofan.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @author mofan
 * @date 2023/12/7 16:36
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActorGson {
    private String imdbId;
    private Date dateOfBirth;
    private List<String> filmography;
}
