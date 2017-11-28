/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal.cgate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class CGateException extends Exception {
    private Logger logger = LoggerFactory.getLogger(CGateException.class);

    private final static String new_line = System.getProperty("line.separator");

    /**
     *
     */
    public CGateException() {
        this(null, null);
    }

    /**
     *
     * @param e
     */
    public CGateException(Exception e) {
        this(e.getMessage(), e);
    }

    /**
     *
     * @param response
     */
    public CGateException(String response) {
        this(response, null);
    }

    /**
     *
     * @param response
     * @param e
     */
    public CGateException(String response, Exception e) {
        super(response, e);

        String message = getMessage();

        Throwable traced_exception = e;
        while (traced_exception instanceof InvocationTargetException) {
            InvocationTargetException ite = (InvocationTargetException) traced_exception;
            traced_exception = ite.getTargetException();
        }
        if (traced_exception instanceof CGateException) {
            return;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        printStackTrace(pr);
        message += new_line + new_line + sw.toString();

        // logger.error(message);
        logger.error("{}",response, e == null ? "" : e.getMessage());
    }
}
