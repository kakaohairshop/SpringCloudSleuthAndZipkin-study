package com.example.service;

import brave.*;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.internal.baggage.ExtraBaggageContext;
import brave.propagation.B3Propagation;
import brave.sampler.SamplerFunction;
import com.example.repository.FirstRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class FirstService {

    private static final String secondUri = "http://localhost:8081/second";
    private static final String thirdUri = "http://localhost:8082/third";
    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final CurrentSpanCustomizer currentSpanCustomizer;
    private final FirstRepository firstRepository;

    public String sendSecond() {
        log.info(">>> new span start ... ");
        String response = restTemplate.getForObject(secondUri + "/ping", String.class);
        log.info(">>> from second-point .... response : {}", response);
        log.info(">>> new span start ... ");
        response = restTemplate.getForObject(thirdUri + "/ping", String.class);
        log.info(">>> from second-point .... response : {}", response);
        return "finish";
    }

    public String sendSecondAndThird() {
        firstRepository.findAny();
        log.info(">>> new span start ... ");
        String response = restTemplate.getForObject(secondUri + "/send-third", String.class);
        log.info(">>> from second-point .... response : {}", response);

        log.info(">>> new span start 2 ... ");
        response = restTemplate.getForObject(secondUri + "/send-third", String.class);
        log.info(">>> from second-point 2 .... response : {}", response);

        log.info(">>> new span start 3 ... ");
        response = restTemplate.getForObject(thirdUri + "/ping", String.class);
//        response = restTemplate.getForObject(thirdUri + "/error", String.class);
        log.info(">>> from third-point .... response : {}", response);

        return "finish";
    }

    public String createNewTracer(){
        log.info(">>> first service");
        Span newSpan = tracer.newTrace().name("newSpan").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            log.info(">>> new span start with new tracer ");
            String response = restTemplate.getForObject(secondUri + "/ping", String.class);
            log.info(">>> from second-point .... response : {}", response);
            return "finish";
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            newSpan.finish();
        }
        return "error!!";
    }

    public void sendSecondForErrorTest() {
        log.info(">>> first service");
        Span nextSpan = tracer.nextSpan();
        log.info(">>> create new span");
        tracer.withSpanInScope(nextSpan);
        log.info(">>> create new span in scope");
        restTemplate.getForObject(secondUri + "/error", String.class);
    }

    public void addSpan() {
        log.info(">>> first service ... ");

        Span newSpan = tracer.newTrace().name("newSpan").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            log.info(">>> new span start ... ");
        } finally {
            newSpan.finish();
        }
    }

    public void nextSpan() {
        log.info(">>> first service ... ");
        SpanCustomizer spanCustomizer = tracer.nextSpan();
        spanCustomizer.name("my new span");
        Span nextSpan = (Span) spanCustomizer;
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(nextSpan.start())) {
            currentSpanCustomizer.tag("customizer", "true");
            log.info(">>> next span start ... ");
        } finally {
            nextSpan.finish();
        }
    }

    @NewSpan("addTagSpan")
    public void addTag(@SpanTag(key = "zeroTag", expression = "'hello characters'") String tag) {
        log.info(">>> first service ... ");
        Span span = tracer.currentSpan();
        span.tag("firstTag", "hello world");
        span.tag("secondTag", "sleuth example");
        restTemplate.getForObject(secondUri + "/ping", String.class);
    }

    public void addBaggage(String baggage) {
        log.info(">>> first service ... ");
        log.info(">>> client baggage : {}", baggage);

        for (BaggageField baggageField : ExtraBaggageContext.getAllFields(tracer.currentSpan().context())) {
            log.info(">>> baggage : {} - {}",baggageField.name(), baggageField.getValue());
        }

        Tracing tracing = Tracing.newBuilder().currentTraceContext(Tracing.current().currentTraceContext()).propagationFactory(
                BaggagePropagation.newFactoryBuilder(B3Propagation.FACTORY)
                        .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create("first-bag")))
                        .add(BaggagePropagationConfig.SingleBaggageField.newBuilder(BaggageField.create("second-bag")).addKeyName("SECOND_KEY").build())
                        .add(BaggagePropagationConfig.SingleBaggageField.local(BaggageField.create("user-local")))
                        .add(BaggagePropagationConfig.SingleBaggageField.remote(BaggageField.create("x-vcap-request-id")))
                        .build()
        ).build();


        Span updatedSpan = tracing.tracer().currentSpan();
        List<BaggageField> baggageFields = ExtraBaggageContext.getAllFields(updatedSpan.context());
        for (BaggageField baggageField : baggageFields) {
            log.info(">>> baggage : {} - {}",baggageField.name(), baggageField.getValue());
        }

        restTemplate.getForObject(secondUri+"/baggage", Object.class);
    }

    public void sampler() {
        log.info(">>> add sampler header!");
        Span span = tracer.currentSpan();

        SamplerFunction<Boolean> samplerFunction = new SamplerFunction() {
            @Override
            public Boolean trySample(Object arg) {
                return (boolean) arg;
            }
        };


        // Span nextSpan = tracer.nextSpanWithParent(samplerFunction, false, span.context());
        ScopedSpan nextSpan = tracer.startScopedSpan("my new span", new SamplerFunction() {
            @Override
            public Boolean trySample(Object arg) {
                return (boolean) arg;
            }
        }, false);

        log.info(">>> next span {} ", nextSpan.context().spanIdString());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("X-B3-Sampled", "0");
        httpHeaders.set("test", "hello world!");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> result = restTemplate.postForEntity(secondUri+"/ping", request, String.class);
        log.info(">>> first service ... ping result : {} ", result.getBody());
    }
}
