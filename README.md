# Building the application

```
$ mvn package
```

# Running the application

(assuming heron 0.14.5 is already set up on localhost and is accepting topologies)

```
$ ./heronctl.sh local submit
$ ./heronctl.sh local kill
```
