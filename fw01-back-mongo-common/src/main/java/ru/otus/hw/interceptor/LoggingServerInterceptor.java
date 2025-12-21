package ru.otus.hw.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata header,
                                                                 ServerCallHandler<ReqT, RespT> next) {

        log.info("✅ Invoked - {}", call.getMethodDescriptor().getFullMethodName());
        return next.startCall(call, header);
    }
}
