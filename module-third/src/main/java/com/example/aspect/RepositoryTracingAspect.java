package com.example.aspect;

import brave.ScopedSpan;
import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RepositoryTracingAspect {

    private final Tracer tracer;

    @Around("execution(* com.example.repository.*.*(..))")
    public Object repositoryTracingHandler(ProceedingJoinPoint pjp) throws Throwable {
        log.info(">>> third service");
        tracer.nextSpan().name("repositorySpan-third").start();
        ScopedSpan newSpan = tracer.startScopedSpan("repositorySpan-third");
        Object result = pjp.proceed();
        newSpan.tag("className", pjp.getSignature().getDeclaringTypeName());
        newSpan.tag("methodName", pjp.getSignature().getName());
        log.info("finished >>> " + pjp.getSignature().getDeclaringTypeName() + " / " + pjp.getSignature().getName());
        newSpan.finish();
        return result;
    }
}
