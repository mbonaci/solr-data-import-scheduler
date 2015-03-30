## Apache Solr DataImportHandler Scheduler
Used to schedule recurring data imports into Apache Solr (from e.g. RDBMS, JSON files, ...).

## And the Solr is... what exactly?
Solr is a very popular, blindingly fast open source enterprise search platform that originated inside Doug Cutting's Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, dynamic clustering,
database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
Solr is highly scalable, providing distributed search and index replication, and it powers the search and
navigation features of many of the world's largest internet sites.

## About
I wrote DIH Scheduler for myself, as I needed to periodically import changes made in MS SQL Server.
The Solr app was deployed on Windows Server (I know, it wasn't up to me), so I didn't have the option of using a simple cron job.  
In 2010 I published the original source in the [Solr Wiki](https://wiki.apache.org/solr/DataImportHandler#Scheduling),
and soon after more and more people started asking for a compiled version so they can just drop in a JAR file in their Solr's `lib` folder, 
customize their scheduler params, add a single line (filter) into `web.xml` and start using it.

You can find the jar file in the [release](./release) folder.

Feel free to ask a questions or give a suggestion (file an [issue](https://github.com/mbonaci/solr-data-import-scheduler/issues)).

## About DIH Scheduler
 - Version 1.2
 - Last revision: 20.09.2010.
 - Author: Marko Bonaci
 - [Jira ticket](http://issues.apache.org/jira/browse/SOLR-2305)
 - Enables scheduling DIH delta or full imports
 - It uses Solr's REST API to send POST request to DIH
 - Successfully tested on Apache Tomcat v6 (should work on any other servlet container)
 - Hasn't landed upstream (see the Jira ticket for the targeted release)


## TODO:

 - enable user to create multiple scheduled tasks (`List<DataImportScheduler>`)
 - add `cancel` functionality (to be able to completely disable _DIHScheduler_ background thread, without stopping the app/server).
 Currently, sync can be disabled by setting `syncEnabled` param to anything other than `"1"` in `dataimport.properties`, 
 but the background thread still remains active and reloads the properties file on every run (so that sync can be hot-redeployed)


## Prereqs

 - working DIH configuration in place 
 - dataimport.properties file in folder solr.home/conf/ with mandatory params inside (see bellow for the example of dataimport.properties) 
 - ApplicationListener declared in Solr's web.xml (see bellow for more info) 
 - Built (or downloaded) jar file placed to solr.war's web-inf\lib folder before war file is deployed

## Revisions:

### v1.2:
 - became core-aware (now works regardless of whether single or multi-core Solr is deployed)
 - parametrized the schedule interval (in minutes)
 
### v1.1:
 - now using SolrResourceLoader to get solr.home (as opposed to System properties in v1.0)
 - forces reloading of the properties file if the response code is not 200
 - logging done using slf4j (used System.out in v1.0)

### v1.0:
 - initial release
 
 
<small>
  Automatically exported from code.google.com/p/solr-data-import-scheduler
</small>
