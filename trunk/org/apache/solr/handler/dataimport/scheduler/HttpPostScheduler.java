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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents main DIHScheduler thread (run by Timer background thread).<br>
 * Gets DIH params and sets default values, where appropriate.<br>
 * Uses DIH params to assemble complete URL.<br>
 * Invokes URL using HTTP POST request.<br>
 * For more info:<br>
 * http://wiki.apache.org/solr/DataImportHandler#HTTPPostScheduler
 * @author mbonaci
 */
public class HttpPostScheduler extends TimerTask {
        private String syncEnabled;
        private String[] syncCores;
        private String server;
        private String port;
        private String webapp;
        private String params;
        private String interval;
        private String cores;
        private SolrDataImportProperties p;
        private boolean singleCore;

        private static final Logger logger = LoggerFactory.getLogger(HttpPostScheduler.class);

        public HttpPostScheduler(String webAppName, Timer t) throws Exception{
                //load properties from global dataimport.properties
                p = new SolrDataImportProperties();
                reloadParams();
                fixParams(webAppName);

                if(!syncEnabled.equals("1")) throw new Exception("Schedule disabled");

                if(syncCores == null || (syncCores.length == 1 && syncCores[0].isEmpty())){
                        singleCore = true;
                        logger.info("<index update process> Single core identified in dataimport.properties");
                }else{
                        singleCore = false;
                        logger.info("<index update process> Multiple cores identified in dataimport.properties. Sync active for: " + cores);
                }
        }

        private void reloadParams(){
                p.loadProperties(true);
                syncEnabled = p.getProperty(SolrDataImportProperties.SYNC_ENABLED);
                cores           = p.getProperty(SolrDataImportProperties.SYNC_CORES);
                server          = p.getProperty(SolrDataImportProperties.SERVER);
                port            = p.getProperty(SolrDataImportProperties.PORT);
                webapp          = p.getProperty(SolrDataImportProperties.WEBAPP);
                params          = p.getProperty(SolrDataImportProperties.PARAMS);
                interval        = p.getProperty(SolrDataImportProperties.INTERVAL);
                syncCores       = cores != null ? cores.split(",") : null;
        }

        private void fixParams(String webAppName){
                if(server == null || server.isEmpty())  server = "localhost";
                if(port == null || port.isEmpty())              port = "8080";
                if(webapp == null || webapp.isEmpty())  webapp = webAppName;
                if(interval == null || interval.isEmpty() || getIntervalInt() <= 0) interval = "30";
        }

        public void run() {
                try{
                        // check mandatory params
                        if(server.isEmpty() || webapp.isEmpty() || params == null || params.isEmpty()){
                                logger.warn("<index update process> Insuficient info provided for data import");
                                logger.info("<index update process> Reloading global dataimport.properties");
                                reloadParams();

                        // single-core
                        }else if(singleCore){
                                prepUrlSendHttpPost();

                        // multi-core
                        }else if(syncCores.length == 0 || (syncCores.length == 1 && syncCores[0].isEmpty())){
                                logger.warn("<index update process> No cores scheduled for data import");
                                logger.info("<index update process> Reloading global dataimport.properties");
                                reloadParams();

                        }else{
                                for(String core : syncCores){
                                        prepUrlSendHttpPost(core);
                                }
                        }
                }catch(Exception e){
                        logger.error("Failed to prepare for sendHttpPost", e);
                        reloadParams();
                }
        }


        private void prepUrlSendHttpPost(){
                String coreUrl = "http://" + server + ":" + port + "/" + webapp + params;
                sendHttpPost(coreUrl, null);
        }

        private void prepUrlSendHttpPost(String coreName){
                String coreUrl = "http://" + server + ":" + port + "/" + webapp + "/" + coreName + params;
                sendHttpPost(coreUrl, coreName);
        }


        private void sendHttpPost(String completeUrl, String coreName){
                DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss SSS");
                Date startTime = new Date();

                // prepare the core var
                String core = coreName == null ? "" : "[" + coreName + "] ";

                logger.info(core + "<index update process> Process started at .............. " + df.format(startTime));

                try{

                    URL url = new URL(completeUrl);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("type", "submit");
                    conn.setDoOutput(true);

                        // Send HTTP POST
                    conn.connect();

                    logger.info(core + "<index update process> Request method\t\t\t" + conn.getRequestMethod());
                    logger.info(core + "<index update process> Succesfully connected to server\t" + server);
                    logger.info(core + "<index update process> Using port\t\t\t" + port);
                    logger.info(core + "<index update process> Application name\t\t\t" + webapp);
                    logger.info(core + "<index update process> URL params\t\t\t" + params);
                    logger.info(core + "<index update process> Full URL\t\t\t\t" + conn.getURL());
                    logger.info(core + "<index update process> Response message\t\t\t" + conn.getResponseMessage());
                    logger.info(core + "<index update process> Response code\t\t\t" + conn.getResponseCode());

                    //listen for change in properties file if an error occurs
                    if(conn.getResponseCode() != 200){
                        reloadParams();
                    }

                    conn.disconnect();
                    logger.info(core + "<index update process> Disconnected from server\t\t" + server);
                    Date endTime = new Date();
                    logger.info(core + "<index update process> Process ended at ................ " + df.format(endTime));
                }catch(MalformedURLException mue){
                        logger.error("Failed to assemble URL for HTTP POST", mue);
                }catch(IOException ioe){
                        logger.error("Failed to connect to the specified URL while trying to send HTTP POST", ioe);
                }catch(Exception e){
                        logger.error("Failed to send HTTP POST", e);
                }
        }

        public int getIntervalInt() {
                try{
                        return Integer.parseInt(interval);
                }catch(NumberFormatException e){
                        logger.warn("Unable to convert 'interval' to number. Using default value (30) instead", e);
                        return 30; //return default in case of error
                }
        }
}