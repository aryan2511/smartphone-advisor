import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { HiCamera, HiBolt, HiShieldCheck, HiSparkles, HiArrowPath } from 'react-icons/hi2';
import { IoBatteryChargingOutline } from 'react-icons/io5';
import { TbExternalLink } from 'react-icons/tb';
import { SiFlipkart } from "react-icons/si";
import { FaAmazon } from 'react-icons/fa';
import { getRecommendations } from '../services/api';
import ComparisonModal from '../components/ComparisonModal';

function Recommendations() {
  const navigate = useNavigate();
  const location = useLocation();
  const { productType, budget, priorities } = location.state || {};

  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Comparison State
  const [selectedForComparison, setSelectedForComparison] = useState([]);
  const [isComparisonOpen, setIsComparisonOpen] = useState(false);

  useEffect(() => {
    const fetchRecommendations = async () => {
      if (!productType || !budget || !priorities) {
        setError('Missing required information. Please start over.');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        // Transform priorities from rank (1-5) to percentage
        const priorityMap = {
          camera: priorities.camera || 3,
          battery: priorities.battery || 3,
          software: priorities.software || 3,
          privacy: priorities.privacy || 3,
          looks: priorities.looks || 3
        };

        const data = await getRecommendations({
          productType,
          budget,
          priorities: priorityMap
        });

        setRecommendations(data);
      } catch (err) {
        console.error('Error fetching recommendations:', err);
        setError('Unable to fetch recommendations. Please make sure the backend is running.');
      } finally {
        setLoading(false);
      }
    };

    fetchRecommendations();
  }, [productType, budget, priorities]);

  const handleStartOver = () => {
    navigate('/');
  };

  const toggleComparison = (phone) => {
    if (selectedForComparison.find(p => p.id === phone.id)) {
      setSelectedForComparison(prev => prev.filter(p => p.id !== phone.id));
    } else {
      if (selectedForComparison.length >= 3) {
        alert("You can compare up to 3 phones at a time.");
        return;
      }
      setSelectedForComparison(prev => [...prev, phone]);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-3xl bg-gradient-to-br from-indigo-500 to-purple-600 mb-6 animate-pulse">
            <HiArrowPath className="w-10 h-10 text-white animate-spin" />
          </div>
          <h2 className="text-3xl font-light text-slate-800 dark:text-white mb-2">Finding your perfect match</h2>
          <p className="text-slate-500 dark:text-slate-400 font-light">Analyzing your preferences...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 flex items-center justify-center p-6">
        <div className="bg-white dark:bg-slate-800 rounded-3xl border border-red-200 dark:border-red-900/50 p-12 max-w-md text-center">
          <div className="text-6xl mb-4">⚠️</div>
          <h2 className="text-2xl font-light text-slate-800 dark:text-white mb-2">Oops! Something went wrong</h2>
          <p className="text-slate-600 dark:text-slate-400 font-light mb-6">{error}</p>
          <button
            onClick={handleStartOver}
            className="px-8 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl hover:shadow-xl hover:shadow-indigo-200 dark:hover:shadow-none transition-all duration-300 font-medium"
          >
            Start Over
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-800 dark:to-slate-900 py-12 px-6 transition-colors duration-300">

      <div className="max-w-5xl mx-auto">
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-5xl font-light text-slate-800 dark:text-white mb-3 tracking-tight">
            Your Perfect Match
          </h1>
          <p className="text-slate-500 dark:text-slate-400 text-lg font-light">
            Personalized recommendations based on what matters to you
          </p>
        </div>

        {/* Comparison Floating Action Button */}
        {selectedForComparison.length > 0 && (
          <div className="fixed bottom-8 right-8 z-40 flex flex-col items-end gap-4 animate-bounce-in">
            <div className="bg-slate-900 dark:bg-white text-white dark:text-slate-900 px-6 py-4 rounded-2xl shadow-2xl flex items-center gap-4">
              <span className="font-medium">{selectedForComparison.length} selected</span>
              <button
                onClick={() => setIsComparisonOpen(true)}
                className="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-xl transition-colors font-medium"
              >
                Compare Now
              </button>
              <button
                onClick={() => setSelectedForComparison([])}
                className="text-slate-400 hover:text-white dark:hover:text-slate-600 transition-colors"
              >
                Clear
              </button>
            </div>
          </div>
        )}

        <ComparisonModal
          isOpen={isComparisonOpen}
          closeModal={() => setIsComparisonOpen(false)}
          phones={selectedForComparison}
        />

        {/* Recommendations */}
        {recommendations.length > 0 ? (
          <div className="space-y-8 mb-8">
            {recommendations.map((phone, index) => {
              const isSelected = selectedForComparison.find(p => p.id === phone.id);

              return (
                <div key={phone.id} className={`bg-white dark:bg-slate-800 rounded-3xl border-2 ${isSelected ? 'border-indigo-500 ring-4 ring-indigo-500/20' : 'border-slate-200 dark:border-slate-700'} overflow-hidden hover:border-indigo-300 dark:hover:border-indigo-700 hover:shadow-2xl transition-all duration-300`}>
                  {/* Phone Header */}
                  <div className="p-8 pb-6">
                    <div className="flex flex-col lg:flex-row gap-8">
                      {/* Rank badge */}
                      <div className="flex-shrink-0 flex lg:flex-col items-center lg:items-start gap-4">
                        <div className={`w-16 h-16 rounded-2xl flex items-center justify-center text-3xl font-light shadow-lg ${index === 0 ? 'bg-gradient-to-br from-yellow-400 to-amber-500 text-white' :
                          index === 1 ? 'bg-gradient-to-br from-slate-300 to-slate-400 text-white' :
                            index === 2 ? 'bg-gradient-to-br from-orange-400 to-rose-400 text-white' :
                              'bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300'
                          }`}>
                          {index === 0 ? '🏆' : `#${index + 1}`}
                        </div>
                        {/* Match score */}
                        <div className="text-center lg:text-left">
                          <p className="text-sm text-slate-500 dark:text-slate-400 font-light">Match Score</p>
                          <p className="text-2xl font-light text-indigo-600 dark:text-indigo-400">{phone.matchScore || 'N/A'}%</p>
                        </div>
                      </div>

                      {/* Phone image */}
                      <div className="flex-shrink-0 mx-auto lg:mx-0">
                        <div className="w-40 h-48 rounded-2xl overflow-hidden bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-700 dark:to-slate-600 shadow-lg flex items-center justify-center p-4">
                          <img
                            src={phone.image || 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400'}
                            alt={`${phone.brand} ${phone.model}`}
                            className="w-full h-full object-contain mix-blend-multiply dark:mix-blend-normal"
                            onError={(e) => {
                              e.target.src = 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400';
                            }}
                          />
                        </div>
                      </div>

                      {/* Phone details */}
                      <div className="flex-grow">
                        <div className="flex justify-between items-start mb-6">
                          <div>
                            <h2 className="text-4xl font-light text-slate-800 dark:text-white mb-2">
                              {phone.model}
                            </h2>
                            <p className="text-4xl font-light text-indigo-600 dark:text-indigo-400 mb-1">
                              ₹{phone.price?.toLocaleString('en-IN') || 'N/A'}
                            </p>
                            {index === 0 && (
                              <p className="text-sm text-emerald-600 dark:text-emerald-400 font-medium">✓ Our Top Pick for You</p>
                            )}
                          </div>

                          {/* Compare Checkbox */}
                          <button
                            onClick={() => toggleComparison(phone)}
                            className={`flex items-center gap-2 px-4 py-2 rounded-xl border transition-all ${isSelected
                              ? 'bg-indigo-50 dark:bg-indigo-900/30 border-indigo-200 dark:border-indigo-700 text-indigo-700 dark:text-indigo-300'
                              : 'border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-700'
                              }`}
                          >
                            <div className={`w-5 h-5 rounded border flex items-center justify-center ${isSelected ? 'bg-indigo-600 border-indigo-600' : 'border-slate-300 dark:border-slate-500'
                              }`}>
                              {isSelected && <span className="text-white text-xs">✓</span>}
                            </div>
                            <span className="text-sm font-medium">Compare</span>
                          </button>
                        </div>

                        {/* Specs grid */}
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-3 mb-6">
                          {phone.specs && Object.entries(phone.specs).map(([key, value]) => (
                            <div key={key} className="bg-slate-50 dark:bg-slate-700/50 rounded-xl p-3 border border-slate-100 dark:border-slate-700">
                              <p className="text-xs text-slate-500 dark:text-slate-400 font-light mb-1 capitalize">{key}</p>
                              <p className="text-sm text-slate-800 dark:text-slate-200 font-medium">{value}</p>
                            </div>
                          ))}
                        </div>

                        {/* Buy buttons */}
                        <div className="flex flex-wrap gap-3">
                          <a
                            href={phone.affiliateLinks?.amazon || '#'}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center gap-2 px-6 py-3 bg-slate-900 dark:bg-black text-white rounded-xl hover:bg-slate-800 dark:hover:bg-slate-900 transition-all font-medium shadow-md hover:shadow-lg"
                          >
                            <FaAmazon className="w-5 h-5" />
                            Buy on Amazon
                            <TbExternalLink className="w-4 h-4" />
                          </a>
                          <a
                            href={phone.affiliateLinks?.flipkart || '#'}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all font-medium shadow-md hover:shadow-lg"
                          >
                            <SiFlipkart className="w-5 h-5" />
                            Buy on Flipkart
                            <TbExternalLink className="w-4 h-4" />
                          </a>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Score breakdown */}
                  <div className="border-t-2 border-slate-100 dark:border-slate-700 bg-gradient-to-br from-slate-50 to-white dark:from-slate-800 dark:to-slate-800/50 p-8">
                    <h3 className="text-lg font-semibold text-slate-800 dark:text-white mb-4">Why this phone?</h3>
                    <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                      {phone.scores && Object.entries(phone.scores).map(([key, value]) => {
                        const icons = {
                          camera: { Icon: HiCamera, gradient: 'from-pink-400 to-rose-500' },
                          battery: { Icon: IoBatteryChargingOutline, gradient: 'from-green-400 to-emerald-500' },
                          software: { Icon: HiBolt, gradient: 'from-blue-400 to-indigo-500' },
                          privacy: { Icon: HiShieldCheck, gradient: 'from-purple-400 to-violet-500' },
                          looks: { Icon: HiSparkles, gradient: 'from-amber-400 to-orange-500' }
                        };
                        const { Icon, gradient } = icons[key] || { Icon: HiSparkles, gradient: 'from-slate-400 to-slate-500' };

                        return (
                          <div key={key} className="text-center">
                            <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${gradient} mx-auto mb-2 flex items-center justify-center`}>
                              <Icon className="w-6 h-6 text-white" />
                            </div>
                            <p className="text-xs text-slate-500 dark:text-slate-400 mb-1 capitalize">{key}</p>
                            <p className="text-lg font-semibold text-slate-800 dark:text-white">{value}</p>
                          </div>
                        );
                      })}
                    </div>

                    {/* Sentiment scores if available */}
                    {(phone.youtubeSentimentScore || phone.redditSentimentScore) && (
                      <div className="mt-6 pt-6 border-t border-slate-200 dark:border-slate-700">
                        <p className="text-sm text-slate-600 dark:text-slate-400 mb-3 font-medium">Community Reviews</p>
                        <div className="flex gap-4">
                          {phone.youtubeSentimentScore && (
                            <div className="flex items-center gap-2">
                              <span className="text-xs text-slate-500 dark:text-slate-400">YouTube</span>
                              <span className="px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 text-sm font-medium">
                                {phone.youtubeSentimentScore}/100
                              </span>
                            </div>
                          )}
                          {phone.redditSentimentScore && (
                            <div className="flex items-center gap-2">
                              <span className="text-xs text-slate-500 dark:text-slate-400">Reddit</span>
                              <span className="px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 text-sm font-medium">
                                {phone.redditSentimentScore}/100
                              </span>
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Insights Section */}
                  {(phone.whyPicked || phone.whyLoveIt || phone.whatToKnow || (phone.youtubeSentimentScore || phone.redditSentimentScore)) && (
                    <div className="border-t-2 border-slate-100 dark:border-slate-700 bg-gradient-to-br from-slate-50 to-white dark:from-slate-800 dark:to-slate-800/50 p-8">
                      <div className="space-y-6">
                        {/* Why We Picked This */}
                        {phone.whyPicked && (
                          <div className="bg-white dark:bg-slate-700/50 rounded-2xl p-6 border-2 border-emerald-100 dark:border-emerald-900/30 shadow-sm">
                            <h3 className="text-lg font-semibold text-emerald-700 dark:text-emerald-400 mb-3 flex items-center gap-2">
                              <span className="text-2xl">✅</span>
                              Why we picked this for you
                            </h3>
                            <p className="text-slate-700 dark:text-slate-300 leading-relaxed font-light">
                              {phone.whyPicked}
                            </p>
                          </div>
                        )}

                        {/* Why You'll Love It */}
                        {phone.whyLoveIt && (
                          <div className="bg-white dark:bg-slate-700/50 rounded-2xl p-6 border-2 border-indigo-100 dark:border-indigo-900/30 shadow-sm">
                            <h3 className="text-lg font-semibold text-indigo-700 dark:text-indigo-400 mb-3 flex items-center gap-2">
                              <span className="text-2xl">💚</span>
                              Why you'll love it
                            </h3>
                            <div className="text-slate-700 dark:text-slate-300 leading-relaxed font-light whitespace-pre-line">
                              {phone.whyLoveIt}
                            </div>
                          </div>
                        )}

                        {/* What to Know */}
                        {phone.whatToKnow && (
                          <div className="bg-white dark:bg-slate-700/50 rounded-2xl p-6 border-2 border-amber-100 dark:border-amber-900/30 shadow-sm">
                            <h3 className="text-lg font-semibold text-amber-700 dark:text-amber-400 mb-3 flex items-center gap-2">
                              <span className="text-2xl">💡</span>
                              What to know
                            </h3>
                            <p className="text-slate-700 dark:text-slate-300 leading-relaxed font-light">
                              {phone.whatToKnow}
                            </p>
                          </div>
                        )}

                        {/* Community Sentiment Analysis */}
                        {(phone.youtubeSentimentScore || phone.redditSentimentScore) && (
                          <div className="bg-white dark:bg-slate-700/50 rounded-2xl p-6 border-2 border-purple-100 dark:border-purple-900/30 shadow-sm">
                            <h3 className="text-lg font-semibold text-purple-700 dark:text-purple-400 mb-4 flex items-center gap-2">
                              <span className="text-2xl">📊</span>
                              What reviewers say
                            </h3>
                            <div className="space-y-4">
                              {phone.youtubeSentimentScore && (
                                <div className="flex items-start gap-4">
                                  <div className="flex-shrink-0">
                                    <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-red-400 to-rose-500 flex items-center justify-center text-white font-bold text-xl shadow-md">
                                      {phone.youtubeSentimentScore}
                                    </div>
                                  </div>
                                  <div className="flex-grow">
                                    <p className="font-semibold text-slate-800 dark:text-white mb-1">YouTube Tech Reviewers</p>
                                    <p className="text-sm text-slate-600 dark:text-slate-300 font-light">
                                      {phone.youtubeSentimentScore >= 80
                                        ? '⭐ Highly praised by tech YouTubers for its overall performance and features'
                                        : phone.youtubeSentimentScore >= 60
                                          ? '👍 Generally positive reviews with some noted trade-offs'
                                          : '⚠️ Mixed reviews - consider reading detailed reviews before purchasing'
                                      }
                                    </p>
                                  </div>
                                </div>
                              )}
                              {phone.redditSentimentScore && (
                                <div className="flex items-start gap-4">
                                  <div className="flex-shrink-0">
                                    <div className="w-16 h-16 rounded-xl bg-gradient-to-br from-orange-400 to-red-500 flex items-center justify-center text-white font-bold text-xl shadow-md">
                                      {phone.redditSentimentScore}
                                    </div>
                                  </div>
                                  <div className="flex-grow">
                                    <p className="font-semibold text-slate-800 dark:text-white mb-1">Reddit Community</p>
                                    <p className="text-sm text-slate-600 dark:text-slate-300 font-light">
                                      {phone.redditSentimentScore >= 80
                                        ? '🎉 Real users love this phone! High satisfaction in daily use'
                                        : phone.redditSentimentScore >= 60
                                          ? '✅ Solid choice with good real-world feedback from users'
                                          : '💬 Community has mixed opinions - check discussions for details'
                                      }
                                    </p>
                                  </div>
                                </div>
                              )}
                            </div>
                          </div>
                        )}

                        {/* Why It Beats Alternatives */}
                        {phone.beatsAlternatives && phone.beatsAlternatives.length > 0 && (
                          <div className="bg-white dark:bg-slate-700/50 rounded-2xl p-6 border-2 border-cyan-100 dark:border-cyan-900/30 shadow-sm">
                            <h3 className="text-lg font-semibold text-cyan-700 dark:text-cyan-400 mb-3 flex items-center gap-2">
                              <span className="text-2xl">💪</span>
                              Why it beats the alternatives
                            </h3>
                            <div className="space-y-3">
                              {phone.beatsAlternatives.map((alt, idx) => (
                                <div key={idx} className="flex items-start gap-3">
                                  <span className="text-cyan-600 dark:text-cyan-400 font-semibold mt-0.5">vs</span>
                                  <div>
                                    <p className="font-medium text-slate-800 dark:text-white">
                                      {alt.brand} {alt.model}
                                    </p>
                                    <p className="text-slate-600 dark:text-slate-300 font-light">
                                      {alt.reason}
                                    </p>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 p-12 text-center">
            <p className="text-xl text-slate-600 dark:text-slate-400 font-light">No phones found matching your criteria</p>
            <p className="text-slate-500 dark:text-slate-500 mt-2 font-light">Try adjusting your budget or priorities</p>
          </div>
        )}

        {/* Action buttons */}
        <div className="flex flex-wrap gap-4 justify-center">
          <button
            onClick={handleStartOver}
            className="px-8 py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl hover:shadow-xl hover:shadow-indigo-200 dark:hover:shadow-none transition-all duration-300 font-medium hover:scale-[1.02]"
          >
            Start New Search
          </button>
          <button
            onClick={() => navigate('/priorities', { state: { productType, budget } })}
            className="px-8 py-4 border-2 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-2xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-all duration-300 font-medium hover:scale-[1.02]"
          >
            Adjust Priorities
          </button>
        </div>

        {/* Disclaimer */}
        <div className="mt-12 text-center text-xs text-slate-400 dark:text-slate-600 max-w-2xl mx-auto font-light">
          <p>Prices and availability may vary. We may earn a commission from purchases made through these links.</p>
        </div>
      </div>
    </div>
  );
}

export default Recommendations;
