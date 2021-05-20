package com.example.service;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracer;
import brave.baggage.BaggageField;
import brave.internal.baggage.ExtraBaggageContext;
import com.example.repository.ThirdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class ThirdService {

    private final Tracer tracer;
    private final ThirdRepository thirdRepository;

    public String ping(){
        log.info(">>> third service ... end");
        return "request ping success!! " + thirdRepository.findAny();
    }

    public String createError() {
        log.info(">>> third service ... ");
        throw new RuntimeException("sleuth error log test");
    }

    public void findBaggage() {
        Span updatedSpan = tracer.currentSpan();
        Long parentId = updatedSpan.context().parentId();
        log.info("parents Id : {}", parentId);
        List<BaggageField> baggageFields = ExtraBaggageContext.getAllFields(updatedSpan.context());
        for (BaggageField baggageField : baggageFields) {
            log.info(">>> third span.. baggage : {} - {}",baggageField.name(), baggageField.getValue());
        }
    }

}
