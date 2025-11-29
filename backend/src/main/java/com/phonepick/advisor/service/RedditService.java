package com.phonepick.advisor.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepick.advisor.model.Phone;
import com.phonepick.advisor.model.RedditPost;
import com.phonepick.advisor.repository.PhoneRepository;
import com.phonepick.advisor.repository.RedditPostRepository;

@Service
public class RedditService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedditService.class);
    private static final String REDDIT_BASE_URL = "https://www.reddit.com";
    private static final List<String> TARGET_SUBREDDITS = Arrays.asList(
        "Android",
        "smartphones",
        "PickAnAndroidForMe",
        "AndroidQuestions",
        "smartphone",
        "IndianGaming"
    );
    
    private final WebClient webClient;
    private final RedditPostRepository redditPostRepository;
    private final PhoneRepository phoneRepository;
    private final ObjectMapper objectMapper;
    
    public RedditService(
            WebClient.Builder webClientBuilder,
            RedditPostRepository redditPostRepository,
            PhoneRepository phoneRepository,
            ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(REDDIT_BASE_URL).build();
        this.redditPostRepository = redditPostRepository;
        this.phoneRepository = phoneRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Search Reddit for posts about a specific phone model
     */
    public List<RedditPost> searchRedditForPhone(String phoneModel) {
        List<RedditPost> allPosts = new ArrayList<>();
        
        for (String subreddit : TARGET_SUBREDDITS) {
            try {
                List<RedditPost> posts = searchSubreddit(subreddit, phoneModel);
                allPosts.addAll(posts);
                
                // Rate limiting - be respectful to Reddit API
                Thread.sleep(2000);
            } catch (Exception e) {
                logger.error("Error searching subreddit {}: {}", subreddit, e.getMessage());
            }
        }
        
        return allPosts;
    }
    
    /**
     * Search a specific subreddit for phone mentions
     */
    private List<RedditPost> searchSubreddit(String subreddit, String phoneModel) {
        List<RedditPost> posts = new ArrayList<>();
        
        try {
            String url = String.format("/r/%s/search.json?q=%s&restrict_sr=1&sort=relevance&limit=25",
                    subreddit, phoneModel.replace(" ", "+"));
            
            String response = webClient.get()
                    .uri(url)
                    .header("User-Agent", "SmartPick/1.0")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            if (response != null) {
                posts = parseRedditResponse(response, phoneModel);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching from r/{}: {}", subreddit, e.getMessage());
        }
        
        return posts;
    }
    
    /**
     * Parse Reddit API JSON response
     */
    private List<RedditPost> parseRedditResponse(String jsonResponse, String phoneModel) {
        List<RedditPost> posts = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode children = root.path("data").path("children");
            
            // Use findByModelContainingIgnoreCase instead of findByModelNameContainingIgnoreCase
            Phone phone = phoneRepository.findByModelContainingIgnoreCase(phoneModel)
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            if (phone == null) {
                logger.warn("Phone not found in database: {}", phoneModel);
                return posts;
            }
            
            for (JsonNode child : children) {
                JsonNode data = child.path("data");
                
                String postId = data.path("id").asText();
                
                // Skip if already exists
                if (redditPostRepository.findByPostId(postId).isPresent()) {
                    continue;
                }
                
                RedditPost post = new RedditPost();
                post.setPhone(phone);
                post.setPostId(postId);
                post.setPostTitle(data.path("title").asText());
                post.setPostUrl("https://reddit.com" + data.path("permalink").asText());
                post.setAuthor(data.path("author").asText());
                post.setSubreddit(data.path("subreddit").asText());
                post.setContent(data.path("selftext").asText());
                post.setUpvotes(data.path("ups").asInt());
                post.setNumComments(data.path("num_comments").asInt());
                
                long createdUtc = data.path("created_utc").asLong();
                post.setPostCreatedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(createdUtc),
                        ZoneId.systemDefault()));
                
                posts.add(post);
            }
            
        } catch (Exception e) {
            logger.error("Error parsing Reddit response: {}", e.getMessage());
        }
        
        return posts;
    }
    
    /**
     * Batch fetch Reddit posts for all phones
     */
    public void batchFetchRedditPosts() {
        logger.info("Starting batch Reddit post fetch...");
        
        List<Phone> phones = phoneRepository.findAll();
        int totalFetched = 0;
        
        for (Phone phone : phones) {
            try {
                // Use phone.getModel() instead of phone.getModelName()
                List<RedditPost> posts = searchRedditForPhone(phone.getModel());
                
                for (RedditPost post : posts) {
                    redditPostRepository.save(post);
                    totalFetched++;
                }
                
                logger.info("Fetched {} posts for {}", posts.size(), phone.getModel());
                
                // Rate limiting between phones
                Thread.sleep(3000);
                
            } catch (Exception e) {
                logger.error("Error fetching Reddit posts for {}: {}", phone.getModel(), e.getMessage());
            }
        }
        
        logger.info("Batch Reddit fetch complete. Total posts fetched: {}", totalFetched);
    }
    
    /**
     * Get Reddit posts for a specific phone
     */
    public List<RedditPost> getPostsForPhone(Long phoneId) {
        return redditPostRepository.findByPhoneId(phoneId);
    }
    
    /**
     * Get average sentiment score from Reddit posts
     */
    public Double getAverageSentimentScore(Long phoneId) {
        return redditPostRepository.getAverageSentimentScoreByPhoneId(phoneId);
    }
}
