package uk.org.lidalia.logging.test;

import uk.org.lidalia.lang.Secure;

public class ClassToBeTraced {

    public ClassToBeTraced() {
        super();
    }

    public ClassToBeTraced(String[] strings) {
        super();
    }

    public ClassToBeTraced(String message) {
        super();
        throw new RuntimeException(message);
    }

    public ClassToBeTraced(@Secure String password, String other) {
        super();
    }

    public void simpleMethod() {}

    public static void simpleStaticMethod() {}

    public void runtimeExceptionMethod() {
        throw new RuntimeException("message");
    }

    public void exceptionMethod() throws Exception {
        throw new Exception("message");
    }

    public void publicMethod() {
        protectedMethod();
    }

    protected void protectedMethod() {
        defaultMethod();
    }

    void defaultMethod() {
        privateMethod();
    }

    private void privateMethod() {}

    public void methodWithParameters(String s, Integer i) {}

    public String methodThatReturns() {
        return "hello";
    }

    public String methodThatReturnsNull() {
        return null;
    }

    public void methodWithArrayParameter(String[] strings) {}

    public String[] methodWithArrayReturnType() {
        return new String[] { "hello", "world" };
    }

    @Override
    public String toString() {
        return "aToStringValue";
    }

    public void nestedExceptionMethod() {
        runtimeExceptionMethod();
    }

    int aField = 1;

    public void setAField(int aField) {
        this.aField = aField;
    }

    public void methodWithSecureParam(@Secure String password) {}

    @Secure
    public String secureMethod() {
        return "password";
    }

    public Object nullReturningMethod() {
        return null;
    }

    @Secure
    String secureField = null;

    public void setSecureField(String secureField) {
        this.secureField = secureField;
    }

}
