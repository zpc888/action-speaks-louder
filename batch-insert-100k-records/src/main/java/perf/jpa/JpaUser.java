package perf.jpa;

import lombok.Data;
import perf.IUser;

import javax.persistence.*;

@Entity
@Table(name="jpauser")
@Data
public class JpaUser implements IUser {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String username;
    private String address;
    private String password;
}
