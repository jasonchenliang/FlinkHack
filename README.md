# FlinkHack
Simple app for tweet stream real-time processing and visualization with Twitter API.
### Requirements
| Tool | Version|
| ------ | ------ |
| Apache Flink | 1.2 |
| Elasticsearch| 2.4.2 | 
| Kibana | 4.5.4 |

Commands to create index and mapping
```sh
curl -XDELETE localhost:9200/flink-twits
```
```sh
curl -XPUT 'http://localhost:9200/flink-twits'
```
```sh
curl -XPUT 'http://localhost:9200/flink-twits/_mapping/twitter-location' --data-ascii '@twitter-location-mapping.txt'
```
### Steps
1. Run Elasticsearch 
1. Import the Index and the Mapping
1. Run Flink 
1. Submit the jar to Flink Cloud 
1. Run Kibana 
1. Import the export.json, set kibana to refresh the dashboard every 5 seconds