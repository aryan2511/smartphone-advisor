/**
 * YouTube Review Sync Service - FIXED VERSION
 * 
 * Fixes:
 * 1. Removed Technical Guruji (Hindi content)
 * 2. Fixed TranscriptAPI to use v2 API (GET method)
 * 3. Better error handling
 */

require('dotenv').config();
const { google } = require('googleapis');
const axios = require('axios');
const Sentiment = require('sentiment');
const db = require('./db');

const youtube = google.youtube({
  version: 'v3',
  auth: process.env.YOUTUBE_API_KEY
});

const sentiment = new Sentiment();

// TranscriptAPI v2 configuration
const TRANSCRIPT_API_URL = 'https://transcriptapi.com/api/v2/youtube/transcript';
const TRANSCRIPT_API_KEY = process.env.TRANSCRIPT_API_KEY;

// Trusted YouTuber channel IDs (English only)
const TRUSTED_CHANNELS = [
  'UCOhHO2ICt0ti9KAh-QHvttQ', // Mrwhosetheboss
  'UCBJycsmduvYEL83R_U4JriQ', // MKBHD
  'UC7cs6Hdf2JWPV_rRgvg-wSg', // Trakin Tech
  'UCf_suVenvfMZ4JYSbmalKNQ', // Geeky Ranjit
  'UCYSt6V_ta00dS_g52MliaIg', // C4ETech
  'UCDLUxbvomVR-TdBnLXM4p3Q', // Beebom
  'UCxvLs6GdK4HLj4JOoGqvsLg', // TechBar
  'UCdp6GUwjKscp5ST4M4WgIpw'  // TechWiser
];

async function searchPhoneReviews(phoneBrand, phoneModel) {
  const searchQuery = `${phoneBrand} ${phoneModel} review`;
  
  console.log(`🔍 Searching YouTube for: "${searchQuery}"`);
  
  try {
    const response = await youtube.search.list({
      part: 'snippet',
      q: searchQuery,
      type: 'video',
      maxResults: 5,
      order: 'relevance',
      relevanceLanguage: 'en',
      safeSearch: 'none',
      videoDuration: 'medium'
    });
    
    console.log(`   📊 Total YouTube results: ${response.data.items.length}`);
    
    const videos = response.data.items.filter(item => {
      return TRUSTED_CHANNELS.includes(item.snippet.channelId);
    });
    
    console.log(`   ✅ Found ${videos.length} videos from trusted channels`);
    
    if (videos.length > 0) {
      videos.forEach(v => {
        console.log(`      📹 ${v.snippet.channelTitle}: "${v.snippet.title}"`);
      });
    }
    
    return videos;
    
  } catch (error) {
    console.error('❌ YouTube search error:', error.message);
    return [];
  }
}

async function getVideoStats(videoId) {
  try {
    const response = await youtube.videos.list({
      part: 'statistics',
      id: videoId
    });
    
    const stats = response.data.items[0]?.statistics;
    return {
      viewCount: parseInt(stats?.viewCount || 0),
      likeCount: parseInt(stats?.likeCount || 0)
    };
  } catch (error) {
    console.error(`❌ Error fetching stats for ${videoId}:`, error.message);
    return { viewCount: 0, likeCount: 0 };
  }
}

/**
 * Fetch transcript using TranscriptAPI v2 (GET method)
 */
async function fetchTranscript(videoId) {
  try {
    console.log(`   📝 Fetching transcript via TranscriptAPI v2...`);
    
    const response = await axios.get(TRANSCRIPT_API_URL, {
      params: {
        video_url: videoId,
        format: 'text',
        include_timestamp: false
      },
      headers: {
        'Authorization': `Bearer ${TRANSCRIPT_API_KEY}`
      },
      timeout: 30000
    });
    
    if (response.data && response.data.transcript) {
      const transcript = response.data.transcript;
      console.log(`   ✅ Transcript fetched (${transcript.length} characters)`);
      return transcript;
    }
    
    console.log('   ⚠️  Unexpected response format');
    return null;
    
  } catch (error) {
    if (error.response) {
      const status = error.response.status;
      const errorData = error.response.data;
      
      if (status === 404) {
        console.error(`   ⚠️  No transcript available`);
      } else if (status === 402) {
        console.error(`   ❌ Out of credits!`);
        console.error(`   💡 ${errorData.detail?.action_url || 'Add credits'}`);
      } else if (status === 429) {
        const retryAfter = error.response.headers['retry-after'] || '60';
        console.error(`   ⏳ Rate limited. Retry after ${retryAfter}s`);
      } else {
        console.error(`   ❌ Error (${status}): ${errorData.detail || error.message}`);
      }
    } else {
      console.error(`   ❌ Error: ${error.message}`);
    }
    return null;
  }
}

