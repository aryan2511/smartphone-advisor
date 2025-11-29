const db = require('../recommendation-service/db');
const { calculateScore, calculateMatch, getTopPriorityScore } = require('../recommendation-service/scoring');
const { generateWhyPicked, generateWhatToKnow, formatPrice } = require('../recommendation-service/scoring');

/**
 * GET /api/recommendations?budget=60000&priorities=camera,battery,performance,privacy,design
 */
async function getRecommendations(req, res) {
  try {
    const { budget, priorities } = req.query;
    
    // Validate inputs
    if (!budget || !priorities) {
      return res.status(400).json({ 
        error: 'Missing required parameters: budget and priorities' 
      });
    }
    
    const budgetNum = parseInt(budget);
    const priorityList = priorities.split(',');
    
    if (priorityList.length !== 5) {
      return res.status(400).json({ 
        error: 'Priorities must include all 5 features' 
      });
    }
    
    // Calculate budget range (±10%)
    const minBudget = budgetNum * 0.9;
    const maxBudget = budgetNum * 1.1;
    
    // Get phones in budget
    const phonesResult = await db.query(`
      SELECT 
        p.*,
        COALESCE(AVG(yr.sentiment_score), 0) as avg_review_score,
        COUNT(yr.id) as review_count
      FROM phones p
      LEFT JOIN youtube_reviews yr ON p.id = yr.phone_id
      WHERE p.price >= $1 AND p.price <= $2
      GROUP BY p.id
    `, [minBudget, maxBudget]);
    
    if (phonesResult.rows.length === 0) {
      return res.json({ 
        message: 'No phones found in this budget',
        recommendations: []
      });
    }
    
    // Calculate scores for each phone
    const scoredPhones = phonesResult.rows.map(phone => ({
      ...phone,
      match_score: calculateScore(phone, priorityList),
      match_percentage: calculateMatch(phone, priorityList),
      top_priority_score: getTopPriorityScore(phone, priorityList[0])
    }));
    
    // Sort by match score
    scoredPhones.sort((a, b) => b.match_score - a.match_score);
    
    // Get top 5 recommendations
    const topPhones = scoredPhones.slice(0, 5);
    
    // Fetch reviews for top phones
    const recommendations = await Promise.all(
      topPhones.map(async (phone) => {
        const reviewsResult = await db.query(`
          SELECT 
            channel_name,
            video_title,
            video_url,
            sentiment_score,
            positive_points,
            negative_points,
            key_insights,
            recommendation
          FROM youtube_reviews
          WHERE phone_id = $1
          ORDER BY sentiment_score DESC
          LIMIT 3
        `, [phone.id]);
        
        const reviews = reviewsResult.rows;
        
        return {
          id: phone.id,
          brand: phone.brand,
          model: phone.model,
          price: formatPrice(phone.price),
          priceRaw: phone.price,
          image_url: phone.image_url,
          
          // Specs
          display: phone.display,
          processor: phone.processor,
          ram: phone.ram,
          storage: phone.storage,
          battery: phone.battery,
          camera: phone.camera,
          
          // Scores
          match_percentage: phone.match_percentage,
          camera_score: phone.camera_score,
          battery_score: phone.battery_score,
          performance_score: phone.software_score,
          privacy_score: phone.privacy_score,
          design_score: phone.looks_score,
          
          // Insights
          why_picked: generateWhyPicked(phone, priorityList[0], reviews),
          what_to_know: generateWhatToKnow(phone, reviews),
          
          // Reviews
          review_count: phone.review_count,
          avg_review_score: Math.round(phone.avg_review_score),
          reviews: reviews.map(r => ({
            channel: r.channel_name,
            title: r.video_title,
            url: r.video_url,
            sentiment: r.sentiment_score,
            recommendation: r.recommendation,
            positive: r.positive_points || [],
            negative: r.negative_points || [],
            insights: r.key_insights
          }))
        };
      })
    );
    
    res.json({
      budget: budgetNum,
      budget_range: `${formatPrice(minBudget)} - ${formatPrice(maxBudget)}`,
      priorities: priorityList,
      total_found: scoredPhones.length,
      recommendations
    });
    
  } catch (error) {
    console.error('Error getting recommendations:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
}

module.exports = {
  getRecommendations
};
