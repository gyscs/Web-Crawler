package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = delegate;
    this.state = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result = null;
    Instant start = null;
    if(method.isAnnotationPresent(Profiled.class)) {
      start = clock.instant();
    }
    try {
      result = method.invoke(delegate, args);
    } catch (Throwable t) {
      throw t.getCause();
    } finally {
      if(method.isAnnotationPresent(Profiled.class)) {
        state.record(delegate.getClass(), method, Duration.between(start, clock.instant()));
      }
    }
    return result;
  }
}
