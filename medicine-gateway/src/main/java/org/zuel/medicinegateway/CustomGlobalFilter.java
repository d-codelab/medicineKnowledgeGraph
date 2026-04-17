package org.zuel.medicinegateway;


import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.zuel.medicinegateway.UrlConstant.*;

/**
 * 全局过滤器实现业务逻辑
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisScript<Boolean> rateLimitScript;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";


    private static final Duration WINDOW_SIZE = Duration.ofHours(3);
    private static final int MAX_REQUESTS = 40;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("custom global filter");
        //1. 用户请求到API网关
        //2. 日志
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().toString();
        String path = request.getPath().value();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAddress = request.getRemoteAddress().getHostString();
        log.info("请求来源地址：" + sourceAddress);
        log.info("请求来源地址：" + request.getRemoteAddress());


        //3. PV、UV统计
        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        String pvKey = "pv:"+currentDate;
        String uvKey = "uv:"+currentDate;
        stringRedisTemplate.opsForValue().increment(pvKey);
        stringRedisTemplate.opsForHyperLogLog().add(uvKey,sourceAddress);
        if (path.equals(AI_PAGE)) {
                String pKey = "aiPage" + ":" + currentDate;
                stringRedisTemplate.opsForValue().increment(pKey);
        }else if (path.equals(HOME_PAGE)) {
                String pKey = "homePage" + ":" + currentDate;
                stringRedisTemplate.opsForValue().increment(pKey);
        }else if (path.equals(ARTICLE_PAGE)) {
                String pKey = "articlePage" + ":" + currentDate;
                stringRedisTemplate.opsForValue().increment(pKey);
        }
        //4. ip限流 滑动窗口+lua脚本
        //当前时间戳
        long now = System.currentTimeMillis();
        if (path.equals(QUESTION_API_PATH)) {
            String key = RATE_LIMIT_KEY_PREFIX + sourceAddress + ":" + path;
            //调用lua脚本获取限流结果
            Boolean isAccess = stringRedisTemplate.execute(
                    //lua限流脚本
                    rateLimitScript,
                    //限流资源名称
                    Collections.singletonList(key),
                    //限流大小
                    String.valueOf(MAX_REQUESTS),
                    //限流窗口的左区间
                    String.valueOf(now - WINDOW_SIZE.toMillis()),
                    //限流窗口的左区间
                    String.valueOf(now),
                    //id值，保证zset集合里面不重复，不然会覆盖
                    UUID.randomUUID().toString()
            );
            if (!isAccess){
                return rejectResponse(exchange.getResponse());
            }
        }
        return handleResponse(exchange, chain);
//        return chain.filter(exchange);
    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode =  originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

    private Mono<Void> rejectResponse(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}