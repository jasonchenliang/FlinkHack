# FlinkHack
Simple app for tweet stream real-time processing and visualization with Twitter API, Apache Flink, ElasticSearch and Grafana. 

#command to create index and mapping

curl -XDELETE localhost:9200/flink-twits

curl -XPUT 'http://localhost:9200/flink-twits'
curl -XPUT 'http://localhost:9200/flink-twits/_mapping/twitter-location' --data-ascii '@twitter-location-mapping.txt'
