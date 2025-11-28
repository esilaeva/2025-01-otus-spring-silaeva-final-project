package ru.otus.hw.interceptor;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

/**
 * Client interceptor for comprehensive request/response logging.
 * Provides detailed logging of gRPC calls including timing and error information.
 */
@Slf4j
public class LoggingClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            private long startTime;

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                startTime = System.currentTimeMillis();
                log.debug("✅ Starting gRPC call: method={}, headers={}",
                        method.getFullMethodName(), headers);

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onMessage(RespT message) {
                        log.debug("✅ Received response for {}: {}",
                                method.getFullMethodName(), message);
                        super.onMessage(message);
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        long duration = System.currentTimeMillis() - startTime;

                        if (status.isOk()) {
                            log.info("✅ gRPC call completed successfully: method={}, duration={}ms",
                                    method.getFullMethodName(), duration);
                        } else {
                            log.error("✅ gRPC call failed: method={}, status={}, duration={}ms, description={}",
                                    method.getFullMethodName(), status.getCode(), duration, status.getDescription());
                        }

                        super.onClose(status, trailers);
                    }
                }, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                log.debug("✅ Sending request for {}: {}",
                        method.getFullMethodName(), message);
                super.sendMessage(message);
            }
        };
    }
}
