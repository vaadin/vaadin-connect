package com.vaadin.services;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VaadinServiceController {
    private final Map<String, VaadinServiceData> vaadinServices = new HashMap<>();

    private static class VaadinServiceData {
        private final Object vaadinServiceClass;
        private final Map<String, Method> methods = new HashMap<>();

        private VaadinServiceData(Object vaadinServiceClass, Method... serviceMethods) {
            this.vaadinServiceClass = vaadinServiceClass;
            Stream.of(serviceMethods)
                    .filter(method -> method.getDeclaringClass() != Object.class)
                    .forEach(method -> methods.put(method.getName(), method));
        }
    }

    @Autowired
    public VaadinServiceController(ApplicationContext context) {
        context.getBeansWithAnnotation(VaadinService.class).forEach((name, serviceBean) -> {
            // Check the bean type instead of the implementation type in
            // case of e.g. proxies
            Class<?> beanType = context.getType(name);
            if (beanType == null) {
                throw new IllegalStateException("WTF?");
            }

            String serviceName = beanType.getSimpleName();
            if (serviceName.isEmpty()) {
                throw new IllegalStateException("Anonymous class as a VaadinService declared, wtf?");
            }

            vaadinServices.put(serviceName, new VaadinServiceData(serviceBean, beanType.getMethods()));
        });

        System.out.println("=======");
        System.out.println(vaadinServices.keySet());
        System.out.println("=======");
    }

    // curl -H "Content-Type: application/json" -d "" http://localhost:8080/ser/var
    @PostMapping(path = "/{service}/{method}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String serveVaadinService(@PathVariable("service") String serviceName,
                                     @PathVariable("method") String methodName) {
        return "Service stub";
    }
}
