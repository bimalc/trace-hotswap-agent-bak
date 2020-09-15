package net.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

 private static Logger LOGGER = LoggerFactory.getLogger(App.class);
 
  public static void main(String[] args) {
        TestClass t1 = new TestClass();
        TestClass2nd t2 = new TestClass2nd();
	t1.start();
	t2.start();
  }

  static class TestClass extends Thread {

    public void test() {
      LOGGER.debug("Function test() called");
      try {
        Thread.sleep(5000);
      }  catch (Exception e) {
      }
    } 

    public void run() {
      long startTime = System.currentTimeMillis();
      int i = 0;
      while ( i < 300) {
        LOGGER.info(getName() + ": New Thread calling test()" + i++);
        test();
      }
     }
  }

  static class TestClass2nd extends Thread {

    public void test2() {
      LOGGER.debug("Function test() called");
      try {
        Thread.sleep(5000);
      } catch (Exception e) {
      }
    }

    public int methodWithArgs(String str, int i) {
      LOGGER.debug("methodWithArgs");
      return 12;
    }
    public void run() {
        long startTime = System.currentTimeMillis();
        int i = 0;
        while ( i < 300) {
            LOGGER.info(getName() + ": New Thread calling test() and methodswithargs().." + i++);
		test2();
		methodWithArgs("First Argument", i);
        }
    }  

  }
}
