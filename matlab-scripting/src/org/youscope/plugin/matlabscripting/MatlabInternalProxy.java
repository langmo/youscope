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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Methods that can be called to control MATLAB except for {@link #checkConnection()}. All of these
 * methods throw RemoteException. RemoteException will be thrown if something occurs to disrupt the
 * communication between this JVM and the one MATLAB is running in. For instance, closing MATLAB
 * will terminate its JVM and then all method calls on this proxy will throw exceptions.
 * Additionally, many of these methods throw InterruptedException. This occurs if while waiting for
 * MATLAB to return from a method called on it, the thread waiting is interrupted. For descriptions
 * of what these methods do see the corresponding methods in {@link RemoteMatlabProxy}.
 * 
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
public interface MatlabInternalProxy extends Remote
{
    /**
     * Exits MATLAB.
     * 
     * @throws RemoteException
     * @throws MatlabInvocationException
     */
    public void exit() throws RemoteException, MatlabInvocationException;

    /**
     * Sets the variable to the given <code>value</code>.
     * 
     * @param variableName
     * @param value
     * @throws RemoteException
     * @throws MatlabInvocationException
     */
    public void setVariable(String variableName, Object value) throws RemoteException,
            MatlabInvocationException;

    /**
     * Gets the value of the variable named </code>variableName</code> from MATLAB.
     * 
     * @param variableName
     * @return value
     * @throws RemoteException
     * @throws MatlabInvocationException
     */
    public Object getVariable(String variableName) throws RemoteException,
            MatlabInvocationException;

    /**
     * Evaluates a command in MATLAB. The result of this command will not be returned. <br>
     * <br>
     * This is equivalent to MATLAB's <code>eval(['command'])</code>.
     * 
     * @param command the command to be evaluated in MATLAB
     * @return A String representation of the Matlab display output.
     * @throws RemoteException
     * @throws MatlabInvocationException
     * @see #returningEval(String, int)
     */
    public String eval(String command) throws RemoteException, MatlabInvocationException;

    /**
     * Calls a MATLAB function with the name <code>functionName</code>. Arguments to the function
     * may be provided as <code>args</code>, if you wish to call the function with no arguments pass
     * in <code>null</code>. The result of this command will not be returned. <br>
     * <br>
     * The <code>Object</code>s in the array will be converted into MATLAB equivalents as
     * appropriate. Importantly, this means that any <code>String</code> will be converted to a
     * MATLAB char array, not a variable name.
     * 
     * @param command name of the MATLAB function to call
     * @param args the arguments to the function, <code>null</code> if none
     * @throws RemoteException
     * @throws MatlabInvocationException
     * @see #returningFeval(String, Object[], int)
     * @see #returningFeval(String, Object[])
     */
    public void feval(String command, Object[] args) throws RemoteException,
            MatlabInvocationException;

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
     * @param command name of the MATLAB function to call
     * @param args the arguments to the function, <code>null</code> if none
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     * @return result of MATLAB function
     * @throws RemoteException
     * @throws MatlabInvocationException
     */
    public Object returningFeval(String command, Object[] args) throws RemoteException,
            MatlabInvocationException;

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
     * @param command name of the MATLAB function to call
     * @param args the arguments to the function, <code>null</code> if none
     * @param returnCount the number of arguments that will be returned from this function
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     * @return result of MATLAB function
     * @throws RemoteException
     * @throws MatlabInvocationException
     */
    public Object returningFeval(String command, Object[] args, int returnCount)
            throws RemoteException, MatlabInvocationException;

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
     * @throws RemoteException
     * @throws MatlabInvocationException
     */
    public Object returningEval(String command, int returnCount) throws RemoteException,
            MatlabInvocationException;

    /**
     * Allows for enabling a diagnostic mode that will show in MATLAB each time a Java method that
     * calls into MATLAB is invoked.
     * 
     * @param echo
     * @throws RemoteException
     */
    public void setEchoEval(boolean echo) throws RemoteException;

    /**
     * This method does nothing. It is used internally to check if a connection is still active.
     * 
     * @throws RemoteException
     */
    public void checkConnection() throws RemoteException;
}