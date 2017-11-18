/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal.cgate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugStatusChangeCallback extends StatusChangeCallback {
    private Logger logger = LoggerFactory.getLogger(DebugStatusChangeCallback.class);

    @Override
    public boolean isActive() {
        return logger.isDebugEnabled();
    }

    @Override
    public void processStatusChange(CGateSession cgate_session, String status_change) {
        // logger.debug("status_change: " + status_change);
    }

}
