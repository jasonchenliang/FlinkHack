import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class FlinkHack {

    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);
        final Properties properties = new Properties();
        properties.setProperty(TwitterSource.CONSUMER_KEY, "");
        properties.setProperty(TwitterSource.CONSUMER_SECRET, "");
        properties.setProperty(TwitterSource.TOKEN, "");
        properties.setProperty(TwitterSource.TOKEN_SECRET, "");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setParallelism(params.getInt("parallelism", 1));

        final DataStream<String> streamSource = env.addSource(new TwitterSource(properties));
        DataStream<Tuple4<String, Integer, Double, Double>> dataStream = streamSource.flatMap(new HashtagTokenizeFlatMap())
                .keyBy(0)
                .sum(1);
        dataStream.print();
        env.execute("Tweet streaming");
    }

    public static class HashtagTokenizeFlatMap implements FlatMapFunction<String, Tuple4<String, Integer, Double, Double>> {
        private static final long serialVersionUID = 1L;
        private transient ObjectMapper jsonParser;

        /**
         * Select the language from the incoming JSON text
         */
        @Override
        public void flatMap(String value, Collector<Tuple4<String, Integer, Double, Double>> out) throws Exception {
            if (jsonParser == null) {
                jsonParser = new ObjectMapper();
            }
            JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);
            if (value.contains("created_at")) {//filter delete record tweet
                final boolean hasHashtags = jsonNode.get("entities").get("hashtags").size() > 0;
                //https://dev.twitter.com/overview/api/tweets#obj-coordinates
                final boolean hasGeoCoordinates = jsonNode.get("geo").has("coordinates");
                final boolean hasCoordinatesCoordinates = !jsonNode.get("coordinates").isNull() && jsonNode.get("coordinates").get("coordinates").size() > 0;
                if (hasHashtags && (hasGeoCoordinates || hasCoordinatesCoordinates)) {
                    final double latitude = hasGeoCoordinates ? jsonNode.get("geo").get("coordinates").get(0).asDouble() : jsonNode.get("coordinates").get("coordinates").get(1).asDouble();
                    final double longitude = hasGeoCoordinates ? jsonNode.get("geo").get("coordinates").get(1).asDouble() : jsonNode.get("coordinates").get("coordinates").get(0).asDouble();
                    for (int i = 0; i < jsonNode.get("entities").get("hashtags").size(); i++) {
                        StringTokenizer tokenizer = new StringTokenizer(jsonNode.get("entities").get("hashtags").get(i).get("text").asText());
                        while (tokenizer.hasMoreTokens()) {
                            String result = tokenizer.nextToken().replaceAll("\\s*", "").toLowerCase();
                            if (!result.equals("")) {
                                out.collect(new Tuple4<>(result, 1, latitude, longitude));
                            }
                        }
                    }
                }
            }
        }
    }
}
