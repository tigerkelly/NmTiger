/*
 * Copyright (c) 2023 Richard Kelly Wiles (rkwiles@twc.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *  Created on: Aug 6, 2023
 *      Author: Kelly Wiles
 */

package application;

import javax.swing.event.EventListenerList;

/**
 * This is a custom event to notify the workbench that a page has been changed.
 * @author Kelly Wiles
 *
 */
public class StatsChanges {
	protected EventListenerList listenerList = new EventListenerList();
	
	public final static int STATS = 1;
	public final static int CLEAR_STATS = 2;
	
	// This methods allows classes to register for Changes
    public void addChangeListener(StatsChangeListener listener) {
        listenerList.add(StatsChangeListener.class, listener);
    }

    // This methods allows classes to unregister for Changes
    public void removeChangeListener(StatsChangeListener listener) {
        listenerList.remove(StatsChangeListener.class, listener);
    }

    // This private class is used to fire Changes
    public void fireChange(StatsChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == StatsChangeListener.class) {
                ((StatsChangeListener)listeners[i+1]).changeEventOccurred(evt);
            }
        }
    }
}
