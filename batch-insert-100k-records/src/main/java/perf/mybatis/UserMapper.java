package perf.mybatis;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("insert into user (username,address,password) values (#{username},#{address},#{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Long addUser(User user);

    @Delete("delete from user")
    int deleteAllUsers();

    @Insert({
        "<script>",
            "insert into user (username,address,password) values ",
            "<foreach item='u' collection='users' open='' close='' separator=','>",
              "(#{u.username}, #{u.address}, #{u.password})",
            "</foreach>",
        "</script>"
    })
    void addUsersInOneShot(@Param("users") List<User> users);
}
