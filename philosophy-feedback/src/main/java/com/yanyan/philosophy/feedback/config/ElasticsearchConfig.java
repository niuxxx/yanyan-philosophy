package com.yanyan.philosophy.feedback.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by niuyan on 18/6/6.
 */
@Configuration
@ConditionalOnExpression("${elasticsearch.enable:true}")
public class ElasticsearchConfig {

    /**
     * es集群地址
     */
    @Value("${elasticsearch.cluster.nodes}")
    private String nodes;

    /**
     * 连接池
     */
    @Value("${elasticsearch.pool}")
    private String poolSize;

    /**
     * 用户名
     */
    @Value("${elasticsearch.username}")
    private String username;

    /**
     * 密码
     */
    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.connectTimeout}")
    private Integer connectTimeout;

    @Value("${elasticsearch.requestTimeout}")
    private Integer requestTimeout;

    @Value("${elasticsearch.socketTimeout}")
    private Integer socketTimeout;

    private RestClientBuilder builder() {

        RestClientBuilder builder = RestClient.builder(parseNodes());

        builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(requestTimeout)
                    .setSocketTimeout(socketTimeout)
                    .build();
            httpAsyncClientBuilder.setDefaultRequestConfig(requestConfig);
            return httpAsyncClientBuilder;
        });

        if (StringUtils.isNotBlank(username)) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider)
            );
        }
        return builder;
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(builder());
    }

    private HttpHost[] parseNodes() {
        String[] nodeArr = StringUtils.split(nodes, ",");
        HttpHost[] hosts = new HttpHost[nodeArr.length];
        int i = 0;
        for (String node : nodeArr) {
            String[] arrs = StringUtils.split(node, ":");
            hosts[i] = new HttpHost(arrs[0], Integer.parseInt(arrs[1]), "http");
            i++;
        }
        return hosts;
    }

}
