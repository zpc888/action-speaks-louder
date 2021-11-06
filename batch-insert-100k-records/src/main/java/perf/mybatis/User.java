package perf.mybatis;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String address;
    private String password;
}
