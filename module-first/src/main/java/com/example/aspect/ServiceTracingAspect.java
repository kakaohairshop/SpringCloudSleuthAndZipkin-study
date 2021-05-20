package com.example.aspect;

import brave.ScopedSpan;
import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ServiceTracingAspect {

    private final Tracer tracer;

    @Around("execution(* com.example.service.*.*(..))")
    public Object serviceTracingHandler(ProceedingJoinPoint pjp) throws Throwable {
        log.info(">>> first service");
        tracer.nextSpan().name("serviceSpan-first").start();
        ScopedSpan newSpan = tracer.startScopedSpan("serviceSpan-first");
        try {
            Object result = pjp.proceed();
            log.info("finished - " + pjp.getSignature().getDeclaringTypeName() + " / " + pjp.getSignature().getName());
            return result;
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            newSpan.tag("className", pjp.getSignature().getDeclaringTypeName());
            newSpan.tag("methodName", pjp.getSignature().getName());
            newSpan.finish();
        }
        return "fail..";
    }
}
