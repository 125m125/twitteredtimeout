package de._125m125.twitteredtimeout;

import java.io.File;
import java.util.List;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.httpclient.jdk.JDKHttpClientConfig;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.rules.FilteredStreamRulePredicate;
import io.github.redouane59.twitter.dto.stream.StreamRules.StreamRule;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.signature.TwitterCredentials;

public class Main {
  public static void main(String[] args) throws Exception {
    TwitterCredentials credentials = TwitterClient.OBJECT_MAPPER
        .readValue(new File("twitterconfig.json"), TwitterCredentials.class);
    TwitterClient client = new TwitterClient(credentials,
        new ServiceBuilder(credentials.getApiKey()).apiSecret(credentials.getApiSecretKey())
            .httpClientConfig(JDKHttpClientConfig
                // timeout 20 seconds
                .defaultConfig().withConnectTimeout(20000).withReadTimeout(20000))
            .build(TwitterApi.instance()));

    List<StreamRule> rules = client.retrieveFilteredStreamRules();
    if (rules != null) {
      for (StreamRule rule : rules) {
        client.deleteFilteredStreamRuleId(rule.getId());
      }
    }
    // tweets every 3 minutes. A currently trending hashtag makes it more obvious
    // client.addFilteredStreamRule(FilteredStreamRulePredicate.withUser("Every3Minutes"), "test");
    client.addFilteredStreamRule(FilteredStreamRulePredicate.withHashtag("BTSat2021TMA"), "test");
    client.startFilteredStream(new IAPIEventListener() {

      public void onUnknownDataStreamed(String json) {
        System.out.println("unknown: " + json);
      }

      public void onTweetStreamed(Tweet tweet) {
        System.out.println("new Tweet: " + tweet.getId());
      }

      public void onStreamError(int httpCode, String error) {
        System.out.println("error " + httpCode + ": " + error);
      }

      public void onStreamEnded(Exception e) {
        System.out.println("ended: " + e.getMessage());
      }

      // requires https://github.com/125m125/twittered (2.10-125m125-SNAPSHOT)
      public void receivedInput() {
        System.out.println("Reveived input...");
      }
    });

  }
}