function analyzeSentiment(transcriptText) {
  if (!transcriptText) return null;
  
  const cleanText = transcriptText
    .toLowerCase()
    .replace(/[^\w\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
  
  const result = sentiment.analyze(cleanText);
  const normalizedScore = Math.max(-100, Math.min(100, result.score * 5));
  
  return {
    score: Math.round(normalizedScore),
    comparative: result.comparative,
    positiveWords: result.positive,
    negativeWords: result.negative
  };
}

function extractInsights(transcriptText) {
  if (!transcriptText) return { positive: [], negative: [], summary: '' };
  
  const text = transcriptText.toLowerCase();
  
  const positivePatterns = [
    { pattern: /great camera|excellent camera|amazing camera|camera is good|camera quality|superb camera/, insight: 'Great camera quality' },
    { pattern: /good battery|excellent battery|amazing battery|battery life is good|long battery/, insight: 'Excellent battery life' },
    { pattern: /fast performance|smooth performance|powerful|fast processor|snappy/, insight: 'Fast and smooth performance' },
    { pattern: /premium design|beautiful design|good build quality|premium feel|solid build/, insight: 'Premium design and build' },
    { pattern: /value for money|worth the price|good price|affordable|bang for buck/, insight: 'Good value for money' },
    { pattern: /great display|excellent screen|beautiful display|good screen|vibrant display/, insight: 'Excellent display quality' },
    { pattern: /fast charging|quick charging|charges fast|rapid charging/, insight: 'Fast charging support' },
    { pattern: /good speakers|great audio|excellent sound|loud speakers/, insight: 'Good audio quality' }
  ];
  
  const negativePatterns = [
    { pattern: /camera issues|camera problem|disappointing camera|average camera|weak camera/, insight: 'Camera could be better' },
    { pattern: /battery drain|poor battery|bad battery|battery life issues|short battery/, insight: 'Battery life concerns' },
    { pattern: /slow performance|laggy|stutters|performance issues|choppy/, insight: 'Performance issues reported' },
    { pattern: /overheating|heating issues|gets hot|thermal|too hot/, insight: 'Heating issues' },
    { pattern: /expensive|overpriced|too costly|not worth|pricey/, insight: 'Expensive for what it offers' },
    { pattern: /cheap build|plastic feel|poor build|feels cheap|flimsy/, insight: 'Build quality could be better' },
    { pattern: /bloatware|too many ads|ui issues|software bugs|buggy/, insight: 'Software needs improvement' },
    { pattern: /no headphone jack|no expandable storage|no wireless charging|missing features/, insight: 'Missing some features' }
  ];
  
  const positivePoints = positivePatterns
    .filter(p => p.pattern.test(text))
    .map(p => p.insight)
    .slice(0, 5);
  
  const negativePoints = negativePatterns
    .filter(p => p.pattern.test(text))
    .map(p => p.insight)
    .slice(0, 5);
  
  let summary = '';
  if (positivePoints.length > negativePoints.length) {
    summary = 'Overall positive review with some minor concerns';
  } else if (negativePoints.length > positivePoints.length) {
    summary = 'Mixed review with several concerns raised';
  } else {
    summary = 'Balanced review highlighting both pros and cons';
  }
  
  return { positive: positivePoints, negative: negativePoints, summary };
}

function determineRecommendation(sentimentScore) {
  if (sentimentScore >= 50) return 'Highly Recommended';
  if (sentimentScore >= 20) return 'Recommended';
  if (sentimentScore >= -20) return 'Mixed';
  if (sentimentScore >= -50) return 'Not Recommended';
  return 'Strongly Not Recommended';
}

async function storeReview(phoneId, videoData, sentimentData, insights) {
  const query = `
    INSERT INTO youtube_reviews (
      phone_id, video_id, channel_name, channel_id, video_title, video_url,
      thumbnail_url, view_count, like_count, published_at,
      sentiment_score, positive_points, negative_points, key_insights,
      recommendation, transcript_available
    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
    ON CONFLICT (phone_id, video_id) DO UPDATE SET
      sentiment_score = EXCLUDED.sentiment_score,
      positive_points = EXCLUDED.positive_points,
      negative_points = EXCLUDED.negative_points,
      key_insights = EXCLUDED.key_insights,
      recommendation = EXCLUDED.recommendation,
      last_updated = CURRENT_TIMESTAMP
    RETURNING id;
  `;
  
  const values = [
    phoneId, videoData.videoId, videoData.channelName, videoData.channelId,
    videoData.title, videoData.url, videoData.thumbnail,
    videoData.viewCount, videoData.likeCount, videoData.publishedAt,
    sentimentData.score, insights.positive, insights.negative,
    insights.summary, determineRecommendation(sentimentData.score),
    sentimentData !== null
  ];
  
  try {
    const result = await db.query(query, values);
    return result.rows[0];
  } catch (error) {
    console.error('   ❌ Error storing review:', error.message);
    return null;
  }
}

async function processVideo(phoneId, videoItem) {
  const videoId = videoItem.id.videoId;
  const snippet = videoItem.snippet;
  
  console.log(`\n   📹 Processing: ${snippet.title}`);
  console.log(`      Channel: ${snippet.channelTitle}`);
  
  try {
    const stats = await getVideoStats(videoId);
    const transcript = await fetchTranscript(videoId);
    
    if (!transcript) {
      console.log('      ⚠️  No transcript available, skipping...');
      return null;
    }
    
    const sentimentData = analyzeSentiment(transcript);
    const insights = extractInsights(transcript);
    
    const videoData = {
      videoId,
      channelName: snippet.channelTitle,
      channelId: snippet.channelId,
      title: snippet.title,
      url: `https://www.youtube.com/watch?v=${videoId}`,
      thumbnail: snippet.thumbnails.high?.url || snippet.thumbnails.default?.url,
      viewCount: stats.viewCount,
      likeCount: stats.likeCount,
      publishedAt: snippet.publishedAt
    };
    
    const result = await storeReview(phoneId, videoData, sentimentData, insights);
    
    if (result) {
      console.log(`      ✅ Stored (Sentiment: ${sentimentData.score})`);
      console.log(`         ${insights.positive.length} positive | ${insights.negative.length} negative`);
    }
    
    return result;
    
  } catch (error) {
    console.error(`      ❌ Error:`, error.message);
    return null;
  }
}

async function syncPhoneReviews(phone) {
  console.log(`\n${'='.repeat(70)}`);
  console.log(`📱 ${phone.brand} ${phone.model}`);
  console.log('='.repeat(70));
  
  try {
    const videos = await searchPhoneReviews(phone.brand, phone.model);
    
    if (videos.length === 0) {
      console.log('⚠️  No videos from trusted channels');
      return { success: 0, failed: 0 };
    }
    
    let successCount = 0;
    let failedCount = 0;
    
    for (const video of videos) {
      const result = await processVideo(phone.id, video);
      
      if (result) {
        successCount++;
      } else {
        failedCount++;
      }
      
      await new Promise(resolve => setTimeout(resolve, 3000));
    }
    
    console.log(`\n   📊 ✅ ${successCount} success | ❌ ${failedCount} failed`);
    
    return { success: successCount, failed: failedCount };
    
  } catch (error) {
    console.error('❌ Error:', error.message);
    return { success: 0, failed: 0 };
  }
}

async function syncAllReviews() {
  console.log('🚀 Starting YouTube review sync...\n');
  
  if (!process.env.YOUTUBE_API_KEY) {
    console.error('❌ YOUTUBE_API_KEY missing');
    process.exit(1);
  }
  
  if (!process.env.TRANSCRIPT_API_KEY) {
    console.error('❌ TRANSCRIPT_API_KEY missing');
    process.exit(1);
  }
  
  try {
    const result = await db.query(`
      SELECT id, brand, model 
      FROM phones 
      ORDER BY brand, model
    `);
    
    const phones = result.rows;
    console.log(`📱 Found ${phones.length} phones\n`);
    
    let totalSuccess = 0;
    let totalFailed = 0;
    
    const phonesToProcess = phones.slice(0, 5);
    
    for (const phone of phonesToProcess) {
      const stats = await syncPhoneReviews(phone);
      totalSuccess += stats.success;
      totalFailed += stats.failed;
      
      await new Promise(resolve => setTimeout(resolve, 5000));
    }
    
    console.log(`\n${'='.repeat(70)}`);
    console.log('📊 Summary');
    console.log('='.repeat(70));
    console.log(`✅ Reviews synced: ${totalSuccess}`);
    console.log(`❌ Failures: ${totalFailed}`);
    console.log(`📱 Phones: ${phonesToProcess.length}`);
    console.log('='.repeat(70) + '\n');
    
    console.log('✅ Done!');
    process.exit(0);
    
  } catch (error) {
    console.error('❌ Failed:', error);
    process.exit(1);
  }
}

syncAllReviews();