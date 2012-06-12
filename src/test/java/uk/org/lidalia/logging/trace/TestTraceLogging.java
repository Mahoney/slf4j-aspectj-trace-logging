package uk.org.lidalia.logging.trace;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.org.lidalia.logging.test.ClassToBeTraced;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static uk.org.lidalia.slf4jtest.LoggingEvent.trace;

public class TestTraceLogging {


    private static ClassToBeTraced testInstance = new ClassToBeTraced();

	@Before public void setUp() {
		TestLoggerFactory.clear();
	}

    @Test
	public void testLogStatementsFromMethod() {
		testInstance.simpleMethod();

        assertEquals(asList(
                trace("> {aToStringValue}.simpleMethod()"),
                trace("< {aToStringValue}.simpleMethod")),
                getLoggingEvents("simpleMethod"));
    }

	@Test
	public void testLogStatementsFromStaticMethod() {
		ClassToBeTraced.simpleStaticMethod();

        assertEquals(asList(
                trace("> simpleStaticMethod()"),
                trace("< simpleStaticMethod")),
                getLoggingEvents("simpleStaticMethod"));
    }

	@Test
	public void testRuntimeExceptionLogStatements() {
		try {
			testInstance.runtimeExceptionMethod();
		} catch (RuntimeException re) {}

        assertEquals(asList(
                trace("> {aToStringValue}.runtimeExceptionMethod()"),
                trace("! {aToStringValue}.runtimeExceptionMethod threw exception java.lang.RuntimeException: message at line 29")),
                getLoggingEvents("runtimeExceptionMethod"));
    }

	@Test
	public void testCheckedExceptionLogStatements() {
		try {
			testInstance.exceptionMethod();
		} catch (Exception e) {}

        assertEquals(asList(
                trace("> {aToStringValue}.exceptionMethod()"),
                trace("! {aToStringValue}.exceptionMethod threw exception java.lang.Exception: message at line 33")),
                getLoggingEvents("exceptionMethod"));
    }

	@Test
	public void testAllVisibilitiesOfMethodsLogged() {
		testInstance.publicMethod();

        assertEquals(asList(
                trace("> {aToStringValue}.publicMethod()"),
                trace("< {aToStringValue}.publicMethod")),
                getLoggingEvents("publicMethod"));
		assertEquals(asList(
                trace("> {aToStringValue}.protectedMethod()"),
                trace("< {aToStringValue}.protectedMethod")),
                getLoggingEvents("protectedMethod"));
		assertEquals(asList(
                trace("> {aToStringValue}.defaultMethod()"),
                trace("< {aToStringValue}.defaultMethod")),
                getLoggingEvents("defaultMethod"));
		assertEquals(asList(
                trace("> {aToStringValue}.privateMethod()"),
                trace("< {aToStringValue}.privateMethod")),
                getLoggingEvents("privateMethod"));
    }

	@Test
	public void testParametersLogged() {
		testInstance.methodWithParameters("hello", 5);

        assertEquals(asList(
                trace("> {aToStringValue}.methodWithParameters(s=hello, i=5)"),
                trace("< {aToStringValue}.methodWithParameters")),
                getLoggingEvents("methodWithParameters"));
    }

	@Test
	public void testNullParametersHandledOK() {
		testInstance.methodWithParameters(null, 5);

        assertEquals(asList(
                trace("> {aToStringValue}.methodWithParameters(s=null, i=5)"),
                trace("< {aToStringValue}.methodWithParameters")),
                getLoggingEvents("methodWithParameters"));
    }

	@Test
	public void testReturnedObjectLogged() {
		testInstance.methodThatReturns();

        assertEquals(asList(
                trace("> {aToStringValue}.methodThatReturns()"),
                trace("< {aToStringValue}.methodThatReturns(hello)")),
                getLoggingEvents("methodThatReturns"));
    }

	@Test
	public void testNullReturnedObjectHandledOK() {
		testInstance.methodThatReturnsNull();

        assertEquals(asList(
                trace("> {aToStringValue}.methodThatReturnsNull()"),
                trace("< {aToStringValue}.methodThatReturnsNull(null)")),
                getLoggingEvents("methodThatReturnsNull"));
    }

