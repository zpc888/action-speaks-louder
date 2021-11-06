# Which is the fastest way to insert 100k records

## How to run
```shell
./gradlew clean build 

# Run with h2 in memory db
./gradlew bootRun 
# API Port: 8081, h2 console port: 9081

# Run with mysql connection
./gradlew bootRun --args="--spring.profiles.active=mysql"
# API Port: 8081
```

## APIs
![APS to performance test 100k insertion](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/zpc888/action-speaks-louder/master/batch-insert-100k-records/docs/api-class-view.puml)

## Test results
### MySQL databases (80K)
100k records concatenation is greater than 4M (4,194,304), which is the max packet size for a query.
To avoid com.mysql.cj.jdbc.exceptions.PacketTooBigException, I reduce to 80K, otherwise I need to add
a `split` help method to `List<List<T> split(<List<T> origin, int chunkSize)`
- batch insert in one shot (string concatenation), it is about 2 seconds
- batch insert one by one (i.e. jdbc addBatch), it is about 15 seconds
- normal insert one by one (i.e. without addBatch), it is about 80 seconds

### H2 In Memory database (100K)
- batch insert in one shot (string concatenation), it is about 2 seconds
- batch insert one by one (i.e. jdbc addBatch), it is about 1.2 seconds
- normal insert one by one (i.e. without addBatch), it is about 1.7 seconds
 
**Analysis:**
- Because h2-in-mem is in memory sharing the same process resources, connection won't be expensive.
- String concatenation may be more expensive than connection, which may be just a local method call
