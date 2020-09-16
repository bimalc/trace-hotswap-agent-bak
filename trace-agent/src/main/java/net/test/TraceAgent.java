package net.test;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.asm.Advice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

public class TraceAgent {

  private TraceAgentArgs traceAgentArgs;

  private Instrumentation instrumentation;

  private TraceAdvice readAction(String line) {
    String[] actionWithArgs = line.split("\\s+");
    final TraceAdvice traceAction;
    if (actionWithArgs.length == 4) {
      traceAction =
          new TraceAdvice(
              actionWithArgs[0], actionWithArgs[1], actionWithArgs[2], actionWithArgs[3]);
    } else if (actionWithArgs.length == 3) {
      traceAction = new TraceAdvice(actionWithArgs[0], actionWithArgs[1], actionWithArgs[2]);
    } else {
      traceAction = null;
    }
    return traceAction;
  }

  private static boolean isBlank(String line) {
    char[] chars = line.toCharArray();
    for (char c : chars) {
      if (c != ' ' && c != '\t') {
        return false;
      }
    }
    return true;
  }

  private static boolean isComment(String line) {
    return line.charAt(0) == '#';
  }

  private List<TraceAdvice> readActions(InputStream in) {
    List<TraceAdvice> actions = new ArrayList<TraceAdvice>();
    try {
      try (BufferedReader buffReader = new BufferedReader(new InputStreamReader(in))) {
        String line = null;
        while ((line = buffReader.readLine()) != null) {
          // blank lines and comments are skipped
          if (!isBlank(line) && !isComment(line)) {
            TraceAdvice traceAction = readAction(line);
            if (traceAction == null) {
              System.err.println("TraceAgent skips the rule: " + line);
            } else {
              actions.add(traceAction);
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    return actions;
  }

  private void installActions(List<TraceAdvice> actions) {

    for (TraceAdvice action : actions) {

      if (action.getAdviceName() != null) {
        if (action.getAdviceName().equals("log4jlevel"))
          LogManager.getRootLogger().setLevel(Level.DEBUG);

      } else {
        final Advice advice = action.getAdvice(traceAgentArgs);
        if (advice != null) {

          System.err.println(
              "Installing " + action.getClassMatcher() + ":" + action.getMethodMatcher());
          new AgentBuilder.Default()
              .disableClassFormatChanges()
              .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE) //
              .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
              .with(AgentBuilder.TypeStrategy.Default.REDEFINE) //
              .type(action.getClassMatcher())
              .transform(
                  (builder, type, classLoader, module) -> {
                    return builder.visit(advice.on(action.getMethodMatcher()));
                  })
              .installOn(instrumentation);
        }
      }
    }
  }

  private void install() {
    List<TraceAdvice> allActions =
        readActions(TraceAgent.class.getResourceAsStream("/actions.txt"));
    String externalActionFile = traceAgentArgs.getExternalActionFilePath();
    if (externalActionFile != null) {
      try {
        allActions.addAll(readActions(new FileInputStream(externalActionFile)));
      } catch (FileNotFoundException fnf) {
        System.err.println(
            "TraceAgent does not find the external action file: " + externalActionFile);
      }
    }
    installActions(allActions);
  }

  private TraceAgent(TraceAgentArgs traceAgentArgs, Instrumentation instrumentation) {
    this.traceAgentArgs = traceAgentArgs;
    this.instrumentation = instrumentation;
  }

  public static void premain(String arguments, Instrumentation instrumentation) {
    TraceAgentArgs traceAgentArgs = new TraceAgentArgs(arguments);
    TraceAgent traceAgent = new TraceAgent(traceAgentArgs, instrumentation);
    System.err.println("Calling install main");
    traceAgent.install();
  }

  public static void agentmain(String arguments, Instrumentation instrumentation) {
    premain(arguments, instrumentation);
  }
}
