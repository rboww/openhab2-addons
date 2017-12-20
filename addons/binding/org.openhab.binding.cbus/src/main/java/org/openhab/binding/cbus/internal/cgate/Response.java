/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.cbus.internal.cgate;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class Response implements Iterable<String> {
    private Logger logger = LoggerFactory.getLogger(Response.class);

    private final Object response_mutex = new Object();

    private final Object iterator_mutex = new Object();

    private BufferedReader response_reader;

    private ArrayList<String> array_response;

    private boolean response_generated = false;

    protected static final ThreadPoolExecutor thread_pool;

    static {
        thread_pool = (ThreadPoolExecutor) ThreadPoolManager.getPool("CGateResponses");
    }


    Response(BufferedReader response_reader) throws CGateException {
        try {
            this.response_reader = response_reader;
            thread_pool.execute(new Thread() {
                @Override
                public void run() {
                    synchronized (iterator_mutex) {
                        array_response = new ArrayList<String>();
                    }

                    try {
                        boolean has_more = true;
                        while (has_more && !isInterrupted()) {
                            String response = Response.this.response_reader.readLine();
                            synchronized (Response.this.iterator_mutex) {
                                array_response.add(response);
                                Response.this.iterator_mutex.notifyAll();
                            }
                            has_more = responseHasMore(response);
                        }

                        if (logger.isDebugEnabled()) {
                            for (String response : array_response) {
                                logger.trace("response: {}", response);
                            }
                        }
                    } catch (IOException e) {
                        new CGateException(e);
                        logger.error("Failed to load response ", e);
                    } finally {
                        Response.this.response_reader = null;
                        synchronized (Response.this.response_mutex) {
                            synchronized (Response.this.iterator_mutex) {
                                Response.this.response_generated = true;
                                Response.this.response_mutex.notifyAll();
                                Response.this.iterator_mutex.notifyAll();
                            }
                        }
                    }
                }

                @Override
                public void interrupt() {
                    super.interrupt();
                    synchronized (Response.this.response_mutex) {
                        synchronized (Response.this.iterator_mutex) {
                            Response.this.response_generated = true;
                            Response.this.response_mutex.notifyAll();
                            Response.this.iterator_mutex.notifyAll();
                        }
                    }
                }

            }

            );

        } catch (Exception e) {
            throw new CGateException(e);
        }
    }

    static boolean responseHasMore(String response) {
        return response == null ? false : response.substring(3, 4).equals("-");
    }

    private class x extends Thread {

    }

    ArrayList<String> toArray() {
        synchronized (response_mutex) {
            while (!response_generated) {
                try {
                    response_mutex.wait(10000l);
                } catch (InterruptedException ie) {
                }
            }
        }

        return array_response;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                synchronized (iterator_mutex) {
                    while (array_response == null || (index >= array_response.size() && !response_generated)) {
                        try {
                            iterator_mutex.wait(10000l);
                        } catch (InterruptedException ie) {
                        }
                    }

                    if (index < array_response.size()) {
                        return true;
                    }

                    if (response_generated) {
                        return false;
                    }

                    throw new IllegalStateException("Impossible");
                }
            }

            @Override
            public String next() {
                synchronized (iterator_mutex) {
                    if (index >= array_response.size()) {
                        throw new NoSuchElementException();
                    }

                    return array_response.get(index++);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove not supported");
            }
        };
    }

    public void handle200() throws CGateException {
        ArrayList<String> resp_array = toArray();
        if (resp_array.isEmpty()) {
            throw new CGateException();
        }

        String resp_str = resp_array.get(resp_array.size() - 1);
        String result_code = resp_str.substring(0, 3).trim();
        if (!result_code.equals("200")) {
            throw new CGateException(resp_str);
        }
    }
}
