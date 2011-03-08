package uk.org.lidalia.logging.trace;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import uk.org.lidalia.logging.test.ClassToBeTraced;

public class TestTraceLogging {
	
	private static LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	private ListAppender<LoggingEvent> appender;
	private static Logger log = lc.getLogger(ClassToBeTraced.class);
	private static ClassToBeTraced testInstance = new ClassToBeTraced();

	static {
		BasicConfigurator.configure(lc);
		lc.getLogger(LoggerContext.ROOT_NAME).setLevel(Level.ERROR);
		log.setAdditive(false);
	}
	
	@Before public void setUp() {
		log.setLevel(Level.TRACE);
		log.detachAndStopAllAppenders();
		appender = new ListAppender<LoggingEvent>();
		appender.setContext(lc);
		appender.start();
		log.addAppender(appender);
	}
	
	public void testNumberAndLevelOfAppenders(List<LoggingEvent> events, int numberExpected) {
		assertEquals(numberExpected, events.size());
		for (LoggingEvent loggingEvent : events) {
			assertEquals(Level.TRACE, loggingEvent.getLevel());
		}
	}
	
	@Test
	public void testLogStatementsFromMethod() {
		testInstance.simpleMethod();
		assertEquals("> {aToStringValue}.simpleMethod()", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.simpleMethod", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}

	@Test
	public void testLogStatementsFromStaticMethod() {
		ClassToBeTraced.simpleStaticMethod();
		assertEquals("> simpleStaticMethod()", appender.list.get(0).getMessage());
		assertEquals("< simpleStaticMethod", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testRuntimeExceptionLogStatements() {
		
		try {
			testInstance.runtimeExceptionMethod();
		} catch (RuntimeException re) {}
		assertEquals("> {aToStringValue}.runtimeExceptionMethod()", appender.list.get(0).getMessage());
		assertEquals("! {aToStringValue}.runtimeExceptionMethod threw exception java.lang.RuntimeException: message at line 29", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testCheckedExceptionLogStatements() {
		try {
			testInstance.exceptionMethod();
		} catch (Exception e) {}
		assertEquals("> {aToStringValue}.exceptionMethod()", appender.list.get(0).getMessage());
		assertEquals("! {aToStringValue}.exceptionMethod threw exception java.lang.Exception: message at line 33", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testAllVisibilitiesOfMethodsLogged() {
		testInstance.publicMethod();
		assertEquals("> {aToStringValue}.publicMethod()", appender.list.get(0).getMessage());
		assertEquals("> {aToStringValue}.protectedMethod()", appender.list.get(1).getMessage());
		assertEquals("> {aToStringValue}.defaultMethod()", appender.list.get(2).getMessage());
		assertEquals("> {aToStringValue}.privateMethod()", appender.list.get(3).getMessage());
		assertEquals("< {aToStringValue}.privateMethod", appender.list.get(4).getMessage());
		assertEquals("< {aToStringValue}.defaultMethod", appender.list.get(5).getMessage());
		assertEquals("< {aToStringValue}.protectedMethod", appender.list.get(6).getMessage());
		assertEquals("< {aToStringValue}.publicMethod", appender.list.get(7).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 8);
	}
	
	@Test
	public void testParametersLogged() {
		testInstance.methodWithParameters("hello", 5);
		assertEquals("> {aToStringValue}.methodWithParameters(s=hello, i=5)", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.methodWithParameters", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testNullParametersHandledOK() {
		testInstance.methodWithParameters(null, 5);
		assertEquals("> {aToStringValue}.methodWithParameters(s=null, i=5)", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.methodWithParameters", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testReturnedObjectLogged() {
		testInstance.methodThatReturns();
			assertEquals("> {aToStringValue}.methodThatReturns()", appender.list.get(0).getMessage());
			assertEquals("< {aToStringValue}.methodThatReturns(hello)", appender.list.get(1).getMessage());
			testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testNullReturnedObjectHandledOK() {
		testInstance.methodThatReturnsNull();
			assertEquals("> {aToStringValue}.methodThatReturnsNull()", appender.list.get(0).getMessage());
			assertEquals("< {aToStringValue}.methodThatReturnsNull(null)", appender.list.get(1).getMessage());
			testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testArrayParametersTurnedIntoSomethingLegible() {
		testInstance.methodWithArrayParameter(new String[] {"hello", "world"});
		assertEquals("> {aToStringValue}.methodWithArrayParameter(strings={hello,world})", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.methodWithArrayParameter", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testReturnedArrayTurnedIntoSomethingLegible() {
		testInstance.methodWithArrayReturnType();
		assertEquals("> {aToStringValue}.methodWithArrayReturnType()", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.methodWithArrayReturnType({hello,world})", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testRuntimeExceptionInConstructorLogged() {
		Logger fieldLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.aField");
		fieldLog.setLevel(Level.DEBUG);
		Logger secureFieldLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.secureField");
		secureFieldLog.setLevel(Level.DEBUG);
		
		try {
			new ClassToBeTraced("exceptionMessage");
		} catch (RuntimeException re) {}
		assertEquals("> ClassToBeTraced(message=exceptionMessage)", appender.list.get(0).getMessage());
		assertEquals("! ClassToBeTraced threw exception java.lang.RuntimeException: exceptionMessage at line 17", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
		
		fieldLog.setLevel(null);
		secureFieldLog.setLevel(null);
	}
	
	@Test
	public void testNestedExceptionsReportedWithCorrectLineNumbers() {
		try {
			testInstance.nestedExceptionMethod();
		} catch (RuntimeException re) {}
		assertEquals("> {aToStringValue}.nestedExceptionMethod()", appender.list.get(0).getMessage());
		assertEquals("> {aToStringValue}.runtimeExceptionMethod()", appender.list.get(1).getMessage());
		assertEquals("! {aToStringValue}.runtimeExceptionMethod threw exception java.lang.RuntimeException: message at line 29", appender.list.get(2).getMessage());
		assertEquals("! {aToStringValue}.nestedExceptionMethod threw exception java.lang.RuntimeException: message at line 72", appender.list.get(3).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 4);
	}
	
	@Test
	public void testLoggerIncludesMethodName() {
		log.setLevel(Level.DEBUG);
		Logger methodLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.simpleMethod");
		methodLog.setLevel(Level.TRACE);
		
		testInstance.methodThatReturns();
		assertTrue(appender.list.isEmpty());
		
		testInstance.simpleMethod();
		assertEquals("> {aToStringValue}.simpleMethod()", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.simpleMethod", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
		
		methodLog.setLevel(null);
	}
	
	@Test
	public void testLoggerIncludesInitOnConstructor() {
		log.setLevel(Level.DEBUG);
		Logger constructorLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.<init>");
		constructorLog.setLevel(Level.TRACE);
		
		testInstance.methodThatReturns();
		assertTrue(appender.list.isEmpty());
		
		new ClassToBeTraced();
		assertEquals("> ClassToBeTraced()", appender.list.get(0).getMessage());
		assertEquals("< ClassToBeTraced{aToStringValue}", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
		constructorLog.setLevel(null);
	}
	
	@Test
	public void testFieldChangeLogged() {
		log.setLevel(Level.DEBUG);
		Logger fieldLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.aField");
		fieldLog.setLevel(Level.TRACE);
		
		testInstance.setAField(2);
		assertEquals("= {aToStringValue}.aField [1] -> [2]", appender.list.get(0).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 1);
		
		fieldLog.setLevel(null);
	}
	
	@Test
	public void testSecureParameterEscapedInMethod() {
		testInstance.methodWithSecureParam("mypassword");
		assertEquals("> {aToStringValue}.methodWithSecureParam(password=****)", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.methodWithSecureParam", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testSecureParameterEscapedInConstructor() {
		Logger fieldLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.aField");
		fieldLog.setLevel(Level.DEBUG);
		Logger secureFieldLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.secureField");
		secureFieldLog.setLevel(Level.DEBUG);
		
		new ClassToBeTraced("mypassword", "otherparam");
		assertEquals("> ClassToBeTraced(password=****, other=otherparam)", appender.list.get(0).getMessage());
		assertEquals("< ClassToBeTraced{aToStringValue}", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
		
		fieldLog.setLevel(null);
		secureFieldLog.setLevel(null);
	}
	
	@Test
	public void testSecureMethodsReturnValueEscaped() {
		testInstance.secureMethod();
		assertEquals("> {aToStringValue}.secureMethod()", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.secureMethod(****)", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testNullReturningMethod() {
		testInstance.nullReturningMethod();
		assertEquals("> {aToStringValue}.nullReturningMethod()", appender.list.get(0).getMessage());
		assertEquals("< {aToStringValue}.nullReturningMethod(null)", appender.list.get(1).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 2);
	}
	
	@Test
	public void testSecureFieldsNotLogged() {
		log.setLevel(Level.DEBUG);
		Logger fieldLog = (Logger) LoggerFactory.getLogger("uk.org.lidalia.logging.test.ClassToBeTraced.secureField");
		fieldLog.setLevel(Level.TRACE);
		
		testInstance.setSecureField("Hello World");
		assertEquals("= {aToStringValue}.secureField [****] -> [****]", appender.list.get(0).getMessage());
		testNumberAndLevelOfAppenders(appender.list, 1);
		
		fieldLog.setLevel(null);
	}
}
