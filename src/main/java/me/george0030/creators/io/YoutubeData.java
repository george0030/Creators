package me.george0030.creators.io;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import me.george0030.creators.Creators;

import java.io.IOException;
import java.util.List;

public class YoutubeData {

    private final Creators plugin;
    private final YouTube youtube;

    public YoutubeData(Creators plugin) {
        this.plugin = plugin;
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                })
                .setApplicationName("youtube-cmdline-search-sample")
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(plugin.getConfig().getString("youtube_apikey")))
                .build();
    }

    public long findSubCount(String channelID) throws IOException {

        YouTube.Channels.List channelRequest = youtube.channels().list("snippet, statistics");
        channelRequest.setId(channelID);
        ChannelListResponse response = channelRequest.execute();
        List<Channel> channels = response.getItems();

        if (channels != null && channels.size() == 1) {
            try {
                return channels.get(0).getStatistics().getSubscriberCount().longValueExact();
            } catch (ArithmeticException e) {
                return Long.MAX_VALUE;
            }
        } else return -1L;
    }

}



