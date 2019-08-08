
# Spring Session using Redis

https://codeboje.de/spring-session-tutorial/

## Getting Started with Spring Session
Last Update: 03.04.2019. By Jens in Spring Boot

In this tutorial, we are going to look at Spring Session and build two applications which share the session in Redis.

Spring Session provides a mechanism for managing user’s session information across multiple applications or instances; in an application container independent way. In a traditional web environment, it replaces the container stored HttpSession with its implementation. This implementation relies on various backend data stores, like Redis or JDBC, to actually store the user’s session information. As it is not tied to a particular application container or application anymore, you basically get a clustered session store out of the box.

Furthermore, it provides a ServletFilter for session handling via HTTP headers too so we can even use it in RESTful APIs.

When we use Spring Session, the default JSESSIONID cookie is replaced with one named SESSION.


### The UI
The UI consists of a single HTML page which is protected by a form-based login.

### The API
Provides a simple /name endpoint which simply returns the logged in user’s name.

### The User
We use the default in-memory user store with the default user named user. The only thing we change is we set a fixed password for the user in application.properties like:

spring.security.user.password=password


### Setting Up Spring Session using Redis
The first thing, we need to do for using Spring Session with Redis is to add its Spring Boot starter dependencies:

```

<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```

The first dependency is the actual Spring Session support for Redis. However as it does not include a Redis driver anymore, we must provide one, which we do by including Spring Data Redis.

Now the auto-configuration tries to set up Spring Session but fails in an essential part. It must know which backend store we want to use. You can either declare it with various annotations, e.g., @EnableRedisHttpSession or, as we use now, set a single property in application.properties like:

`spring.session.store-type=redis`
Now, the auto-configuration uses a Redis for the Session and set up everything accordingly. If you do not specify a host, it defaults to localhost and the default Redis port 6379. You can change the connection settings in the application.properties like:

```

spring.redis.host=localhost # Redis server host.
spring.redis.password= # Login password of the redis server.
spring.redis.port=6379 # Redis server port.

```

### Start the UI application now and let’s login with curl.

`curl -X POST -F "username=user" -F "password=password" -v http://localhost:8080/login`

And in the response there is the SESSION cookie value like:

`< Set-Cookie: SESSION=M2E3MzkzNmYtN2IxMS00MmJhLWEzMTAtNTg0ZjI1Y2M1ODU4; Path=/; HttpOnly`

With Spring Session 2 a session id is stored base64 encoded in the cookie value!

The user is now identified by the value of the SESSION cookie on subsequent requests. When our applications run all on the same domain, we can just authenticate with the cookie. However, in a RESTful API, you usually, don’t want to log in via a cookie.

```
C:\>rdcli -h 192.168.99.100
192.168.99.100:6379> keys *
1) spring:session:sessions:expires:65678faf-d016-4a88-9d4e-50a0950482c4
2) spring:session:expirations:1565271060000
3) spring:session:sessions:65678faf-d016-4a88-9d4e-50a0950482c4
4) spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:user
192.168.99.100:6379>

// another example for searching by key:
192.168.99.100:6379> keys "spring:session:index*"
1) spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:user

```

### For RESTful APIs - Using a Header
We do not want a session cookies in a RESTful API or many other web APIs. So, let’s get rid of it and handle it via a header accordingly.

In Spring Session a HttpSessionIdResolver is responsible for detecting and resolving the session Id. By default, it uses the CookieHttpSessionIdResolver, which looks for the session id in a cookie.

Let’s change that by providing another one to the Spring context so it can pick it up. Just define it in a @Configuration class, e.g., SessionApiApplication_ like:

```

@Bean
public HttpSessionIdResolver httpSessionIdResolver() {
    return HeaderHttpSessionIdResolver.xAuthToken(); 
}

```

HeaderHttpSessionIdResolver does two things. First, it expects the session id in the HTTP header x-auth-token and uses it for identification. Second, it will add the same header to each response so we can extract it there.

As mentioned before, the session id is base64 encoded in the cookie, and you can not directly use the value received by the login and input it here. You can either decode it on the command line (on *nix and Macs):

`echo M2E3MzkzNmYtN2IxMS00MmJhLWEzMTAtNTg0ZjI1Y2M1ODU4 | base64 -D`

Or you can disable this feature temporarily, by configuring a HttpSessionIdResolver in SessionUiApplication similiar like:

```

@Bean
public HttpSessionIdResolver httpSessionIdResolver() {
    CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
    DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
    cookieSerializer.setUseBase64Encoding(false);
    resolver.setCookieSerializer(cookieSerializer);
    return resolver; 
}

```

This disables the base64 encoding, and you can use the session id directly from the cookie.

When you start the API now and run curl:

`curl http://localhost:8081/name -v -H "X-Auth-Token: 3a73936f-7b11-42ba-a310-584f25cc5858"`

You’ll get the username as a response:

`user`

and the header in the response: ????

```

> X-Auth-Token: 3f749733-f384-47ca-a351-fc71595583f0

```

========================

springboot-session-redis-ui and springboot-session-redis-api

http://localhost:8080/login

user/password

will see Cookie "SESSION"

