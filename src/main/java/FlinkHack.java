import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
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
        DataStream<Tuple2<String, Integer>> dataStream = streamSource.flatMap(new HashtagTokenizeFlatMap())
                .keyBy(0)
                .timeWindow(Time.seconds(5))
                .sum(1);
        dataStream.print();
        env.execute("Tweet streaming");
    }

    public static class HashtagTokenizeFlatMap implements FlatMapFunction<String, Tuple2<String, Integer>> {
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
            final boolean isEnglish = jsonNode.has("user")
                    && jsonNode.get("user").has("lang")
                    && jsonNode.get("user").get("lang").asText().equalsIgnoreCase("en");
            final boolean hasHashtags = jsonNode.has("entities")
                    && jsonNode.get("entities").has("hashtags")
                    && jsonNode.get("entities").get("hashtags").size() > 0;
            if (isEnglish && hasHashtags) {
                for (int i = 0; i < jsonNode.get("entities").get("hashtags").size(); i++) {
                    StringTokenizer tokenizer = new StringTokenizer(jsonNode.get("entities").get("hashtags").get(i).get("text").asText());
                    while (tokenizer.hasMoreTokens()) {
                        String result = tokenizer.nextToken().replaceAll("\\s*", "").toLowerCase();
                        if (!result.equals("")) {
                            out.collect(new Tuple2<>(result, 1));
                        }
                    }
                }
            }
        }
    }
