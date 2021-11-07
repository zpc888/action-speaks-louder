package perf.mybatis;

import lombok.Data;
import perf.IUser;

@Data
public class User implements IUser {
    private Long id;
    private String username;
    private String address;
    private String password;
}
