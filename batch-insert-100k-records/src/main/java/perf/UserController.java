package perf;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import perf.jpa.JpaUser;
import perf.mybatis.User;
import perf.jpa.UserServiceInJpa;
import perf.mybatis.UserServiceInMyBatis;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@AllArgsConstructor
@Slf4j
@RestController
public class UserController {
    private final UserServiceInJpa userServiceInJpa;
    private final UserServiceInMyBatis userServiceInMyBatis;
//    private final LocalDateTime upAt = LocalDateTime.now();
//    private final Map<String, Object> batchInsert1By1 = new HashMap<>();
//    private final Map<String, Object> insert1By1 = new HashMap<>();
//    private final Map<String, Object> batchInsertIn1Shot = new HashMap<>();

    @GetMapping("/perf-tests/jpa/batch-insert-1-by-1")
    public ResponseEntity<Mono<Map<String, Object>>> jpaBatchInsert1By1(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "jpa-batch-insert-one-by-one", userServiceInJpa::addUsers, userServiceInJpa::deleteAllUsers, () -> new JpaUser());
    }

    @GetMapping("/perf-tests/mybatis/batch-insert-1-by-1")
    public ResponseEntity<Mono<Map<String, Object>>> batchInsert1By1(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "batch-insert-one-by-one", userServiceInMyBatis::addUsersInBatch1By1, userServiceInMyBatis::deleteAllUsers, () -> new User());
    }

    // in h2 memory db, to insert 100K in mybatis
    // in normal 1-by-1: 1.7 sec; in batch 1-by-1: 1.2 sec; in batch one shot: 2.0 sec

    // in mysql, to insert 80K in mybatis
    // in normal 1-by-1: 72 sec; in batch 1-by-1: 14.5 sec; in batch one shot: 1.8 sec

    @GetMapping("/perf-tests/mybatis/normal-insert-1-by-1")
    public ResponseEntity<Mono<Map<String, Object>>> normalInsert1By1(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "normal-insert-one-by-one", userServiceInMyBatis::addUsers1By1, userServiceInMyBatis::deleteAllUsers, () -> new User());
    }

    @GetMapping("/perf-tests/mybatis/batch-insert-one-shot")
    public ResponseEntity<Mono<Map<String, Object>>> batchInsertOneShot(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "batch-insert-one-shot", userServiceInMyBatis::addUsersInBatchOneShot, userServiceInMyBatis::deleteAllUsers, () -> new User());
    }

    @GetMapping("/perf-tests/mybatis/all-in-one")
    public ResponseEntity<Mono<Map<String, Object>>> allInOneTest(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        ResponseEntity<Mono<Map<String, Object>>> batch1x1 = batchInsert1By1(size);
        if (batch1x1.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return batch1x1;
        }
        ResponseEntity<Mono<Map<String, Object>>> normal1x1 = normalInsert1By1(size);
        ResponseEntity<Mono<Map<String, Object>>> batchOneShot = batchInsertOneShot(size);

        Mono<Map<String, Object>> ret = Flux.merge(batch1x1.getBody(), normal1x1.getBody(), batchOneShot.getBody())
                .reduce((Map<String, Object>)(new HashMap<String, Object>()), (m1, m2) -> { m1.putAll(m2); return m1; }).single();

        return ResponseEntity.ok(ret);
    }



    private <T extends IUser> ResponseEntity<Mono<Map<String, Object>>> perform(int size, String prefix, Consumer<List<T>> howToInsert
            , Runnable deleteAllHistoryRecords, Supplier<T> recordCreator) {
        if (size < 0) {
            return ResponseEntity.badRequest().build();
        }
        if (size == 0) {
            size = 100_000;
        }
        List<T> users = prepareUsers(size, recordCreator);
        int deletedCount = userServiceInMyBatis.deleteAllUsers();
        Map<String, Object> ret = new HashMap<>();
        ret.put("deleted-history-count", deletedCount);
        ret.put("size", size);
        ret.put(prefix + "-duration", timeInserting(users, howToInsert));
        return ResponseEntity.ok(Mono.just(ret));
    }

    private <T extends IUser> List<T> prepareUsers(int size, Supplier<T> recordCreator) {
        List<T> ret = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T user = recordCreator.get();
            user.setUsername("gz-" + i);
            user.setAddress("toronto-" + i);
            user.setPassword("secret-" + i);
            ret.add(user);
        }
        return ret;
    }

    private <T extends IUser> Duration timeInserting(List<T> users, Consumer<List<T>> func) {
        LocalDateTime bgnAt = LocalDateTime.now();
        func.accept(users);
        return Duration.between(bgnAt, LocalDateTime.now());
    }
}
