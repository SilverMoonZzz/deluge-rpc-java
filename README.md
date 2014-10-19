deluge-rpc-java
===============

Deluge RPC implementation in Java

### Examples

#### Connect to server

```java
DelugeSession session = DelugeClient.getSession("localhost")

DelugeFuture<IntegerResponse>  future = session.login("user", "password");

// blocks indefinitely
Integer loginResponse = future.get();

// blocks up to 5 seconds then throws TimeOutException
Integer loginResponse = future.get(5, TimeUnit.SECONDS);
```

#### Response Callback

```java
DelugeSession session = ... // get session and login

session.getTorrentsStatus().then(new ResponseCallback<TorrentsStatusResponse, DelugeException>() {
    @Override
    public void onResponse(TorrentsStatusResponse response) {
        // handle response
    }
});
```


### Futures

All the API calls return [Futures](http://en.wikipedia.org/wiki/Futures_and_promises).


### Download

Maven:

```xml
<dependency>
    <groupId>se.dimovski.projects</groupId>
    <artifactId>deluge.rpc</artifactId>
    <version>(wanted version)</version>
</dependency>
```