//  {
//    "created_at":"Wed Apr 05 05:26:58 +0000 2017",
//          "id":849493545207681024,
//          "id_str":"849493545207681024",
//          "text":"RT @MsgsForYou: I hate you and I love you.",
//          "source":"<a href=\"https://about.twitter.com/products/tweetdeck\" rel=\"nofollow\">TweetDeck</a>",
//          "truncated":false,
//          "in_reply_to_status_id":null,
//          "in_reply_to_status_id_str":null,
//          "in_reply_to_user_id":null,
//          "in_reply_to_user_id_str":null,
//          "in_reply_to_screen_name":null,
//          "user":{
//    "id":2160397254,
//            "id_str":"2160397254",
//            "name":"Simpleng Hugot ™",
//            "screen_name":"SimplingHugot",
//            "location":"Republic of the Philippines",
//            "url":null,
//            "description":"Simple lang mga hugot ko dito pero siguradong makakarelate ka! Expressing 140 character or less | Owner: @HncAltvrs | Follow Us  ~ Phillipines",
//            "protected":false,
//            "verified":false,
//            "followers_count":32271,
//            "friends_count":6097,
//            "listed_count":28,
//            "favourites_count":445,
//            "statuses_count":7176,
//            "created_at":"Mon Oct 28 07:28:24 +0000 2013",
//            "utc_offset":28800,
//            "time_zone":"Irkutsk",
//            "geo_enabled":false,
//            "lang":"en",
//            "contributors_enabled":false,
//            "is_translator":false,
//            "profile_background_color":"C0DEED",
//            "profile_background_image_url":"http://pbs.twimg.com/profile_background_images/378800000104450285/0d195440403c9c06029c9246b3093be7.jpeg",
//            "profile_background_image_url_https":"https://pbs.twimg.com/profile_background_images/378800000104450285/0d195440403c9c06029c9246b3093be7.jpeg",
//            "profile_background_tile":true,
//            "profile_link_color":"3B94D9",
//            "profile_sidebar_border_color":"FFFFFF",
//            "profile_sidebar_fill_color":"DDEEF6",
//            "profile_text_color":"333333",
//            "profile_use_background_image":true,
//            "profile_image_url":"http://pbs.twimg.com/profile_images/713649969807409154/okqodRL0_normal.jpg",
//            "profile_image_url_https":"https://pbs.twimg.com/profile_images/713649969807409154/okqodRL0_normal.jpg",
//            "profile_banner_url":"https://pbs.twimg.com/profile_banners/2160397254/1458982562",
//            "default_profile":false,
//            "default_profile_image":false,
//            "following":null,
//            "follow_request_sent":null,
//            "notifications":null
//  },
//    "geo":null,
//          "coordinates":null,
//          "place":null,
//          "contributors":null,
//          "retweeted_status":{
//    "created_at":"Tue Apr 04 09:07:11 +0000 2017",
//            "id":849186575816638464,
//            "id_str":"849186575816638464",
//            "text":"I hate you and I love you.",
//            "source":"<a href=\"http://twitter.com/download/android\" rel=\"nofollow\">Twitter for Android</a>",
//            "truncated":false,
//            "in_reply_to_status_id":null,
//            "in_reply_to_status_id_str":null,
//            "in_reply_to_user_id":null,
//            "in_reply_to_user_id_str":null,
//            "in_reply_to_screen_name":null,
//            "user":{
//      "id":1255681598,
//              "id_str":"1255681598",
//              "name":"FEELINGS.",
//              "screen_name":"MsgsForYou",
//              "location":"International",
//              "url":null,
//              "description":"Tweeting random messages to everyone. DM us now! TURN ON NOTIFICATIONS!",
//              "protected":false,
//              "verified":false,
//              "followers_count":112402,
//              "friends_count":2,
//              "listed_count":68,
//              "favourites_count":62,
//              "statuses_count":7377,
//              "created_at":"Sun Mar 10 00:07:12 +0000 2013",
//              "utc_offset":28800,
//              "time_zone":"Beijing",
//              "geo_enabled":false,
//              "lang":"en",
//              "contributors_enabled":false,
//              "is_translator":false,
//              "profile_background_color":"3B94D9",
//              "profile_background_image_url":"http://pbs.twimg.com/profile_background_images/568359759137353729/OdguztHD.jpeg",
//              "profile_background_image_url_https":"https://pbs.twimg.com/profile_background_images/568359759137353729/OdguztHD.jpeg",
//              "profile_background_tile":true,
//              "profile_link_color":"DD2E44",
//              "profile_sidebar_border_color":"000000",
//              "profile_sidebar_fill_color":"E2EAEF",
//              "profile_text_color":"6C961C",
//              "profile_use_background_image":true,
//              "profile_image_url":"http://pbs.twimg.com/profile_images/820226815583096833/WSQLGx9U_normal.jpg",
//              "profile_image_url_https":"https://pbs.twimg.com/profile_images/820226815583096833/WSQLGx9U_normal.jpg",
//              "profile_banner_url":"https://pbs.twimg.com/profile_banners/1255681598/1484392290",
//              "default_profile":false,
//              "default_profile_image":false,
//              "following":null,
//              "follow_request_sent":null,
//              "notifications":null
//    },
//    "geo":null,
//            "coordinates":null,
//            "place":null,
//            "contributors":null,
//            "is_quote_status":false,
//            "retweet_count":403,
//            "favorite_count":431,
//            "entities":{
//      "hashtags":[
//
//         ],
//      "urls":[
//
//         ],
//      "user_mentions":[
//
//         ],
//      "symbols":[
//
//         ]
//    },
//    "favorited":false,
//            "retweeted":false,
//            "filter_level":"low",
//            "lang":"en"
//  },
//    "is_quote_status":false,
//          "retweet_count":0,
//          "favorite_count":0,
//          "entities":{
//    "hashtags":[
//
//      ],
//    "urls":[
//
//      ],
//    "user_mentions":[
//    {
//      "screen_name":"MsgsForYou",
//            "name":"FEELINGS.",
//            "id":1255681598,
//            "id_str":"1255681598",
//            "indices":[
//      3,
//              14
//            ]
//    }
//      ],
//    "symbols":[
//
//      ]
//  },
//    "favorited":false,
//          "retweeted":false,
//          "filter_level":"low",
//          "lang":"en",
//          "timestamp_ms":"1491370018661"
//  }
}
