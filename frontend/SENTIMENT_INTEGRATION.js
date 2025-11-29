// Frontend Integration for YouTube Sentiment Analysis
// Add this to: frontend/src/pages/Recommendations.jsx

// 1. Add this function inside the Recommendations component (after useState declarations)

const analyzeSentiment = async (phones) => {
  if (!phones || phones.length === 0) return;
  
  const phoneIds = phones.map(p => p.id);
  
  try {
    console.log('Analyzing sentiment for phones:', phoneIds);
    
    const response = await fetch(`${API_BASE_URL}/analysis/phones`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(phoneIds)
    });
    
    if (!response.ok) {
      console.warn('Sentiment analysis failed, continuing without sentiment scores');
      return;
    }
    
    const results = await response.json();
    console.log('Sentiment analysis results:', results);
    
    // Update recommendations with sentiment scores
    setRecommendations(prevPhones => 
      prevPhones.map(phone => ({
        ...phone,
        sentimentScore: results[phone.id]?.averageSentimentScore || null,
        sentimentDetails: results[phone.id]?.channelScores || null
      }))
    );
  } catch (error) {
    console.error('Sentiment analysis error:', error);
    // Continue without sentiment scores
  }
};

// 2. Modify the useEffect to call analyzeSentiment after getting recommendations

useEffect(() => {
  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`${API_BASE_URL}/recommend`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          productType: productType || 'smartphone',
          budget: budget || '20-25',
          priorities: priorities || {
            camera: 50,
            battery: 50,
            performance: 50,
            privacy: 50,
            looks: 50
          }
        })
      });

      if (!response.ok) {
        throw new Error('Failed to fetch recommendations');
      }

      const data = await response.json();
      setRecommendations(data);
      
      // NEW: Analyze sentiment for recommended phones
      await analyzeSentiment(data);
      
    } catch (err) {
      console.error('Error fetching recommendations:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  fetchRecommendations();
}, [productType, budget, priorities]);

// 3. Add sentiment score display in the phone card (find the section that displays scores)
// Add this inside the phone card mapping where scores are displayed:

{phone.sentimentScore && (
  <div className="mt-4 pt-4 border-t border-slate-200">
    <div className="flex items-center justify-between mb-2">
      <span className="text-xs font-medium text-slate-600">YouTube Reviews</span>
      <span className={`text-sm font-semibold ${
        phone.sentimentScore >= 75 ? 'text-green-600' : 
        phone.sentimentScore >= 60 ? 'text-blue-600' : 
        'text-amber-600'
      }`}>
        {phone.sentimentScore}/100
      </span>
    </div>
    <div className="w-full bg-slate-100 rounded-full h-1.5">
      <div 
        className={`h-1.5 rounded-full ${
          phone.sentimentScore >= 75 ? 'bg-green-500' : 
          phone.sentimentScore >= 60 ? 'bg-blue-500' : 
          'bg-amber-500'
        }`}
        style={{ width: `${phone.sentimentScore}%` }}
      />
    </div>
    {phone.sentimentDetails && (
      <div className="mt-2 text-xs text-slate-500">
        Analyzed from {Object.keys(phone.sentimentDetails).length} trusted reviewers
      </div>
    )}
  </div>
)}

// COMPLETE UPDATED useEffect CODE:

useEffect(() => {
  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch(`${API_BASE_URL}/recommend`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          productType: productType || 'smartphone',
          budget: budget || '20-25',
          priorities: priorities || {
            camera: 50,
            battery: 50,
            performance: 50,
            privacy: 50,
            looks: 50
          }
        })
      });

      if (!response.ok) {
        throw new Error('Failed to fetch recommendations');
      }

      const data = await response.json();
      setRecommendations(data);
      
      // Analyze sentiment for recommended phones
      if (data && data.length > 0) {
        const phoneIds = data.map(p => p.id);
        
        try {
          const sentimentResponse = await fetch(`${API_BASE_URL}/analysis/phones`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(phoneIds)
          });
          
          if (sentimentResponse.ok) {
            const results = await sentimentResponse.json();
            
            setRecommendations(prevPhones => 
              prevPhones.map(phone => ({
                ...phone,
                sentimentScore: results[phone.id]?.averageSentimentScore || null,
                sentimentDetails: results[phone.id]?.channelScores || null
              }))
            );
          }
        } catch (sentimentError) {
          console.warn('Sentiment analysis unavailable:', sentimentError);
        }
      }
      
    } catch (err) {
      console.error('Error fetching recommendations:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  fetchRecommendations();
}, [productType, budget, priorities]);
