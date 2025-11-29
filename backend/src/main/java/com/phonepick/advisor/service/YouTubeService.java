package com.phonepick.advisor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.phonepick.advisor.model.YouTubeChannel;
import com.phonepick.advisor.repository.YouTubeChannelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for YouTube video discovery and transcript fetching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeService {
    
    private final YouTubeChannelRepository channelRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${youtube.api.key:}")
    private String youtubeApiKey;
    
    @Value("${youtube.transcript.api.url:https://youtube-transcript-api.fly.dev/transcript}")
    private String transcriptApiUrl;
    
    /**
     * Check if a video exists for a phone model from credible channels
     * @param phoneModel The phone model to search for
     * @return Map of channel name to video details
     */
    public Map<String, VideoInfo> findVideosForPhone(String phoneModel) {
        List<YouTubeChannel> credibleChannels = channelRepository.findByActiveTrue();
        Map<String, VideoInfo> videoMap = new HashMap<>();
        
        if (credibleChannels.isEmpty()) {
            log.warn("No active YouTube channels configured");
            return videoMap;
        }
        
        for (YouTubeChannel channel : credibleChannels) {
            try {
                VideoInfo videoInfo = searchVideoInChannel(channel.getChannelId(), phoneModel);
                if (videoInfo != null) {
                    videoMap.put(channel.getChannelName(), videoInfo);
                }
            } catch (Exception e) {
                log.error("Error searching channel {}: {}", channel.getChannelName(), e.getMessage());
            }
        }
        
        log.info("Found {} videos for phone: {}", videoMap.size(), phoneModel);
        return videoMap;
    }
    
    /**
     * Search for a video in a specific channel
     */
    private VideoInfo searchVideoInChannel(String channelId, String phoneModel) {
        if (youtubeApiKey == null || youtubeApiKey.isEmpty()) {
            log.warn("YouTube API key not configured");
            return null;
        }
        
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("channelId", channelId)
                .queryParam("q", phoneModel)
                .queryParam("type", "video")
                .queryParam("maxResults", "1")
                .queryParam("key", youtubeApiKey)
                .build()
                .toUriString();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                
                if (!items.isEmpty()) {
                    Map<String, Object> item = items.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> id = (Map<String, Object>) item.get("id");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    
                    String videoId = (String) id.get("videoId");
                    String title = (String) snippet.get("title");
                    
                    return new VideoInfo(videoId, title, 
                        "https://www.youtube.com/watch?v=" + videoId);
                }
            }
        } catch (Exception e) {
            log.error("Error calling YouTube API: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Fetch transcript for a video
     * @param videoId YouTube video ID
     * @return Transcript text or null if unavailable
     */
    public String fetchTranscript(String videoId) {
        try {
            String url = UriComponentsBuilder
                .fromHttpUrl(transcriptApiUrl)
                .queryParam("video_id", videoId)
                .build()
                .toUriString();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("transcript")) {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> transcript = 
                    (List<Map<String, String>>) response.get("transcript");
                
                StringBuilder fullText = new StringBuilder();
                for (Map<String, String> segment : transcript) {
                    fullText.append(segment.get("text")).append(" ");
                }
                
                return fullText.toString().trim();
            }
        } catch (Exception e) {
            log.error("Error fetching transcript for video {}: {}", videoId, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Data class for video information
     */
    public static class VideoInfo {
        private final String videoId;
        private final String title;
        private final String url;
        
        public VideoInfo(String videoId, String title, String url) {
            this.videoId = videoId;
            this.title = title;
            this.url = url;
        }
        
        public String getVideoId() { return videoId; }
        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }
}
