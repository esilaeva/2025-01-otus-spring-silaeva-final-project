package ru.otus.hw.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingServerInterceptor implements ServerInterceptor {

    @Override
    public <I, O> ServerCall.Listener<I> interceptCall(ServerCall<I, O> call,
                                                                 Metadata header,
                                                                 ServerCallHandler<I, O> next) {

        log.info("✅ Invoked - {}", call.getMethodDescriptor().getFullMethodName());
        return next.startCall(call, header);
    }
}
