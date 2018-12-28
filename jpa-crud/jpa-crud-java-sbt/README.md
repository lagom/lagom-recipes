# Lagom Recipe: JPA CRUD  

This recipe demonstrates, how you can use Play's [JPA API](https://www.playframework.com/documentation/2.6.x/JavaJPA) 
for implement Lagom service with CRUD-oriented persistence.

## About service

Service implement CRUD operation for entity `Entity`.

## Testing the recipe

#### unit tests

You can test this recipe using the provided tests:

```bash
sbt test
```

#### manual tests

You can also test this recipe manually using 2 separate terminals.

On one terminal start the service:

```bash
sbt runAll
```

On a separate terminal, use `curl` for:

* create entity
```bash
$ curl -X POST -H "Content-Type: application/json" -d '{"property": "property", "parts": ["part1", "part2"]}' http://localhost:9000/entities
{"id":"cc46f88a-95f3-40f0-b46a-510b9ea76a63","property":"property","parts":["part1","part2"]}
```

* update entity
```bash
$ curl -X PUT -H "Content-Type: application/json" -d '{"property": "new property", "parts": ["part3", "part4"]}' http://localhost:9000/entities/cc46f88a-95f3-40f0-b46a-510b9ea76a63
```

* get updated entity
```bash
$ curl http://localhost:9000/entities/cc46f88a-95f3-40f0-b46a-510b9ea76a63
{"id":"cc46f88a-95f3-40f0-b46a-510b9ea76a63","property":"new property","parts":["part3","part4"]}
```

* delete entity
```bash
$ curl -X DELETE http://localhost:9000/entities/cc46f88a-95f3-40f0-b46a-510b9ea76a63
```

* get deleted entity
```bash
$ curl http://localhost:9000/entities/cc46f88a-95f3-40f0-b46a-510b9ea76a63
{"name":"NotFound","detail":"Entity with id='cc46f88a-95f3-40f0-b46a-510b9ea76a63' is not found"}
```