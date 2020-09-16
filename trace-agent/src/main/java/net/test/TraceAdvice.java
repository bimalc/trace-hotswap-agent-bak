package net.test;

import net.test.advice.*;
import net.bytebuddy.asm.Advice;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.util.Map;

class TraceAdvice {

  private final String actionId;

  private final String classMatcherExp;

  private final String methodMatcherExp;

  private final String actionArgs;

  public TraceAdvice(
      String actionId, String classMatcherExp, String methodMatcherExp, String actionArgs) {
    this.actionId = actionId;
    this.classMatcherExp = classMatcherExp;
    this.methodMatcherExp = methodMatcherExp;
    this.actionArgs = actionArgs;
  }

  public TraceAdvice(String actionId, String classMatcherExp, String methodMatcherExp) {
    this(actionId, classMatcherExp, methodMatcherExp, null);
  }

  public String getAdviceName() {
    if (actionId.equals("log4jlevel")) return actionId;
    else return null;
  }

  public Advice getAdvice(DefaultArguments defaultArguments) {
    final Advice advice;
    if (actionId.equals("elapsed_time_in_nano")) {
      advice = Advice.to(NanoTimeMeasurementAdvice.class);
    } else if (actionId.equals("elapsed_time_in_ms")) {
      advice = Advice.to(MsTimeMeasurementAdvice.class);
    } else if (actionId.equals("stack_trace")) {
      advice = Advice.to(StackAdvice.class);
    } else if (actionId.equals("trace_args")) {
      System.err.println("Setting trace args");
      advice = Advice.to(ArgsAdvice.class);
    } else if (actionId.equals("trace_retval")) {
      advice = Advice.to(RetvalAdvice.class);
    } else if (actionId.equals("counter")) {
      advice = Advice.to(CounterAdvice.class);
    } else {
      System.err.println("TraceAgent detected an invalid action: " + actionId);
      advice = null;
    }
    return advice;
  }

  public <T extends NamedElement> ElementMatcher.Junction<T> getClassMatcher() {
    return toMatcher(classMatcherExp);
  }

  public <T extends NamedElement> ElementMatcher.Junction<T> getMethodMatcher() {
    return toMatcher(methodMatcherExp);
  }

  private <T extends NamedElement> ElementMatcher.Junction<T> toMatcher(String inputExpression) {
    final ElementMatcher.Junction<T> res;
    int i = inputExpression.indexOf('(');
    if (i == -1) {
      res = named(inputExpression);
    } else {
      String matchFn = inputExpression.substring(0, i);
      String pattern = inputExpression.substring(i + 1, inputExpression.length() - 1);
      if (matchFn.equals("REGEXP")) {
        res = nameMatches(pattern);
      } else {
        res = named(pattern);
      }
    }
    return res;
  }
}
