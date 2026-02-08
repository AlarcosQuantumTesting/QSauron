package edu.uclm.reper.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
@RequestMapping("system")
public class SystemController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/endpoints")
    public List<Map<String, Object>> getEndpoints() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();

        map.forEach((info, method) -> {
            Set<String> patterns = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

            if (!patterns.isEmpty()) {
                for (String pattern : patterns) {
                    Map<String, Object> endpoint = new HashMap<>();
                    endpoint.put("path", pattern);
                    endpoint.put("methods", methods.stream().map(Enum::name).collect(Collectors.toList()));
                    endpoints.add(endpoint);
                }
            }
        });

        return endpoints;
    }
}
