/**
 * YouTube Review Sync - DEBUG VERSION
 * Run this to see what's happening
 */

require('dotenv').config();
const { google } = require('googleapis');
const { YoutubeTranscript } = require('youtube-transcript');
const Sentiment = require('sentiment');
const db = require('./db');

const youtube = google.youtube({
  version: 'v3',
  auth: process.env.YOUTUBE_API_KEY
});

const sentiment = new Sentiment();
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
  
  console.log(`\n🔍 Searching: "${searchQuery}"`);
  
  try {
    const response = await youtube.search.list({
      part: 'snippet',
      q: searchQuery,
      type: 'video',
      maxResults: 15, // Increased from 10
      order: 'relevance',
      relevanceLanguage: 'en',
      videoDuration: 'medium'
    });
    
    console.log(`📊 Total results: ${response.data.items.length}\n`);
    
    // Log ALL channels found
    console.log('All channels found:');
    response.data.items.forEach((item, i) => {
      const channelId = item.snippet.channelId;
      const isTrusted = TRUSTED_CHANNELS.includes(channelId);
      console.log(`${i + 1}. ${isTrusted ? '✅' : '❌'} ${item.snippet.channelTitle}`);
      console.log(`   Channel ID: ${channelId}`);
      console.log(`   Video: "${item.snippet.title}"\n`);
    });
    
    // Filter by trusted channels
    const videos = response.data.items.filter(item => {
      return TRUSTED_CHANNELS.includes(item.snippet.channelId);
    });
    
    console.log(`\n✅ Found ${videos.length} from trusted channels\n`);
    
    return videos;
    
  } catch (error) {
    console.error('❌ YouTube API Error:', error.message);
    if (error.response) {
      console.error('Error details:', error.response.data);
    }
    return [];
  }
}

// Test with one phone
async function test() {
  console.log('🧪 Testing YouTube Search\n');
  
  // Test with a popular phone
  const videos = await searchPhoneReviews('Apple', 'iPhone 16');
  
  if (videos.length > 0) {
    console.log('✅ SUCCESS! Found videos from trusted channels');
    videos.forEach(v => {
      console.log(`  - ${v.snippet.channelTitle}: ${v.snippet.title}`);
    });
  } else {
    console.log('❌ No videos from trusted channels found');
    console.log('\n💡 Possible reasons:');
    console.log('1. Trusted channels haven\'t reviewed this phone yet');
    console.log('2. YouTube API quota exceeded');
    console.log('3. Search query not matching videos');
  }
  
  process.exit(0);
}

test();