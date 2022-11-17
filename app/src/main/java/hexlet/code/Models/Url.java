package hexlet.code.Models;

import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;
}