	@Test
	public void testArrayParametersTurnedIntoSomethingLegible() {
		testInstance.methodWithArrayParameter(new String[] {"hello", "world"});

        assertEquals(asList(
                trace("> {aToStringValue}.methodWithArrayParameter(strings={hello,world})"),
                trace("< {aToStringValue}.methodWithArrayParameter")),
                getLoggingEvents("methodWithArrayParameter"));
    }

	@Test
	public void testReturnedArrayTurnedIntoSomethingLegible() {
		testInstance.methodWithArrayReturnType();

        assertEquals(asList(
                trace("> {aToStringValue}.methodWithArrayReturnType()"),
                trace("< {aToStringValue}.methodWithArrayReturnType({hello,world})")),
                getLoggingEvents("methodWithArrayReturnType"));
    }

	@Test
	public void testRuntimeExceptionInConstructorLogged() {
		try {
			new ClassToBeTraced("exceptionMessage");
		} catch (RuntimeException re) {}

        assertEquals(asList(
                trace("> ClassToBeTraced(message=exceptionMessage)"),
                trace("! ClassToBeTraced threw exception java.lang.RuntimeException: exceptionMessage at line 17")),
                getLoggingEvents("<init>"));
    }

	@Test
	public void testNestedExceptionsReportedWithCorrectLineNumbers() {
		try {
			testInstance.nestedExceptionMethod();
		} catch (RuntimeException re) {}

        assertEquals(asList(
                trace("> {aToStringValue}.nestedExceptionMethod()"),
                trace("! {aToStringValue}.nestedExceptionMethod threw exception java.lang.RuntimeException: message at line 72")),
                getLoggingEvents("nestedExceptionMethod"));
		assertEquals(asList(
                trace("> {aToStringValue}.runtimeExceptionMethod()"),
                trace("! {aToStringValue}.runtimeExceptionMethod threw exception java.lang.RuntimeException: message at line 29")),
                getLoggingEvents("runtimeExceptionMethod"));
    }

	@Test
	public void testFieldChangeLogged() {
		testInstance.setAField(2);

        assertEquals(asList(trace("= {aToStringValue}.aField [1] -> [2]")), getLoggingEvents("aField"));
    }

	@Test
	public void testSecureParameterEscapedInMethod() {
		testInstance.methodWithSecureParam("mypassword");

        assertEquals(asList(
                trace("> {aToStringValue}.methodWithSecureParam(password=****)"),
                trace("< {aToStringValue}.methodWithSecureParam")),
                getLoggingEvents("methodWithSecureParam"));
    }

    @Test
	public void testSecureParameterEscapedInConstructor() {
		new ClassToBeTraced("mypassword", "otherparam");

        assertEquals(asList(
                trace("> ClassToBeTraced(password=****, other=otherparam)"),
                trace("< ClassToBeTraced{aToStringValue}")),
                getLoggingEvents("<init>"));
    }

	@Test
	public void testSecureMethodsReturnValueEscaped() {
		testInstance.secureMethod();

        assertEquals(asList(
                trace("> {aToStringValue}.secureMethod()"),
                trace("< {aToStringValue}.secureMethod(****)")),
                getLoggingEvents("secureMethod"));
    }

	@Test
	public void testNullReturningMethod() {
		testInstance.nullReturningMethod();

        assertEquals(asList(
                trace("> {aToStringValue}.nullReturningMethod()"),
                trace("< {aToStringValue}.nullReturningMethod(null)")),
                getLoggingEvents("nullReturningMethod"));
    }

	@Test
	public void testSecureFieldsNotLogged() {
		testInstance.setSecureField("Hello World");

        assertEquals(asList(
                trace("= {aToStringValue}.secureField [****] -> [****]")),
                getLoggingEvents("secureField"));
    }

    private List<LoggingEvent> getLoggingEvents(String loggerName) {
        return TestLoggerFactory.getTestLogger(ClassToBeTraced.class.getName() + "." + loggerName).getLoggingEvents();
    }
}
