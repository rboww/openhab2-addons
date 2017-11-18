/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal.cgate;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class StatusChangeCallback {

    /**
     *
     * @return true if callback is active
     */
    public abstract boolean isActive();

    /**
     *
     * @param cgate_session
     * @param status_change
     */
    public abstract void processStatusChange(CGateSession cgate_session, String status_change);
}
