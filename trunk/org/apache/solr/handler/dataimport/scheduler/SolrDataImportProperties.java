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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to load settings from global dataimport.properties<br>
 * See here for more info:<br>
 * http://wiki.apache.org/solr/DataImportHandler#SolrDataImportProperties
 * @author mbonaci
 */
public class SolrDataImportProperties {
        private Properties properties;

        public static final String SYNC_ENABLED         = "syncEnabled";
        public static final String SYNC_CORES           = "syncCores";
        public static final String SERVER               = "server";
        public static final String PORT                 = "port";
        public static final String WEBAPP               = "webapp";
        public static final String PARAMS               = "params";
        public static final String INTERVAL             = "interval";

        private static final Logger logger = LoggerFactory.getLogger(SolrDataImportProperties.class);

        public SolrDataImportProperties(){
//              loadProperties(true);
        }

        public void loadProperties(boolean force){
                try{
                        SolrResourceLoader loader = new SolrResourceLoader(null);
                        logger.info("Instance dir = " + loader.getInstanceDir());

                        String configDir = loader.getConfigDir();
                        configDir = SolrResourceLoader.normalizeDir(configDir);
                        if(force || properties == null){
                                properties = new Properties();

                                File dataImportProperties = new File(configDir, "dataimport.properties");

                                FileInputStream fis = new FileInputStream(dataImportProperties);
                                properties.load(fis);
                        }
                }catch(FileNotFoundException fnfe){
                        logger.error("Error locating DataImportScheduler dataimport.properties file", fnfe);
                }catch(IOException ioe){
                        logger.error("Error reading DataImportScheduler dataimport.properties file", ioe);
                }catch(Exception e){
                        logger.error("Error loading DataImportScheduler properties", e);
                }
        }

        public String getProperty(String key){
                return properties.getProperty(key);
        }
}
