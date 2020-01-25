package org.youscope.plugin.matlabscripting;

/*
 * Copyright (c) 2010, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.HashMap;

import com.mathworks.jmi.Matlab;
import com.mathworks.jmi.MatlabException;
import com.mathworks.jmi.NativeMatlab;

/**
 * This code is inspired by <a href="mailto:whitehouse@virginia.edu">Kamin Whitehouse</a>'s <a
 * href="http://www.cs.virginia.edu/~whitehouse/matlab/JavaMatlab.html">MatlabControl</a>. <br>
 * <br>
 * This class runs inside of the MATLAB Java Virtual Machine and relies upon the jmi.jar which is
 * distributed with MATLAB in order to send commands to MATLAB and receive results. <br>
 * <br>
 * Only this class and {@link MatlabInvocationException} directly interact with jmi.jar.
 * 
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
class JMIWrapper
{
    /**
     * The return value from blocking methods.
     */
    private Object _returnValue;

    /**
     * A MatlabException that may be thrown during execution of {@link #returningEval(String, int)},
     * {@link #returningFeval(String, Object[])}, or {@link #returningFeval(String, Object[], int)}.
     * The exception must be stored as the direction cannot be thrown directly because it is inside
     * of a Runnable.
     */
    private MatlabException _thrownException = null;

    /**
     * The default value that _returnVal is set to before an actual return value is returned.
     */
    private static final String BEFORE_RETURN_VALUE = "noReturnValYet";

    /**
     * Map of variables used by {@link #getVariableValue(String)}, and
     * {@link #setVariable(String, Object)}.
     */
    private static HashMap<String, Object> VARIABLES = new HashMap<String, Object>();

    /**
     * The name of this class and package.
     */
    private static final String CLASS_NAME = JMIWrapper.class.getName();

    /**
     * Gets the variable value stored by setVariable(String, Object).
     * 
     * @param variableName
     * @return variable value
     */
    public static Object retrieveVariableValue(String variableName)
    {
        Object result = VARIABLES.get(variableName);
        VARIABLES.remove(variableName);

        return result;
    }

    /**
     * Sets the variable to the given value. This is done by storing the variable in Java and then
     * retrieving it in MATLAB by calling a Java method that will return it.
     * 
     * @param variableName
     * @param value
     * @throws MatlabInvocationException
     */
    void setVariable(String variableName, Object value) throws MatlabInvocationException
    {
        VARIABLES.put(variableName, value);
        this.eval(variableName + " = " + CLASS_NAME + ".retrieveVariableValue('" + variableName + "');");
    }

    /**
     * Convenience method to retrieve a variable's value from MATLAB.
     * 
     * @param variableName
     * @throws MatlabInvocationException
     */
    Object getVariable(String variableName) throws MatlabInvocationException
    {
        return this.returningEval(variableName, 1);
    }

    /**
     * Exits MATLAB.
     * 
     * @throws MatlabInvocationException
     */
    static void exit() throws MatlabInvocationException
    {
        Matlab.whenMatlabReady(new Runnable()
            {
                @Override
				public void run()
                {
                    try
                    {
                        Matlab.mtFevalConsoleOutput("exit", null, 0);
                    } catch (@SuppressWarnings("unused") Exception e)
                    {
                        // Do nothing.
                    }
                }
            });
    }

    /**
     * Evaluates a command in MATLAB. The result of this command will not be returned. <br>
     * <br>
     * This is equivalent to MATLAB's <code>eval(['command'])</code>.
     * 
     * @param command the command to be evaluated in MATLAB
     * @throws MatlabInvocationException
     * @see #returningEval(String, int)
     */
    String eval(final String command) throws MatlabInvocationException
    {
    	return this.returningFeval("evalc", new Object[]{ command }, 1).toString();
    }

    /**
     * Evaluates a command in MATLAB. The result of this command can be returned. <br>
     * <br>
     * This is equivalent to MATLAB's <code>eval(['command'])</code>. <br>
     * <br>
     * In order for the result of this command to be returned the number of arguments to be returned
     * must be specified by <code>returnCount</code>. If the command you are evaluating is a MATLAB
     * function you can determine the amount of arguments it returns by using the
     * <code>nargout</code> function in the MATLAB Command Window. If it returns -1 that means the
     * function returns a variable number of arguments based on what you pass in. In that case, you
     * will need to manually determine the number of arguments returned. If the number of arguments
     * returned differs from <code>returnCount</code> then either <code>null</code> or an empty
     * <code>String</code> will be returned.
     * 
     * @param command the command to be evaluated in MATLAB
     * @param returnCount the number of arguments that will be returned from evaluating the command
     * @see #eval(String)
     * @return result of MATLAB eval
     * @throws MatlabInvocationException
     */
    Object returningEval(final String command, final int returnCount)
            throws MatlabInvocationException
    {
        return this.returningFeval("eval", new Object[]
            { command }, returnCount);
    }

    /**
     * Calls a MATLAB function with the name <code>functionName</code>. Arguments to the function
     * may be provided as <code>args</code>, if you wish to call the function with no arguments pass
     * in <code>null</code>. The result of this command will not be returned. <br>
     * <br>
     * The <code>Object</code>s in the array will be converted into MATLAB equivalents as
     * appropriate. Importantly, this means that any <code>String</code> will be converted to a
     * MATLAB char array, not a variable name.
     * 
     * @param functionName name of the MATLAB function to call
     * @param args the arguments to the function, <code>null</code> if none
     * @throws MatlabInvocationException
     * @see #returningFeval(String, Object[], int)
     */
    void feval(final String functionName, final Object[] args) throws MatlabInvocationException
    {
        this.returningFeval(functionName, args, 0);
    }

    /**
     * Calls a MATLAB function with the name <code>functionName</code>. Arguments to the function
     * may be provided as <code>args</code>, if you wish to call the function with no arguments pass
     * in <code>null</code>. <br>
     * <br>
     * The <code>Object</code>s in the array will be converted into MATLAB equivalents as
     * appropriate. Importantly, this means that any <code>String</code> will be converted to a
     * MATLAB char array, not a variable name. <br>
     * <br>
     * The result of this function can be returned. In order for the result of this function to be
     * returned the number of arguments to be returned must be specified by <code>returnCount</code>
     * . You can use the <code>nargout</code> function in the MATLAB Command Window to determine the
     * number of arguments that will be returned. If <code>nargout</code> returns -1 that means the
     * function returns a variable number of arguments based on what you pass in. In that case, you
     * will need to manually determine the number of arguments returned. If the number of arguments
     * returned differs from <code>returnCount</code> then either only some of the items will be
     * returned or <code>null</code> will be returned.
     * 
     * @param functionName name of the MATLAB function to call
     * @param args the arguments to the function, <code>null</code> if none
     * @param returnCount the number of arguments that will be returned from this function
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     * @return result of MATLAB function
     * @throws MatlabInvocationException
     */
    Object returningFeval(final String functionName, final Object[] args, final int returnCount)
            throws MatlabInvocationException
    {
        if (isMatlabThread())
        {
            try
            {
                return Matlab.mtFevalConsoleOutput(functionName, args, returnCount);
            } 
            catch (Exception e)
            {
                throw new MatlabInvocationException(e.getMessage(), e);
            }
        }
		_returnValue = BEFORE_RETURN_VALUE;
		_thrownException = null;
		
		Object returnValue;
		synchronized (this)
		{
		    Matlab.whenMatlabIdle(new Runnable()
		        {
		            @Override
					public void run()
		            {
		                try
		                {
		                    JMIWrapper.this.setReturnValue(Matlab.mtFevalConsoleOutput(
		                            functionName, args, returnCount));
		                } catch (MatlabException e)
		                {
		                    _thrownException = e;
		                    JMIWrapper.this.setReturnValue(null);
		                } catch (@SuppressWarnings("unused") Exception e)
		                {
		                    JMIWrapper.this.setReturnValue(null);
		                }
		            }
		        });
		    returnValue = this.getReturnValue();
		}
		if(returnValue instanceof double[] && ((double[])returnValue).length == 1)
			returnValue = ((double[])returnValue)[0];
		else if(returnValue instanceof int[] && ((int[])returnValue).length == 1)
			returnValue = ((int[])returnValue)[0];
		else if(returnValue instanceof Object[] && ((Object[])returnValue).length == 1)
			returnValue = ((Object[])returnValue)[0];
		else if(returnValue instanceof byte[] && ((byte[])returnValue).length == 1)
			returnValue = ((byte[])returnValue)[0];
		else if(returnValue instanceof char[] && ((char[])returnValue).length == 1)
			returnValue = ((char[])returnValue)[0];
		else if(returnValue instanceof long[] && ((long[])returnValue).length == 1)
			returnValue = ((long[])returnValue)[0];
		return returnValue;
    }

    /**
     * Calls a MATLAB function with the name <code>functionName</code>. Arguments to the function
     * may be provided as <code>args</code>, if you wish to call the function with no arguments pass
     * in <code>null</code>. <br>
     * <br>
     * The <code>Object</code>s in the array will be converted into MATLAB equivalents as
     * appropriate. Importantly, this means that any <code>String</code> will be converted to a
     * MATLAB char array, not a variable name. <br>
     * <br>
     * The result of this function can be returned. In order for a function's return data to be
     * returned to MATLAB it is necessary to know how many arguments will be returned. This method
     * will attempt to determine that automatically, but in the case where a function has a variable
     * number of arguments returned it will only return one of them. To have all of them returned
     * use {@link #returningFeval(String, Object[], int)} and specify the number of arguments that
     * will be returned.
     * 
     * @param functionName name of the MATLAB function to call
     * @param args the arguments to the function, <code>null</code> if none
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     * @return result of MATLAB function
     * @throws MatlabInvocationException
     */
    Object returningFeval(final String functionName, final Object[] args)
            throws MatlabInvocationException
    {
        // Get the number of arguments that will be returned
        Object result = this.returningFeval("nargout", new String[]
            { functionName }, 1);
        int nargout = 0;
        nargout = (int) ((double[]) result)[0];

        // If an unlimited number of arguments (represented by -1), choose 1
        if (nargout == -1)
        {
            nargout = 1;
        }

        // Send the request
        return this.returningFeval(functionName, args, nargout);
    }

    /**
     * Allows for enabling a diagnostic mode that will show in MATLAB each time a Java method that
     * calls into MATLAB is invoked.
     * 
     * @param echo
     */
    static void setEchoEval(final boolean echo)
    {
        if (isMatlabThread())
        {
            Matlab.setEchoEval(echo);
        } else
        {
            Matlab.whenMatlabReady(new Runnable()
                {
                    @Override
					public void run()
                    {
                        Matlab.setEchoEval(echo);
                    }
                });
        }
    }

    /**
     * Returns the value returned by MATLAB to {@link #returningFeval(String, Object[], int)}.
     * Throws a MatlabException if the JMI call threw an exception. <br>
     * <br>
     * Returning this operates by pausing the thread until MATLAB returns a value.
     * 
     * @return result of MATLAB call
     * @throws MatlabInvocationException
     * @see #setReturnValue(Object)
     */
    private Object getReturnValue() throws MatlabInvocationException
    {
        // If _returnVal has not been changed yet (in all likelihood it has not)
        // then wait, it will be resumed when the call to MATLAB returns
        while(_returnValue == BEFORE_RETURN_VALUE)
        {
            synchronized (_returnValue)
            {
                try
                {
                    _returnValue.wait();
                } 
                catch (InterruptedException e)
                {
                    throw new MatlabInvocationException(MatlabInvocationException.INTERRUPTED_MSG, e);
                }
            }
        }

        if (_thrownException != null)
        {
        	String message = _thrownException.getMessage();
        	_thrownException = null;
            throw new MatlabInvocationException(message);
        }

        return _returnValue;
    }

    /**
     * Sets the return value from any of the eval or feval commands. In the case of the
     * non-returning value null is passed in, but it still provides the functionality of waking up
     * the thread so that {@link #getReturnValue()} will be able to return.
     * 
     * @param val
     * @see #getReturnValue()
     */
    private void setReturnValue(Object val)
    {
        synchronized (_returnValue)
        {
            Object oldVal = _returnValue;
            _returnValue = val;
            oldVal.notifyAll();
        }
    }

    /**
     * Returns whether or not the calling thread is the main MATLAB thread.
     * 
     * @return if main MATLAB thread
     */
    private static boolean isMatlabThread()
    {
        return NativeMatlab.nativeIsMatlabThread();
    }
}