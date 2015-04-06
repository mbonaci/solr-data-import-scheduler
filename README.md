## Apache Solr DataImportHandler Scheduler
Used to schedule recurring data imports into Apache Solr (from e.g. RDBMS, JSON files, ...).

## And the Solr is... what exactly?
Solr is a very popular, blindingly fast open source enterprise search platform that originated inside Doug Cutting's Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, dynamic clustering,
database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
Solr is highly scalable, providing distributed search and index replication, and it powers the search and
navigation features of many of the world's largest internet sites.

## About
I wrote DIH Scheduler for myself, as I needed to periodically index changes made in MS SQL Server.
The Solr app was deployed on Windows Server (I know, it wasn't up to me), so I didn't have the option of using a simple cron job.  
In 2010 I published the original source in the [Solr Wiki](https://wiki.apache.org/solr/DataImportHandler#Scheduling),
and soon after more and more people started asking for a compiled version so they can just drop in a JAR file and be done with it.

## Prereqs
 - working DIH configuration in place 

## Install
#### Important! There's currently [a bug](https://github.com/mbonaci/solr-data-import-scheduler/issues/1) in the jar file, so you'll have to build it yourself, from the provided source (until I get some [free time](http://www.manning.com/bonaci))
 1. download the jar file [here](https://github.com/mbonaci/solr-data-import-scheduler/raw/master/release/dihs.jar) (it's in the [release](./release) folder).
 2. place the jar file into the `web-inf/lib` folder, inside your WAR (before deployment), or into `lib` folder inside (already deployed) Solr's root
 3. copy the contents of [dataimport.properties](./conf/dataimport.properties) file (everything bellow `last_index_time`) in your existing `dataimport.properties`. Make sure, regardless of whether you have single or multi-core Solr, that you use `dataimport.properties` located in your `solr.home/conf` (NOT `solr.home/core/conf`)
 6. customize the synchronization schedule and other mandatory params inside `dataimport.properties`
 4. add the following snippet into your WAR/EAR's `web.xml`:

```xml
<listener>
  <listener-class>org.apache.solr.handler.dataimport.scheduler.ApplicationListener</listener-class>
</listener>
```

Restart the Solr web app to apply changes.

## Additional info
 - Enables scheduling DIH delta or full imports
 - It uses Solr's REST API to send POST request to DIH
 - Successfully tested on Apache Tomcat v6 (should work on any other servlet container)
 - [Jira ticket](http://issues.apache.org/jira/browse/SOLR-2305)
 - Hasn't landed upstream (see the Jira ticket for the targeted release)


Feel free to ask a question or give a suggestion (file an [issue](https://github.com/mbonaci/solr-data-import-scheduler/issues)).


## Would accept pull request:

 - enable user to create multiple scheduled tasks (`List<DataImportScheduler>`)
 - add `cancel` functionality (to be able to completely disable _DIHScheduler_ background thread, without stopping the app/server).
 Currently, sync can be disabled by setting `syncEnabled` param to anything other than `"1"` in `dataimport.properties`, 
 but the background thread still remains active and reloads the properties file on every run (so that sync can be hot-redeployed).


## Changelog:

### v1.2 (2010.09.20):
 - became core-aware (now works regardless of whether single or multi-core Solr is deployed)
 - parameterized the schedule interval (in minutes)
 
### v1.1:
 - use `SolrResourceLoader` to get `solr.home` (instead of `System` properties in v1.0)
 - forces reloading of the properties file if the response code is not `200`
 - use _slf4j_ for logging

### v1.0:
 - initial release
 
 
<small>
  Automatically exported from code.google.com/p/solr-data-import-scheduler
</small>
