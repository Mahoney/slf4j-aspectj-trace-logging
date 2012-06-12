package uk.org.lidalia.logging.trace;

import static org.junit.Assert.*;
import static uk.org.lidalia.slf4jtest.LoggingEvent.trace;

import java.util.List;

import org.junit.Before;
import org.junit.Test;


import uk.org.lidalia.logging.test.ClassToBeTraced;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

public class TestTraceLogging {


    private static ClassToBeTraced testInstance = new ClassToBeTraced();

	@Before public void setUp() {
		TestLoggerFactory.clear();
	}

    @Test
	public void testLogStatementsFromMethod() {
		testInstance.simpleMethod();

        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.simpleMethod");
        assertEquals(trace("> {aToStringValue}.simpleMethod()"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.simpleMethod"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testLogStatementsFromStaticMethod() {
		ClassToBeTraced.simpleStaticMethod();

        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.simpleStaticMethod");
		assertEquals(trace("> simpleStaticMethod()"), loggingEvents.get(0));
		assertEquals(trace("< simpleStaticMethod"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testRuntimeExceptionLogStatements() {

		try {
			testInstance.runtimeExceptionMethod();
		} catch (RuntimeException re) {}

        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.runtimeExceptionMethod");
		assertEquals(trace("> {aToStringValue}.runtimeExceptionMethod()"), loggingEvents.get(0));
		assertEquals(trace("! {aToStringValue}.runtimeExceptionMethod threw exception java.lang.RuntimeException: message at line 29"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testCheckedExceptionLogStatements() {
		try {
			testInstance.exceptionMethod();
		} catch (Exception e) {}
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.exceptionMethod");
		assertEquals(trace("> {aToStringValue}.exceptionMethod()"), loggingEvents.get(0));
		assertEquals(trace("! {aToStringValue}.exceptionMethod threw exception java.lang.Exception: message at line 33"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testAllVisibilitiesOfMethodsLogged() {
		testInstance.publicMethod();
        List<LoggingEvent> publicMethodLoggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.publicMethod");
        List<LoggingEvent> protectedMethodLoggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.protectedMethod");
        List<LoggingEvent> defaultMethodLoggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.defaultMethod");
        List<LoggingEvent> privateMethodLoggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.privateMethod");
		assertEquals(trace("> {aToStringValue}.publicMethod()"), publicMethodLoggingEvents.get(0));
		assertEquals(trace("> {aToStringValue}.protectedMethod()"), protectedMethodLoggingEvents.get(0));
		assertEquals(trace("> {aToStringValue}.defaultMethod()"), defaultMethodLoggingEvents.get(0));
		assertEquals(trace("> {aToStringValue}.privateMethod()"), privateMethodLoggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.privateMethod"), privateMethodLoggingEvents.get(1));
		assertEquals(trace("< {aToStringValue}.defaultMethod"), defaultMethodLoggingEvents.get(1));
		assertEquals(trace("< {aToStringValue}.protectedMethod"), protectedMethodLoggingEvents.get(1));
		assertEquals(trace("< {aToStringValue}.publicMethod"), publicMethodLoggingEvents.get(1));
        assertEquals(2, publicMethodLoggingEvents.size());
        assertEquals(2, protectedMethodLoggingEvents.size());
        assertEquals(2, defaultMethodLoggingEvents.size());
        assertEquals(2, privateMethodLoggingEvents.size());
    }

	@Test
	public void testParametersLogged() {
		testInstance.methodWithParameters("hello", 5);
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodWithParameters");
		assertEquals(trace("> {aToStringValue}.methodWithParameters(s=hello, i=5)"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.methodWithParameters"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testNullParametersHandledOK() {
		testInstance.methodWithParameters(null, 5);
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodWithParameters");
		assertEquals(trace("> {aToStringValue}.methodWithParameters(s=null, i=5)"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.methodWithParameters"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testReturnedObjectLogged() {
		testInstance.methodThatReturns();
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodThatReturns");
        assertEquals(trace("> {aToStringValue}.methodThatReturns()"), loggingEvents.get(0));
        assertEquals(trace("< {aToStringValue}.methodThatReturns(hello)"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testNullReturnedObjectHandledOK() {
		testInstance.methodThatReturnsNull();
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodThatReturnsNull");
        assertEquals(trace("> {aToStringValue}.methodThatReturnsNull()"), loggingEvents.get(0));
        assertEquals(trace("< {aToStringValue}.methodThatReturnsNull(null)"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testArrayParametersTurnedIntoSomethingLegible() {
		testInstance.methodWithArrayParameter(new String[] {"hello", "world"});
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodWithArrayParameter");
		assertEquals(trace("> {aToStringValue}.methodWithArrayParameter(strings={hello,world})"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.methodWithArrayParameter"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testReturnedArrayTurnedIntoSomethingLegible() {
		testInstance.methodWithArrayReturnType();
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodWithArrayReturnType");
		assertEquals(trace("> {aToStringValue}.methodWithArrayReturnType()"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.methodWithArrayReturnType({hello,world})"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testRuntimeExceptionInConstructorLogged() {
		try {
			new ClassToBeTraced("exceptionMessage");
		} catch (RuntimeException re) {}
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.<init>");
		assertEquals(trace("> ClassToBeTraced(message=exceptionMessage)"), loggingEvents.get(0));
		assertEquals(trace("! ClassToBeTraced threw exception java.lang.RuntimeException: exceptionMessage at line 17"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testNestedExceptionsReportedWithCorrectLineNumbers() {
		try {
			testInstance.nestedExceptionMethod();
		} catch (RuntimeException re) {}
        List<LoggingEvent> loggingEvents1 = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.nestedExceptionMethod");
        List<LoggingEvent> loggingEvents2 = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.runtimeExceptionMethod");
		assertEquals(trace("> {aToStringValue}.nestedExceptionMethod()"), loggingEvents1.get(0));
		assertEquals(trace("> {aToStringValue}.runtimeExceptionMethod()"), loggingEvents2.get(0));
		assertEquals(trace("! {aToStringValue}.runtimeExceptionMethod threw exception java.lang.RuntimeException: message at line 29"), loggingEvents2.get(1));
		assertEquals(trace("! {aToStringValue}.nestedExceptionMethod threw exception java.lang.RuntimeException: message at line 72"), loggingEvents1.get(1));
        assertEquals(2, loggingEvents1.size());
        assertEquals(2, loggingEvents2.size());
    }

	@Test
	public void testFieldChangeLogged() {
		testInstance.setAField(2);
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.aField");
		assertEquals(trace("= {aToStringValue}.aField [1] -> [2]"), loggingEvents.get(0));
        assertEquals(1, loggingEvents.size());
    }

	@Test
	public void testSecureParameterEscapedInMethod() {
		testInstance.methodWithSecureParam("mypassword");
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.methodWithSecureParam");
		assertEquals(trace("> {aToStringValue}.methodWithSecureParam(password=****)"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.methodWithSecureParam"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

    @Test
	public void testSecureParameterEscapedInConstructor() {
		new ClassToBeTraced("mypassword", "otherparam");
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.<init>");
		assertEquals(trace("> ClassToBeTraced(password=****, other=otherparam)"), loggingEvents.get(0));
		assertEquals(trace("< ClassToBeTraced{aToStringValue}"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testSecureMethodsReturnValueEscaped() {
		testInstance.secureMethod();
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.secureMethod");
		assertEquals(trace("> {aToStringValue}.secureMethod()"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.secureMethod(****)"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testNullReturningMethod() {
		testInstance.nullReturningMethod();
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.nullReturningMethod");
		assertEquals(trace("> {aToStringValue}.nullReturningMethod()"), loggingEvents.get(0));
		assertEquals(trace("< {aToStringValue}.nullReturningMethod(null)"), loggingEvents.get(1));
        assertEquals(2, loggingEvents.size());
    }

	@Test
	public void testSecureFieldsNotLogged() {
		testInstance.setSecureField("Hello World");
        List<LoggingEvent> loggingEvents = getLoggingEvents("uk.org.lidalia.logging.test.ClassToBeTraced.secureField");
        assertEquals(trace("= {aToStringValue}.secureField [****] -> [****]"), loggingEvents.get(0));
        assertEquals(1, loggingEvents.size());
    }

    private List<LoggingEvent> getLoggingEvents(String loggerName) {
        return TestLoggerFactory.getTestLogger(loggerName).getLoggingEvents();
    }
}
