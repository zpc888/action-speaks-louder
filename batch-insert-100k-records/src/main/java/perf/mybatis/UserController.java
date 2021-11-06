package perf.mybatis;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@AllArgsConstructor
@Slf4j
@RestController
public class UserController {
    private final UserService userService;
//    private final LocalDateTime upAt = LocalDateTime.now();
//    private final Map<String, Object> batchInsert1By1 = new HashMap<>();
//    private final Map<String, Object> insert1By1 = new HashMap<>();
//    private final Map<String, Object> batchInsertIn1Shot = new HashMap<>();

    @GetMapping("/perf-tests/batch-insert-1-by-1")
    public ResponseEntity<Mono<Map<String, Object>>> batchInsert1By1(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "batch-insert-one-by-one", userService::addUsersInBatch1By1);
    }

    // in h2 memory db, to insert 100K
    // in normal 1-by-1: 1.7 sec; in batch 1-by-1: 1.2 sec; in batch one shot: 2.0 sec

    // in mysql, to insert 80K
    // in normal 1-by-1: 72 sec; in batch 1-by-1: 14.5 sec; in batch one shot: 1.8 sec

    @GetMapping("/perf-tests/normal-insert-1-by-1")
    public ResponseEntity<Mono<Map<String, Object>>> normalInsert1By1(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "normal-insert-one-by-one", userService::addUsers1By1);
    }

    @GetMapping("/perf-tests/batch-insert-one-shot")
    public ResponseEntity<Mono<Map<String, Object>>> batchInsertOneShot(
            @RequestParam(value = "size", required = false, defaultValue = "100000") int size) {
        return perform(size, "batch-insert-one-shot", userService::addUsersInBatchOneShot);
    }

    @GetMapping("/perf-tests/all-in-one")
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



    private ResponseEntity<Mono<Map<String, Object>>> perform(int size, String prefix, Consumer<List<User>> howToInsert) {
        if (size < 0) {
            return ResponseEntity.badRequest().build();
        }
        if (size == 0) {
            size = 100_000;
        }
        List<User> users = prepareUsers(size);
        int deletedCount = userService.deleteAllUsers();
        Map<String, Object> ret = new HashMap<>();
        ret.put("deletedHistoryCount", deletedCount);
        ret.put("size", size);
        ret.put(prefix + "-duration", timeInserting(users, howToInsert));
        return ResponseEntity.ok(Mono.just(ret));
    }

    private List<User> prepareUsers(int size) {
        List<User> ret = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            User user = new User();
            user.setUsername("gz-" + i);
            user.setAddress("toronto-" + i);
            user.setPassword("secret-" + i);
            ret.add(user);
        }
        return ret;
    }

    private Duration timeInserting(List<User> users, Consumer<List<User>> func) {
        LocalDateTime bgnAt = LocalDateTime.now();
        func.accept(users);
        return Duration.between(bgnAt, LocalDateTime.now());
    }
}
