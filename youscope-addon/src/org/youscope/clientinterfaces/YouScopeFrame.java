/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.youscope.clientinterfaces;

import java.awt.Component;
import java.awt.Dimension;

/**
 * Generalized interface for frames. YouScope does not use generic classes like JFrame or JInternalFrame to make it easier to switch between the setup
 * of the program. This might be e.g. useful if YouScope should later on be modified to also work directly on microscopes, e.g. over a integrated
 * touch screen. Furthermore, most UI frameworks are not thread save. Since microscope communication should be done in separate threads to not block
 * the UI, we implemented these methods to be thread save.
 *
 * @author langmo
 */
public interface YouScopeFrame
{
    /**
     * Sets the content pane of this frame. The content pane can be set from any thread (setting is guaranteed to be thread-save), however, after
     * setting the content pane it should be only manipulated by the UI thread (e.g. the thread processing action events of a button, but not a worker
     * thread). To change the content pane after setting it from a thread which is not the UI thread, see SwingUtilities.invokeLater and
     * SwingUtilities.invokeAndWait().
     *
     * @param contentPane The new content pane of the frame.
     */
    void setContentPane(Component contentPane);

    /**
     * Sets the visibility of the frame and its content. This function is guaranteed to be thread save.
     *
     * @param visible True if frame should be visible, false otherwise.
     */
    void setVisible(boolean visible);

    /**
     * Sets the margins between the frame borders and the frame content (i.e. the margins around the content pane).
     * @param left margin left, in pixels.
     * @param top margin top, in pixels.
     * @param right margin right, in pixels.
     * @param bottom margin bottom, in pixels.
     */
    void setMargins(int left, int top, int right, int bottom);
    
    /**
     * Returns if frame is visible. This function is guaranteed to be thread save.
     *
     * @return True if frame is visible, false otherwise.
     */
    boolean isVisible();

    /**
     * Maximizes the frame (if maximum == true) or restores the actual frame's size (if maximum == false). This function is guaranteed to be thread
     * save.
     *
     * @param maximum True if frame should be maximized.
     */
    void setMaximum(boolean maximum);

    /**
     * Creates a non-modal child frame. Child frames get automatically closed if the parent frame is closed. This function is guaranteed to be thread
     * save. Same as calling createFrame() and addChildFrame().
     *
     * @return A new frame.
     */
    YouScopeFrame createChildFrame();

    /**
     * Creates a modal child frame. The parent frame gets blocked until the child frame is closed. This function is guaranteed to be thread save.
     * Same as calling createFrame() and addModalChildFrame().
     *
     * @return A new frame.
     */
    YouScopeFrame createModalChildFrame();

    /**
     * Creates a new non-modal frame. This frame gets not closed if the parent frame is closed. This function is guaranteed to be thread save.
     *
     * @return A new frame.
     */
    YouScopeFrame createFrame();

    /**
     * Adds the frame as a child frame to this frame. This implies that the child frame is closed when this frame is closed.
     * @param childFrame The child frame to add.
     */
    void addChildFrame(YouScopeFrame childFrame);
    
    /**
     * Adds the frame as a modal child frame to this frame. This implies that it becomes impossible to edit the current frame as long as the child frame is open,
     * and that the focus is automatically transferred to the child frame whenever this frame gets it.
     * @param childFrame The frame to add as a modal child frame.
     */
    void addModalChildFrame(YouScopeFrame childFrame);
    
    /**
     * Sets the size of the frame. Be aware that this might or might not only be interpreted as a hint for the UI. This function is guaranteed to be
     * thread save.
     *
     * @param size Size of the frame.
     */
    void setSize(Dimension size);

    /**
     * Returns the size of the frame. This function is guaranteed to be thread save.
     *
     * @return Size of the frame.
     */
    Dimension getSize();

    /**
     * Sets the size of the frame such that all UI elements will be (optimally) sized. This function is guaranteed to be thread save.
     */
    void pack();

    /**
     * Sets if the frame is resizable or not.This might only be interpreted as a hint by certain implementations. This function is guaranteed to be
     * thread save.
     *
     * @param resizable True if frame is resizable, false otherwise.
     */
    void setResizable(boolean resizable);

