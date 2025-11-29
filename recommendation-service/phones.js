const db = require('../recommendation-service/db');
const { formatPrice } = require('../recommendation-service/insights');

/**
 * GET /api/phones/:id
 */
async function getPhoneById(req, res) {
  try {
    const { id } = req.params;
    
    const result = await db.query(`
      SELECT 
        p.*,
        COALESCE(AVG(yr.sentiment_score), 0) as avg_review_score,
        COUNT(yr.id) as review_count
      FROM phones p
      LEFT JOIN youtube_reviews yr ON p.id = yr.phone_id
      WHERE p.id = $1
      GROUP BY p.id
    `, [id]);
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Phone not found' });
    }
    
    const phone = result.rows[0];
    
    res.json({
      id: phone.id,
      brand: phone.brand,
      model: phone.model,
      price: formatPrice(phone.price),
      priceRaw: phone.price,
      image_url: phone.image_url,
      display: phone.display,
      processor: phone.processor,
      ram: phone.ram,
      storage: phone.storage,
      battery: phone.battery,
      camera: phone.camera,
      camera_score: phone.camera_score,
      battery_score: phone.battery_score,
      performance_score: phone.software_score,
      privacy_score: phone.privacy_score,
      design_score: phone.looks_score,
      review_count: phone.review_count,
      avg_review_score: Math.round(phone.avg_review_score)
    });
    
  } catch (error) {
    console.error('Error getting phone:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
}

/**
 * GET /api/phones/:id/reviews
 */
async function getPhoneReviews(req, res) {
  try {
    const { id } = req.params;
    
    const result = await db.query(`
      SELECT 
        channel_name,
        video_title,
        video_url,
        thumbnail_url,
        view_count,
        published_at,
        sentiment_score,
        positive_points,
        negative_points,
        key_insights,
        recommendation
      FROM youtube_reviews
      WHERE phone_id = $1
      ORDER BY sentiment_score DESC, view_count DESC
    `, [id]);
    
    const reviews = result.rows.map(r => ({
      channel: r.channel_name,
      title: r.video_title,
      url: r.video_url,
      thumbnail: r.thumbnail_url,
      views: r.view_count,
      published: r.published_at,
      sentiment: r.sentiment_score,
      recommendation: r.recommendation,
      positive: r.positive_points || [],
      negative: r.negative_points || [],
      insights: r.key_insights
    }));
    
    res.json({ reviews });
    
  } catch (error) {
    console.error('Error getting reviews:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
}

module.exports = {
  getPhoneById,
  getPhoneReviews
};
