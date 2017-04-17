# FlinkHack
Simple app for tweet stream real-time processing and visualization with Twitter API.
### Requirements
| Tool | Version|
| ------ | ------ |
| Apache Flink | [1.2.0](http://archive.apache.org/dist/flink/flink-1.2.0/flink-1.2.0-bin-hadoop2-scala_2.10.tgz) |
| Elasticsearch| [2.4.2](https://www.elastic.co/downloads/past-releases/elasticsearch-2-4-2) | 
| Kibana | [4.5.4](https://github.com/elastic/kibana/archive/v4.5.4.zip) |

### Steps
1. Start Elasticsearch 
* run `bin/elasticsearch` on Unix, or `bin\elasticsearch.bat` on Windows

2. Import the Index and the Mapping to Elasticsearch
```sh
curl -XDELETE localhost:9200/flink-twits
curl -XPUT 'http://localhost:9200/flink-twits'
curl -XPUT 'http://localhost:9200/flink-twits/_mapping/twitter-location' --data-ascii '@data/twitter-location-mapping.txt'
```
3. Package the FlinkHack
  ```sh
  mvn clean package
  ```
4. Start Flink
* run `bin/start-local` on Unix, or `bin\start-local.bat` on Windows
* visit [http://localhost:8081](http://localhost:8081)
* submit the new Job with `FlinkHack-1.0-SNAPSHOT.jar`
5. Start Kibana 
* run `bin/kibana` on Unix, or `bin\kibana.bat` on Windows
* visit [http://localhost:5601](http://localhost:5601)
6. Import the data/export.json, set Kibana to refresh the dashboard every 5 seconds