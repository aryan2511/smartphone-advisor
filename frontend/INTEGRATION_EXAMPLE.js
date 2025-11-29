// Example: How to trigger phone analysis from React frontend

// When user selects phones and clicks "Get Recommendations"
const analyzeAndRecommend = async (phoneIds, budget, priorities) => {
  
  // Step 1: Trigger analysis for selected phones
  // This runs YouTube video search and sentiment analysis
  try {
    const analysisResponse = await fetch('http://localhost:8080/api/analysis/phones', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(phoneIds) // e.g., [1, 2, 3, 4, 5]
    });
    
    const analysisResults = await analysisResponse.json();
    console.log('Analysis complete:', analysisResults);
    
    // analysisResults will look like:
    // {
    //   "1": {
    //     "phoneId": 1,
    //     "phoneModel": "Samsung S24",
    //     "averageSentimentScore": 85,
    //     "channelScores": { "Mrwhosetheboss": 90, "TechGuruji": 80 },
    //     "message": "Analyzed 2 video(s)"
    //   },
    //   "2": { ... }
    // }
    
  } catch (error) {
    console.error('Analysis failed:', error);
    // Continue anyway - recommendations will work without sentiment scores
  }
  
  // Step 2: Get recommendations (now with sentiment-boosted scores)
  const priorityString = Object.entries(priorities)
    .map(([key, value]) => `${key}:${value}`)
    .join(',');
  
  const recResponse = await fetch(
    `http://localhost:8080/api/recommendations?budget=${budget}&priorities=${priorityString}`
  );
  
  const recommendations = await recResponse.json();
  return recommendations;
};

// Example usage in React component:
const handleGetRecommendations = async () => {
  const selectedPhoneIds = [1, 2, 3, 4, 5]; // from your phone selection state
  const budget = 60000; // from budget selection state
  const priorities = {
    camera: 80,
    battery: 70,
    performance: 60,
    privacy: 40,
    design: 50
  }; // from priority sliders state
  
  setLoading(true);
  
  const recommendations = await analyzeAndRecommend(
    selectedPhoneIds,
    budget,
    priorities
  );
  
  setRecommendations(recommendations);
  setLoading(false);
};


// Alternative: Analyze phones individually (slower but more granular)
const analyzeSinglePhone = async (phoneId) => {
  const response = await fetch(`http://localhost:8080/api/analysis/phone/${phoneId}`, {
    method: 'POST'
  });
  
  const result = await response.json();
  console.log(`Phone ${phoneId} analysis:`, result);
  return result;
};
