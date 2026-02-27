package com.scheduler.kis_client.middleware;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.scheduler.kis_client.KisClient;
import com.scheduler.kis_client.api.ApiResult;
import com.scheduler.kis_client.api.rest.PageableApiResult;
import com.scheduler.kis_client.config.Credentials;
import com.scheduler.kis_client.context.ApiContext;
import com.scheduler.kis_client.context.ApiData;
import com.scheduler.kis_client.util.Pair;
import com.scheduler.kis_client.util.ReflectionUtil;

public class PageableResultMiddleware implements Middleware {

    @Override
    public void afterInit(KisClient client, ApiContext context) {
        // nothing to do
    }

    @Override
    public void before(KisClient client, ApiContext context) {
        // nothing to do
    }

    @Override
    public void after(KisClient client, ApiContext context) {
        ApiResult result = context.getApiResult();

        if (!(result instanceof PageableApiResult)) {
            return;
        }

        ApiData apiData = context.getApiData();
        Credentials credentials = context.getCredentials();

        try {
            Field nextFunctionField = ReflectionUtil.getFieldFromClassHierarchy(
                result.getClass(),
                "nextFunction"
            );

            // 익명 클래스에서는 다이아몬드 연산자(<>) 사용 불가 → 타입 명시
            nextFunctionField.set(result, new Supplier<ApiResult>() {
                @Override
                public ApiResult get() {
                    ApiData clonedApiData = apiData.clone();

                    PageableApiResult<?> pageableResult = (PageableApiResult<?>) result;

                    List<Pair<String, String>> values = List.of(
                        new Pair<>("TR_CONT", "N"),
                        new Pair<>("CTX_AREA_FK", pageableResult.getCtxAreaFk()),
                        new Pair<>("CTX_AREA_NK", pageableResult.getCtxAreaNk()),
                        new Pair<>("CTX_AREA_FK100", pageableResult.getCtxAreaFk100()),
                        new Pair<>("CTX_AREA_NK100", pageableResult.getCtxAreaNk100()),
                        new Pair<>("CTX_AREA_FK200", pageableResult.getCtxAreaFk200()),
                        new Pair<>("CTX_AREA_NK200", pageableResult.getCtxAreaNk200())
                    );

                    List<Map<String, Object>> maps = List.of(
                        clonedApiData.getHeaders(),
                        clonedApiData.getBody(),
                        clonedApiData.getParameters()
                    );

                    for (Pair<String, String> entry : values) {
                        String key = entry.getFirst();
                        String value = entry.getSecond();

                        if (value == null || value.isEmpty()) {
                            continue;
                        }

                        for (Map<String, Object> map : maps) {
                            if (map.containsKey(key)) {
                                map.put(key, value);
                            } else {
                                String lowerKey = key.toLowerCase();
                                if (map.containsKey(lowerKey)) {
                                    map.put(lowerKey, value);
                                }
                            }
                        }
                    }

                    return client.execute(clonedApiData, credentials);
                }
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 프로젝트에 맞게 필요하면 여기서 로깅 추가
            // 예: Logger 사용 등
        }
    }

}
