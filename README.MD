# feign 使用oktthp 和 httpclient 的配置方式

feign 的客户端工具配置类为： 
org.springframework.cloud.netflix.feign.FeignAutoConfiguration

## okhttp 
首先，添加对okhttp 的依赖，以及feign-okhttp
```xml
<dependency>
    <groupId>com.netflix.feign</groupId>
    <artifactId>feign-okhttp</artifactId>
    <version>8.18.0</version>
    <exclusions>
        <exclusion>
            <artifactId>feign-core</artifactId>
            <groupId>com.netflix.feign</groupId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
</dependency>
```
由于默认的配置中使用okhttp的一个条件为：
@ConditionalOnMissingClass("com.netflix.loadbalancer.ILoadBalancer")
但是，由于不使用ribbon 时无法满足条件，所以把这个单独拿出来配置

## httpclient
```xml
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
</dependency>
``` 
需要单独加配置bean 理由同上

## 存在的问题
使用feign 的时候 ,存在依赖ribbon 需要想办法去掉