package hexlet.code.models;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Setter
public class Url extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    public Url(){};

    public Url(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Url{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
