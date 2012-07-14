/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.dataimport.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to Solr web application Initialize and Destroy events
 * Uses HTTPPostScheduler, java.util.Timer and context attribute map
 * to facilitate periodic method invocation (scheduling).<br>
 * Timer is essentially a facility for threads to schedule tasks
 * for future execution in a background thread.<br>
 * For more info:<br>
 * http://wiki.apache.org/solr/DataImportHandler#ApplicationListener
 * @author mbonaci
 */
public class ApplicationListener implements ServletContextListener {

        private static final Logger logger = LoggerFactory.getLogger(ApplicationListener.class);

        @Override
        public void contextDestroyed(ServletContextEvent servletContextEvent) {
                ServletContext servletContext = servletContextEvent.getServletContext();

                // get our timer from the context
                Timer timer = (Timer)servletContext.getAttribute("timer");

                // cancel all active tasks in the timers queue
                if (timer != null)
                        timer.cancel();

                // remove the timer from the context
                servletContext.removeAttribute("timer");

        }

        @Override
        public void contextInitialized(ServletContextEvent servletContextEvent) {
                ServletContext servletContext = servletContextEvent.getServletContext();
                try{
                        // create the timer and timer task objects
                        Timer timer = new Timer();
                        HttpPostScheduler task = new HttpPostScheduler(servletContext.getServletContextName(), timer);

                        // get our interval from HTTPPostScheduler
                        int interval = task.getIntervalInt();

                        // get a calendar to set the start time (first run)
                        Calendar calendar = Calendar.getInstance();

                        // set the first run to now + interval (to avoid fireing while the app/server is starting)
                        calendar.add(Calendar.MINUTE, interval);
                        Date startTime = calendar.getTime();

                        // schedule the task
                        timer.scheduleAtFixedRate(task, startTime, 1000 * 60 * interval);

                        // save the timer in context
                        servletContext.setAttribute("timer", timer);

                } catch (Exception e) {
                        if(e.getMessage().endsWith("disabled")){
                                logger.info("Schedule disabled");
                        }else{
                                logger.error("Problem initializing the scheduled task: ", e);
                        }
                }
        }

}