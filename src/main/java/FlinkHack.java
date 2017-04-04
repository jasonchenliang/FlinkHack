import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
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
//    final DataStream<String> streamSource = env.fromElements(TwitterExampleData.TEXTS);
    DataStream<Tuple2<String, Integer>> tweets = streamSource.flatMap(new SelectEnglishAndTokenizeFlatMap()).keyBy(0).sum(1);
    tweets.print();
    env.execute("Tweet streaming");
  }

  public static class SelectEnglishAndTokenizeFlatMap implements FlatMapFunction<String, Tuple2<String, Integer>> {
    private static final long serialVersionUID = 1L;
    private transient ObjectMapper jsonParser;

    /**
     * Select the language from the incoming JSON text
     */
    @Override
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
      if (jsonParser == null) {
        jsonParser = new ObjectMapper();
      }
      JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);
      boolean isEnglish = jsonNode.has("user") && jsonNode.get("user").has("lang") &&
          jsonNode.get("user").get("lang").asText().equals("en");
      boolean hasText = jsonNode.has("text");
      if (isEnglish && hasText) {
        // message of tweet
        StringTokenizer tokenizer = new StringTokenizer(jsonNode.get("text").asText());

        // split the message
        while (tokenizer.hasMoreTokens()) {
          String result = tokenizer.nextToken().replaceAll("\\s*", "").toLowerCase();

          if (!result.equals("")) {
            out.collect(new Tuple2<>(result, 1));
          }
        }
      }
    }
  }

  private static class TwitterExampleData {
  	static final String[] TEXTS = new String[] {
  			"{\"created_at\":\"Mon Jan 1 00:00:00 +0000 1901\",\"id\":0,\"id_str\":\"000000000000000000\",\"text\":\"Apache Flink\",\"source\":null,\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":0,\"id_str\":\"0000000000\",\"name\":\"Apache Flink\",\"screen_name\":\"Apache Flink\",\"location\":\"Berlin\",\"protected\":false,\"verified\":false,\"followers_count\":999999,\"friends_count\":99999,\"listed_count\":999,\"favourites_count\":9999,\"statuses_count\":999,\"created_at\":\"Mon Jan 1 00:00:00 +0000 1901\",\"utc_offset\":7200,\"time_zone\":\"Amsterdam\",\"geo_enabled\":false,\"lang\":\"en\",\"entities\":{\"hashtags\":[{\"text\":\"example1\",\"indices\":[0,0]},{\"text\":\"tweet1\",\"indices\":[0,0]}]},\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C6E2EE\",\"profile_background_tile\":false,\"profile_link_color\":\"1F98C7\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"252429\",\"profile_text_color\":\"666666\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null}",
  			"{\"created_at\":\"Mon Jan 1 00:00:00 +0000 1901\",\"id\":1,\"id_str\":\"000000000000000000\",\"text\":\"Apache Flink\",\"source\":null,\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":0,\"id_str\":\"0000000000\",\"name\":\"Apache Flink\",\"screen_name\":\"Apache Flink\",\"location\":\"Berlin\",\"protected\":false,\"verified\":false,\"followers_count\":999999,\"friends_count\":99999,\"listed_count\":999,\"favourites_count\":9999,\"statuses_count\":999,\"created_at\":\"Mon Jan 1 00:00:00 +0000 1901\",\"utc_offset\":7200,\"time_zone\":\"Amsterdam\",\"geo_enabled\":false,\"lang\":\"en\",\"entities\":{\"hashtags\":[{\"text\":\"example2\",\"indices\":[0,0]},{\"text\":\"tweet2\",\"indices\":[0,0]}]},\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C6E2EE\",\"profile_background_tile\":false,\"profile_link_color\":\"1F98C7\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"252429\",\"profile_text_color\":\"666666\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null}",
  			"{\"created_at\":\"Mon Jan 1 00:00:00 +0000 1901\",\"id\":2,\"id_str\":\"000000000000000000\",\"text\":\"Apache Flink\",\"source\":null,\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":0,\"id_str\":\"0000000000\",\"name\":\"Apache Flink\",\"screen_name\":\"Apache Flink\",\"location\":\"Berlin\",\"protected\":false,\"verified\":false,\"followers_count\":999999,\"friends_count\":99999,\"listed_count\":999,\"favourites_count\":9999,\"statuses_count\":999,\"created_at\":\"Mon Jan 1 00:00:00 +0000 1901\",\"utc_offset\":7200,\"time_zone\":\"Amsterdam\",\"geo_enabled\":false,\"lang\":\"en\",\"entities\":{\"hashtags\":[{\"text\":\"example3\",\"indices\":[0,0]},{\"text\":\"tweet3\",\"indices\":[0,0]}]},\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C6E2EE\",\"profile_background_tile\":false,\"profile_link_color\":\"1F98C7\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"252429\",\"profile_text_color\":\"666666\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null}",
  	};

  	public static final String STREAMING_COUNTS_AS_TUPLES = "(apache,1)\n" + "(apache,2)\n" + "(apache,3)\n" + "(flink,1)\n" + "(flink,2)\n" + "(flink,3)\n";

  	private TwitterExampleData() {
  	}
  }
}