    /**
     * Sets if the frame is closable by the user (i.e. if a "close-button" is shown at the top of the frame). This might only be interpreted as a hint
     * by certain implementations. This function is guaranteed to be thread save.
     *
     * @param closable True if user should be able to close the frame in the usual way.
     */
    void setClosable(boolean closable);

    /**
     * Sets if the user should be able to maximize the frame. This might only be interpreted as a hint by certain implementations. This function is
     * guaranteed to be thread save.
     *
     * @param maximizable True if frame should be maximizable.
     */
    void setMaximizable(boolean maximizable);

    /**
     * Returns if frame is resizable. This function is guaranteed to be thread save.
     *
     * @return True if frame is resizable, otherwise false.
     */
    boolean isResizable();

    /**
     * Returns if frame is closable by the user. This function is guaranteed to be thread save.
     *
     * @return True if frame is closable.
     */
    boolean isClosable();

    /**
     * Returns if frame is maximizable. This function is guaranteed to be thread save.
     *
     * @return True if frame is maximizable.
     */
    boolean isMaximizable();

    /**
     * Adds a listener to the frame which is informed e.g. if the frame is closed. This function is guaranteed to be thread save.
     *
     * @param listener The listener to add.
     */
    void addFrameListener(YouScopeFrameListener listener);

    /**
     * Removes a previously added listener. This function is guaranteed to be thread save.
     *
     * @param listener The listener to remove.
     */
    void removeFrameListener(YouScopeFrameListener listener);

    /**
     * Sets the title of the frame. This title may be displayed or not by the implementation. This function is guaranteed to be thread save.
     *
     * @param title Title of the frame.
     */
    void setTitle(String title);

    /**
     * Returns the title of the frame. This function is guaranteed to be thread save.
     *
     * @return The title of the frame.
     */
    String getTitle();

    /**
     * Removes all previously added UI elements from the frame and displays instead the given error. Should be called when an error happened which is
     * such serious that the frame cannot be used by the user anymore. This function is guaranteed to be thread save.
     *
     * @param message The message describing the error.
     * @param e The error.
     */
    void setToErrorState(String message, Exception e);

    /**
     * Indicates to the user, in one or the other way, that the content of the frame is currently initialized. This function should be called if the
     * initialization takes significant time (e.g. due to necessary communication with the microscope). After the function is called, a worker thread
     * should be started doing the real initialization, such that the UI is not blocked. After initialization, the worker thread should then call
     * <code>setContentPane()</code> followed by <code>endLoading()</code>. The content pane should not be set before all elements of the content pane
     * were created (i.e. no components of the content pane should be manipulated by the worker thread after calling setContentPane()). This function
     * is guaranteed to be thread save.
     */
    void startInitializing();

    /**
     * Similar to <code>startInitializing()</code>, only that in this case the UI is already initialized, but certain data has to be loaded e.g. after
     * some user input. The loading should also be done in a worker thread which is calling <code>endLoading()</code> after finishing. This function
     * is guaranteed to be thread save. However, if the content pane was already set prior to a call to this function, the worker thread should not
     * manipulate any component of the content pane anymore. Instead, it should either create and set a new content pane (including all components of
     * the content pane) or only manipulate the content pane by using the UI thread (see SwingUtilities.invokeLater and
     * SwingUtilities.invokeAndWait()).
     */
    void startLoading();

    /**
     * Removes the indication from this frame that it is loaded or initialized. This function should be called from a worker thread, after the main
     * thread has called <code>startInitializing()</code> or <code>startLoading()</code>. This function is guaranteed to be thread save.
     */
    void endLoading();

    /**
     * Positions the frame at the front and sets the focus to this frame.
     */
    void toFront();
    
    /**
     * Relocates all calls to this object's functions to the corresponding functions of the target frame.
     * With this mechanism, the pointer to the physical frame containing the content can be switched without any other function using the frame noticing it.
     * Should in general only be used for specific purposes. 
     * @param targetFrame The target frame calls to this frame should be relocated to.
     */
    void relocateFrameTo(YouScopeFrame targetFrame);
}
