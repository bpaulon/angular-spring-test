# angular-spring-test
AngularJs to Spring Boot tests

# Redis controller
it needs a running Redis db

Redis support in spring data
http://docs.spring.io/spring-data/redis/docs/1.4.0.RC1/reference/html/redis.html


# Redis cheat sheet
List of commands here https://redis.io/commands

### General
KEYS * - shows all the keys

### Hashes
hgetall <the key of the hash> - displays all 

### Transactions
> MULTI
OK
> INCR foo
QUEUED
> INCR bar
QUEUED
> EXEC
1) (integer) 1
2) (integer) 1

WATCH is for optimistic locking. Watch is discarded after the transaction ends (EXEC or DISCARD)
 
### Ordered set
zrange <the key of the set> 0 -1 -displays the whole set ordered by rank
zrevrange 	- displays the values in reverse order

