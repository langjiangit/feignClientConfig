package asia.zrc.springcloud.test;


import feign.Client;
import feign.httpclient.ApacheHttpClient;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientConnectionPoolFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.cloud.netflix.feign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * org.springframework.cloud.netflix.feign.FeignAutoConfiguration
 */

@Configuration
public class DefaultConfiguration {

    @Configuration
    @ConditionalOnClass(OkHttpClient.class)
    @ConditionalOnMissingBean(okhttp3.OkHttpClient.class)
    @ConditionalOnProperty(value = "feign.okhttp.enabled")
    protected static class OkHttpFeignConfiguration {

        private okhttp3.OkHttpClient okHttpClient;

        @Bean
        @ConditionalOnMissingBean(ConnectionPool.class)
        public ConnectionPool httpClientConnectionPool(FeignHttpClientProperties httpClientProperties,
                                                       OkHttpClientConnectionPoolFactory connectionPoolFactory) {
            Integer maxTotalConnections = httpClientProperties.getMaxConnections();
            Long timeToLive = httpClientProperties.getTimeToLive();
            TimeUnit ttlUnit = httpClientProperties.getTimeToLiveUnit();
            return connectionPoolFactory.create(maxTotalConnections, timeToLive, ttlUnit);
        }

        @Bean
        public okhttp3.OkHttpClient client(OkHttpClientFactory httpClientFactory,
                                           ConnectionPool connectionPool, FeignHttpClientProperties httpClientProperties) {
            Boolean followRedirects = httpClientProperties.isFollowRedirects();
            Integer connectTimeout = httpClientProperties.getConnectionTimeout();
            Boolean disableSslValidation = httpClientProperties.isDisableSslValidation();
            this.okHttpClient = httpClientFactory.createBuilder(disableSslValidation).
                    connectTimeout(connectTimeout, TimeUnit.MILLISECONDS).
                    followRedirects(followRedirects).
                    connectionPool(connectionPool).build();
            return this.okHttpClient;
        }

        @PreDestroy
        public void destroy() {
            if (okHttpClient != null) {
                okHttpClient.dispatcher().executorService().shutdown();
                okHttpClient.connectionPool().evictAll();
            }
        }

    }

    @Configuration
    @ConditionalOnClass(ApacheHttpClient.class)
    @ConditionalOnMissingBean(CloseableHttpClient.class)
    @ConditionalOnProperty(value = "feign.httpclient.enabled", matchIfMissing = true)
    protected static class HttpClientFeignConfiguration {
        private final Timer connectionManagerTimer = new Timer(
                "FeignApacheHttpClientConfiguration.connectionManagerTimer", true);

        @Autowired(required = false)
        private RegistryBuilder registryBuilder;

        private CloseableHttpClient httpClient;

        @Bean
        @ConditionalOnMissingBean(HttpClientConnectionManager.class)
        public HttpClientConnectionManager connectionManager(
                ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
                FeignHttpClientProperties httpClientProperties) {
            final HttpClientConnectionManager connectionManager = connectionManagerFactory
                    .newConnectionManager(httpClientProperties.isDisableSslValidation(), httpClientProperties.getMaxConnections(),
                            httpClientProperties.getMaxConnectionsPerRoute(),
                            httpClientProperties.getTimeToLive(),
                            httpClientProperties.getTimeToLiveUnit(), registryBuilder);
            this.connectionManagerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    connectionManager.closeExpiredConnections();
                }
            }, 30000, httpClientProperties.getConnectionTimerRepeat());
            return connectionManager;
        }

        @Bean
        public CloseableHttpClient httpClient(ApacheHttpClientFactory httpClientFactory,
                                              HttpClientConnectionManager httpClientConnectionManager,
                                              FeignHttpClientProperties httpClientProperties) {
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setConnectTimeout(httpClientProperties.getConnectionTimeout())
                    .setRedirectsEnabled(httpClientProperties.isFollowRedirects())
                    .build();
            this.httpClient = httpClientFactory.createBuilder().
                    setConnectionManager(httpClientConnectionManager).
                    setDefaultRequestConfig(defaultRequestConfig).build();
            return this.httpClient;
        }

        @Bean
        @ConditionalOnMissingBean(Client.class)
        public Client feignClient(HttpClient httpClient) {
            return new ApacheHttpClient(httpClient);
        }

        @PreDestroy
        public void destroy() throws Exception {
            connectionManagerTimer.cancel();
            if(httpClient != null) {
                httpClient.close();
            }
        }
    }

}
