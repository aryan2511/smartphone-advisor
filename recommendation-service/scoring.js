/**
 * Calculate weighted score based on user priorities
 * Priorities: [camera, battery, performance, privacy, design]
 * Weights: 1st=40%, 2nd=30%, 3rd=20%, 4th=10%, 5th=0%
 */
function calculateScore(phone, priorities) {
  const weights = [0.40, 0.30, 0.20, 0.10, 0.0];
  
  const scoreMap = {
    camera: phone.camera_score,
    battery: phone.battery_score,
    performance: phone.software_score,
    privacy: phone.privacy_score,
    design: phone.looks_score
  };
  
  let totalScore = 0;
  priorities.forEach((priority, index) => {
    const score = scoreMap[priority] || 75;
    totalScore += score * weights[index];
  });
  
  return Math.round(totalScore);
}

/**
 * Calculate match percentage (how well phone fits priorities)
 */
function calculateMatch(phone, priorities) {
  const score = calculateScore(phone, priorities);
  return Math.round((score / 100) * 100);
}

/**
 * Get top priority score
 */
function getTopPriorityScore(phone, priority) {
  const scoreMap = {
    camera: phone.camera_score,
    battery: phone.battery_score,
    performance: phone.software_score,
    privacy: phone.privacy_score,
    design: phone.looks_score
  };
  return scoreMap[priority] || 75;
}

module.exports = {
  calculateScore,
  calculateMatch,
  getTopPriorityScore
};
