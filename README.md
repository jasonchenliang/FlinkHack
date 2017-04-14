# FlinkHack
Simple app for tweet stream real-time processing and visualization with Twitter API, Apache Flink, ElasticSearch and Kibana.
Flink 1.2
Elasticsearch 2.4.2
Kibana 4.5.4

#command to create index and mapping
curl -XDELETE localhost:9200/flink-twits

curl -XPUT 'http://localhost:9200/flink-twits'
curl -XPUT 'http://localhost:9200/flink-twits/_mapping/twitter-location' --data-ascii '@twitter-location-mapping.txt'

Steps:
1> Run Elasticsearch
2> Import the Index and the Mapping
3> Run Flink
4> Submit the jar to Flink Cloud
5> Run Kibana
6> Import the export.jason, set kibana to refresh the dashboard every 5 seconds