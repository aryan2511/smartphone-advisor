/**
 * Generate "Why we picked this" explanation
 */
function generateWhyPicked(phone, priority, reviews) {
  const reasons = [];
  
  // Top priority strength
  const scoreMap = {
    camera: phone.camera_score,
    battery: phone.battery_score,
    performance: phone.software_score,
    privacy: phone.privacy_score,
    design: phone.looks_score
  };
  
  const topScore = scoreMap[priority];
  
  if (topScore >= 90) {
    reasons.push(`Exceptional ${priority} - one of the best in this range`);
  } else if (topScore >= 85) {
    reasons.push(`Excellent ${priority} performance`);
  } else if (topScore >= 80) {
    reasons.push(`Strong ${priority} capabilities`);
  }
  
  // Add review insights
  if (reviews && reviews.length > 0) {
    const avgSentiment = reviews.reduce((sum, r) => sum + r.sentiment_score, 0) / reviews.length;
    if (avgSentiment >= 50) {
      reasons.push('Highly praised by reviewers');
    } else if (avgSentiment >= 20) {
      reasons.push('Positively reviewed overall');
    }
  }
  
  // Value proposition
  if (phone.price < 30000) {
    reasons.push('Great value for money');
  } else if (phone.price >= 60000) {
    reasons.push('Premium flagship experience');
  }
  
  return reasons.slice(0, 3).join('. ') + '.';
}

/**
 * Generate "What to know" warnings
 */
function generateWhatToKnow(phone, reviews) {
  const warnings = [];
  
  // Check weak areas
  const scores = {
    camera: phone.camera_score,
    battery: phone.battery_score,
    performance: phone.software_score,
    privacy: phone.privacy_score,
    design: phone.looks_score
  };
  
  Object.entries(scores).forEach(([feature, score]) => {
    if (score < 75) {
      warnings.push(`${feature.charAt(0).toUpperCase() + feature.slice(1)} could be better`);
    }
  });
  
  // Add negative review points
  if (reviews && reviews.length > 0) {
    reviews.forEach(review => {
      if (review.negative_points && review.negative_points.length > 0) {
        warnings.push(...review.negative_points.slice(0, 2));
      }
    });
  }
  
  return warnings.length > 0 ? warnings.slice(0, 3).join('. ') + '.' : 'No major concerns';
}

/**
 * Format price in Indian Rupees
 */
function formatPrice(price) {
  return `₹${price.toLocaleString('en-IN')}`;
}

module.exports = {
  generateWhyPicked,
  generateWhatToKnow,
  formatPrice
};
