package net.test.advice;

import net.bytebuddy.asm.Advice;

import java.util.function.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ArgsAdvice {
  @Advice.OnMethodEnter
  public static void enter(@Advice.Origin String origin, @Advice.AllArguments Object[] args)
      throws Exception {
    System.out.println(origin + " Entering ");
    System.out.println(
        "TraceAgent (trace_args): `" + origin + " called with " + Arrays.toString(args));
  }

  @Advice.OnMethodExit(onThrowable = Throwable.class)
  public static void exit(@Advice.Origin String origin) {
    System.out.println(origin + " Exitting ");
  }
}
