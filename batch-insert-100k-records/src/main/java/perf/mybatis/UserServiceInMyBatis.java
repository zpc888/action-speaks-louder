package perf.mybatis;

import lombok.AllArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class UserServiceInMyBatis {
    private final UserMapper userMapper;
    private final SqlSessionFactory sqlSessionFactory;

    @Transactional(rollbackFor = Exception.class)
    public void addUsersInBatch1By1(List<User> users) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        for (User user: users) {
            userMapper.addUser(user);
        }
        session.commit();
    }

    public void addUsers1By1(List<User> users) {
        for (User user: users) {
            userMapper.addUser(user);
        }
    }

    public void addUsersInBatchOneShot(List<User> users) {
        userMapper.addUsersInOneShot(users);
    }

    public int deleteAllUsers() {
        return userMapper.deleteAllUsers();
    }
}
