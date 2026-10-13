package com.sports.common.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class VisitorCounterService {

    private final AtomicLong visitorCount = new AtomicLong(1250);

    public long incrementAndGet() {
        return visitorCount.incrementAndGet();
    }

    public long getCount() {
        return visitorCount.get();
    }
}
