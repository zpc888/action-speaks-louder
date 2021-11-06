# Action speaks louder than words
It tries lots of POC in runnable code/projects.

## Batch Insert 100k Records into database
[To verify which way is the fastest to run](./batch-insert-100k-records/README.md)
- run batch in one shot
```sql
insert into table(col1, col2, ...) values (v11, v12, ...), (v21, v22, ...), ...
```
- run batch with prepared statements, then add into batch
```sql
prepareStatement: 
    PreparedStatement ps = c.prepareStatement insert into table(col1, col2, ...) values (?, ?, ...)
repeating: 
    for (Record r: records) {
        ps.set..(1, r.xxx);
...
        ps.addBatch();
        ps.clearParameters();
}
    int[] results = ps.executeBatch();
```
- run normal way one by one
```sql
prepareStatement: 
    PreparedStatement ps = c.prepareStatement insert into table(col1, col2, ...) values (?, ?, ...)
repeating: 
    for (Record r: records) {
        ps.set..(1, r.xxx);
        ...
        int result = ps.execute();
    }
```
