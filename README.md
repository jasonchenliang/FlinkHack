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
1> Run Flink
2> Run Elasticsearch
3> Import the Index and the Mapping
4> Run Kibana
5> Import the export.jason