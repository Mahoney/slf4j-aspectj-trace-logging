package uk.org.lidalia.logging.trace;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.io.InputStream;
import java.io.IOException;

import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.lidalia.lang.Secure;

public aspect TraceAspect {

	pointcut externalClasses()					: !within(org.aspectj..*) && !within(uk.org.lidalia.logging.trace..*);
	pointcut toStringMethodExecution()			: execution(public String *.toString());
	pointcut methodExecution()					: execution(* *(..));
	pointcut allButToStringMethodExecution()	: methodExecution() && !toStringMethodExecution();
	pointcut constructorExecution()				: execution(new(..));
	pointcut fieldChange()						: set(* *.*);

	before() : externalClasses() && allButToStringMethodExecution() {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled())  log.trace(buildMethodEntryMessage(thisJoinPoint));
	}

	after() returning(Object returnValue) : externalClasses() && allButToStringMethodExecution() {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled())  log.trace(buildMethodExitMessage(thisJoinPoint, returnValue));
	}

	after() throwing(Throwable t) : externalClasses() && methodExecution() {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled()) log.trace(buildMethodExceptionThrownMessage(thisJoinPoint, t));
	}

	before() : externalClasses() && constructorExecution() {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled()) log.trace(buildConstructorEntryMessage(thisJoinPoint));
	}

	after() returning() : externalClasses() && constructorExecution() {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled()) log.trace(buildConstructorExitMessage(thisJoinPoint, thisJoinPoint.getThis()));
	}

	after() throwing(Throwable t) : externalClasses() && constructorExecution() {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled()) log.trace(buildConstructorExceptionThrownMessage(thisJoinPoint, t));
	}

	before(Object newval) : fieldChange() && externalClasses() && args(newval) {
		Logger log = getLogger(thisJoinPointStaticPart.getSignature());
		if (log.isTraceEnabled())  log.trace(buildFieldChangeMessage(thisJoinPoint));
	}

	private Logger getLogger(Signature signature) {
		return LoggerFactory.getLogger(signature.getDeclaringTypeName() + "." + signature.getName());
	}

	private static String methodEntryTemplate =			"> {%4$s}.%2$s(%3$s)";
	private static String staticMethodEntryTemplate =	"> %2$s(%3$s)";
	private String buildMethodEntryMessage(JoinPoint joinPoint) {
		Signature signature = joinPoint.getStaticPart().getSignature();

		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();
		String arguments = getArgumentsString(joinPoint);

		if (Modifier.isStatic(signature.getModifiers())) {
			return String.format(staticMethodEntryTemplate, className, methodName, arguments);
		} else {
			return String.format(methodEntryTemplate, className, methodName, arguments, joinPoint.getTarget());
		}
	}

	private static String constructorEntryTemplate =	"> %1$s(%2$s)";
	private String buildConstructorEntryMessage(JoinPoint joinPoint) {
		String className = joinPoint.getStaticPart().getSignature().getDeclaringType().getSimpleName();
		String arguments = getArgumentsString(joinPoint);
		return String.format(constructorEntryTemplate, className, arguments);
	}

	private String buildMethodExitMessage(JoinPoint joinPoint, Object returnValue) {
		MethodSignature signature = (MethodSignature) joinPoint.getStaticPart().getSignature();
		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();

		if (Modifier.isStatic(signature.getModifiers())) {
			return buildStaticMethodExitMessage(className, methodName, returnValue, signature);
		} else {
			return buildMethodExitMessage(		className, methodName, returnValue, signature, joinPoint.getTarget().toString());
		}
	}

	private static String methodExitTemplate			= "< {%3$s}.%2$s";
	private static String methodExitWithResultTemplate	= "< {%3$s}.%2$s(%4$s)";
	private String buildMethodExitMessage(String className, String methodName, Object returnValue, MethodSignature signature, String instance) {
		String returnValueStr = getMethodReturnValue(signature, returnValue);
		if (returnValueStr == null) {
			return String.format(methodExitTemplate, className, methodName, instance);
		} else {
			return String.format(methodExitWithResultTemplate, className, methodName, instance, returnValueStr);
		}
	}

	private static String staticMethodExitTemplate				= "< %2$s";
	private static String staticMethodExitWithResultTemplate	= "< %2$s(%3$s)";
	private String buildStaticMethodExitMessage(String className, String methodName, Object returnValue, MethodSignature signature) {
		String returnValueStr = getMethodReturnValue(signature, returnValue);
		if (returnValueStr == null) {
			return String.format(staticMethodExitTemplate, className, methodName);
		} else {
			return String.format(staticMethodExitWithResultTemplate, className, methodName, returnValueStr);
		}
	}

	private static String constructorExitTemplate				= "< %1$s{%2$s}";
	private String buildConstructorExitMessage(JoinPoint joinPoint, Object returnValue) {
		String className = joinPoint.getStaticPart().getSignature().getDeclaringType().getSimpleName();
		return String.format(constructorExitTemplate, className, returnValue);
	}

	private static String methodExitWithExceptionTemplate		= "! {%5$s}.%2$s threw exception %3$s at line %4$d";
	private static String staticMethodExitWithExceptionTemplate	= "! %2$s threw exception %3$s at line %4$d";
	private String buildMethodExceptionThrownMessage(JoinPoint joinPoint, Throwable t) {
		Signature signature = joinPoint.getStaticPart().getSignature();

		String className = signature.getDeclaringType().getSimpleName();
		String methodName = signature.getName();
		int lineNumber = getCurrentStackTraceElement(t.getStackTrace()).getLineNumber();

		if (Modifier.isStatic(signature.getModifiers())) {
			return String.format(staticMethodExitWithExceptionTemplate, className, methodName, t, lineNumber);
		} else {
			return String.format(methodExitWithExceptionTemplate, className, methodName, t, lineNumber, joinPoint.getTarget().toString());
		}
	}

	private static String constructorExitWithExceptionTemplate	= "! %1$s threw exception %2$s at line %3$d";
	private String buildConstructorExceptionThrownMessage(JoinPoint joinPoint, Throwable t) {
		String className = joinPoint.getStaticPart().getSignature().getDeclaringType().getSimpleName();
		int lineNumber = getCurrentStackTraceElement(t.getStackTrace()).getLineNumber();

		return String.format(constructorExitWithExceptionTemplate, className, t, lineNumber);
	}

	private StackTraceElement getCurrentStackTraceElement(StackTraceElement[] stackTrace) {
		int currentDepth = Thread.currentThread().getStackTrace().length - 4;
		return stackTrace[stackTrace.length - currentDepth];
	}

	private static String fieldChangeTemplate					= "= {%5$s}.%2$s [%3$s] -> [%4$s]";
	private String buildFieldChangeMessage(JoinPoint joinPoint) {
		FieldSignature fieldSignature = (FieldSignature) joinPoint.getSignature();
		Field field = fieldSignature.getField();
		String initialValueString = "unknown";
		String newValueString;

		if (hasSecureAnnotation(field.getAnnotations())) {
			initialValueString = "****";
			newValueString = "****";
		} else {
			newValueString = joinPoint.getArgs()[0] != null ? joinPoint.getArgs()[0].toString() : "";
			Object target = joinPoint.getTarget();
			try {
				field.setAccessible(true);
				Object initialValue = field.get(target);
				field.setAccessible(false);
				initialValueString = getStringValue(initialValue);
			} catch (IllegalAccessException iae) {
				// Do nothing
			}
		}

		String className = fieldSignature.getDeclaringType().getSimpleName();
		String fieldName = fieldSignature.getName();
		Object instance = joinPoint.getTarget();

		return String.format(fieldChangeTemplate, className, fieldName, initialValueString, newValueString, instance);
	}

	private String getArgumentsString(JoinPoint joinPoint) {
		Signature signature = joinPoint.getStaticPart().getSignature();
		String[] paramNames = ((CodeSignature) signature).getParameterNames();
		Annotation[][] annotations = getMemberParameterAnnotations(signature);
		return buildArgumentsString(paramNames, annotations, joinPoint.getArgs());
	}

	private Annotation[][] getMemberParameterAnnotations(Signature signature) {
		if (signature instanceof MethodSignature) {
			Method method = ((MethodSignature) signature).getMethod();
			return method.getParameterAnnotations();
		} else {
			Constructor<?> constructor = ((ConstructorSignature) signature).getConstructor();
			return constructor.getParameterAnnotations();
		}
	}

	private String buildArgumentsString(String[] paramNames, Annotation[][] annotations, Object[] arguments) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < arguments.length; i++) {
			if (paramNames != null) {
				result.append(paramNames[i]).append("=");
			}
			if (hasSecureAnnotation(annotations[i])) {
				result.append("****");
			} else {
				result.append(getStringValue(arguments[i]));
			}
			if (i != arguments.length - 1) {
				result.append(", ");
			}
		}
		return result.toString();
	}

	private boolean hasSecureAnnotation(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(Secure.class))
				return true;
		}
		return false;
	}

	private String getStringValue(Object argument) {
		String argString;
		if (argument != null) {
			if (argument.getClass().isArray()) {
				argString = ArrayUtils.toString(argument);
			} else {
				argString = argument.toString();
			}
		} else {
			argString = "null";
		}
		return argString;
	}

	private boolean memberHasSecureAnnotation(Signature signature) {
		Annotation[] annotations = null;
		if (signature instanceof MethodSignature) {
			Method method = ((MethodSignature) signature).getMethod();
			annotations = method.getAnnotations();
		} else {
			Constructor<?> constructor = ((ConstructorSignature) signature).getConstructor();
			annotations = constructor.getAnnotations();
		}
		return hasSecureAnnotation(annotations);
	}

	private String getMethodReturnValue(MethodSignature signature, Object returnValue) {
		String returnValueStr = null;
		if (memberHasSecureAnnotation(signature)) {
			returnValueStr = "****";
		} else {
			if (!signature.getReturnType().equals(void.class)) {
				returnValueStr = getStringValue(returnValue);
			}
		}
		return returnValueStr;
	}

	static {
		try {
			InputStream input = TraceAspect.class.getClassLoader().getResourceAsStream("traceformat.properties");
			if (input != null) {
				Properties props = new Properties();
				props.load(input);

				if (props.getProperty("methodEntryTemplate") != null)
					methodEntryTemplate = props.getProperty("methodEntryTemplate");
				if (props.getProperty("methodExitTemplate") != null)
					methodExitTemplate = props.getProperty("methodExitTemplate");
				if (props.getProperty("methodExitWithResultTemplate") != null)
					methodExitWithResultTemplate = props.getProperty("methodExitWithResultTemplate");
				if (props.getProperty("methodExitWithExceptionTemplate") != null)
					methodExitWithExceptionTemplate = props.getProperty("methodExitWithExceptionTemplate");

				if (props.getProperty("staticMethodEntryTemplate") != null)
					staticMethodEntryTemplate = props.getProperty("staticMethodEntryTemplate");
				if (props.getProperty("staticMethodExitTemplate") != null)
					staticMethodExitTemplate = props.getProperty("staticMethodExitTemplate");
				if (props.getProperty("staticMethodExitWithResultTemplate") != null)
					staticMethodExitWithResultTemplate = props.getProperty("staticMethodExitWithResultTemplate");
				if (props.getProperty("staticMethodExitWithExceptionTemplate") != null)
					staticMethodExitWithExceptionTemplate = props.getProperty("staticMethodExitWithExceptionTemplate");

				if (props.getProperty("constructorEntryTemplate") != null)
					constructorEntryTemplate = props.getProperty("constructorEntryTemplate");
				if (props.getProperty("constructorExitTemplate") != null)
					constructorExitTemplate = props.getProperty("constructorExitTemplate");
				if (props.getProperty("constructorExitWithExceptionTemplate") != null)
					constructorExitWithExceptionTemplate = props.getProperty("constructorExitWithExceptionTemplate");

				if (props.getProperty("fieldChangeTemplate") != null)
					fieldChangeTemplate = props.getProperty("fieldChangeTemplate");
			}
		} catch (IOException ioe) {
			LoggerFactory.getLogger(TraceAspect.class).error("Unable to load template properties file", ioe);
		}
	}

}
