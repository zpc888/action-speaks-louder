package perf.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import perf.mybatis.User;

@Repository
public interface UserRepository extends JpaRepository<JpaUser, Long> {
}
