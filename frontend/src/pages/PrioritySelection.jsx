import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { IoChevronBack, IoChevronForward } from 'react-icons/io5';
import { HiCamera, HiBolt, HiShieldCheck, HiSparkles } from 'react-icons/hi2';
import { IoBatteryChargingOutline } from 'react-icons/io5';
import { MdDragIndicator } from 'react-icons/md';

function PrioritySelection() {
  const navigate = useNavigate();
  const location = useLocation();
  const productType = location.state?.productType || 'smartphone';
  const budget = location.state?.budget || '';

  const [features, setFeatures] = useState([
    {
      id: 'camera',
      label: 'Camera',
      icon: HiCamera,
      description: 'Photo & video quality',
      color: 'indigo',
      rank: 1
    },
    {
      id: 'battery',
      label: 'Battery',
      icon: IoBatteryChargingOutline,
      description: 'All-day power',
      color: 'emerald',
      rank: 2
    },
    {
      id: 'performance',
      label: 'Performance',
      icon: HiBolt,
      description: 'Speed & responsiveness',
      color: 'blue',
      rank: 3
    },
    {
      id: 'privacy',
      label: 'Privacy',
      icon: HiShieldCheck,
      description: 'Data protection',
      color: 'purple',
      rank: 4
    },
    {
      id: 'looks',
      label: 'Design',
      icon: HiSparkles,
      description: 'Aesthetics & build',
      color: 'pink',
      rank: 5
    }
  ]);

  const [draggedItem, setDraggedItem] = useState(null);
  const [dragOverIndex, setDragOverIndex] = useState(null);

  const handleDragStart = (e, index) => {
    setDraggedItem(index);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e, index) => {
    e.preventDefault();
    setDragOverIndex(index);
  };

  const handleDragLeave = () => {
    setDragOverIndex(null);
  };

  const handleDrop = (e, dropIndex) => {
    e.preventDefault();

    if (draggedItem === null || draggedItem === dropIndex) {
      setDraggedItem(null);
      setDragOverIndex(null);
      return;
    }

    const newFeatures = [...features];
    const draggedFeature = newFeatures[draggedItem];

    newFeatures.splice(draggedItem, 1);
    newFeatures.splice(dropIndex, 0, draggedFeature);

    newFeatures.forEach((feature, index) => {
      feature.rank = index + 1;
    });

    setFeatures(newFeatures);
    setDraggedItem(null);
    setDragOverIndex(null);
  };

  const handleDragEnd = () => {
    setDraggedItem(null);
    setDragOverIndex(null);
  };

  const convertRankingToPriorities = () => {
    const scoreMap = {
      1: 100,
      2: 85,
      3: 70,
      4: 55,
      5: 40
    };

    const priorities = {};
    features.forEach(feature => {
      priorities[feature.id] = scoreMap[feature.rank];
    });

    return priorities;
  };

  const handleNext = () => {
    const priorities = convertRankingToPriorities();
    navigate('/recommendations', {
      state: {
        productType,
        budget,
        priorities
      }
    });
  };

  const handleBack = () => {
    navigate('/budget', { state: { productType } });
  };

  const applyPreset = (preset) => {
    const presets = {
      photography: ['camera', 'looks', 'performance', 'battery', 'privacy'],
      battery: ['battery', 'performance', 'camera', 'looks', 'privacy'],
      performance: ['performance', 'camera', 'battery', 'looks', 'privacy'],
      privacy: ['privacy', 'performance', 'camera', 'battery', 'looks'],
      balanced: ['camera', 'battery', 'performance', 'privacy', 'looks']
    };

    const order = presets[preset];
    const newFeatures = order.map((id, index) => {
      const feature = features.find(f => f.id === id);
      return { ...feature, rank: index + 1 };
    });

    setFeatures(newFeatures);
  };

  const getRankBadgeColor = (rank) => {
    const colors = {
      1: 'from-yellow-400 to-amber-500',
      2: 'from-slate-300 to-slate-400',
      3: 'from-orange-400 to-rose-400',
      4: 'from-blue-300 to-indigo-400',
      5: 'from-slate-200 to-slate-300'
    };
    return colors[rank] || 'from-slate-200 to-slate-300';
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-900 dark:to-slate-800 py-12 px-6 transition-colors duration-300">
      <div className="max-w-3xl mx-auto">
        <div className="text-center mb-10">
          <h1 className="text-5xl font-light text-slate-800 dark:text-white mb-3 tracking-tight">
            Rank Your Priorities
          </h1>
          <p className="text-slate-500 dark:text-slate-400 text-lg font-light mb-2">
            Drag to reorder - what matters most to you?
          </p>
          <p className="text-sm text-slate-400 dark:text-slate-500 font-light">
            📱 #1 = Most Important · #5 = Least Important
          </p>
        </div>

        <div className="flex justify-center mb-12 gap-2">
          <div className="w-2 h-2 rounded-full bg-indigo-600"></div>
          <div className="w-2 h-2 rounded-full bg-indigo-600"></div>
          <div className="w-2 h-2 rounded-full bg-indigo-600"></div>
          <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700"></div>
        </div>

        <div className="space-y-3 mb-8">
          {features.map((feature, index) => {
            const IconComponent = feature.icon;
            const isDragging = draggedItem === index;
            const isOver = dragOverIndex === index;

            return (
              <div
                key={feature.id}
                draggable
                onDragStart={(e) => handleDragStart(e, index)}
                onDragOver={(e) => handleDragOver(e, index)}
                onDragLeave={handleDragLeave}
                onDrop={(e) => handleDrop(e, index)}
                onDragEnd={handleDragEnd}
                className={`bg-white dark:bg-slate-800 rounded-2xl border-2 p-6 cursor-move transition-all duration-200 ${isDragging
                    ? 'opacity-50 scale-95 border-indigo-300 dark:border-indigo-700'
                    : isOver
                      ? 'border-indigo-400 dark:border-indigo-500 shadow-lg scale-102'
                      : 'border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 hover:shadow-md'
                  }`}
              >
                <div className="flex items-center gap-4">
                  <div className="text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300 transition-colors flex-shrink-0">
                    <MdDragIndicator className="w-6 h-6" />
                  </div>

                  <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${getRankBadgeColor(feature.rank)} flex items-center justify-center shadow-md flex-shrink-0`}>
                    <span className="text-white font-semibold text-lg">#{feature.rank}</span>
                  </div>

                  <div className={`w-14 h-14 rounded-xl bg-${feature.color}-100 flex items-center justify-center flex-shrink-0`}>
                    <IconComponent className={`w-7 h-7 text-${feature.color}-600`} />
                  </div>

                  <div className="flex-grow">
                    <h3 className="text-xl font-medium text-slate-800 dark:text-white">
                      {feature.label}
                    </h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 font-light">
                      {feature.description}
                    </p>
                  </div>

                  <div className="text-right flex-shrink-0">
                    <div className={`text-2xl font-light ${feature.rank === 1 ? 'text-yellow-500' :
                        feature.rank === 2 ? 'text-slate-500' :
                          feature.rank === 3 ? 'text-orange-500' :
                            'text-slate-400 dark:text-slate-600'
                      }`}>
                      {feature.rank === 1 ? '🏆' : feature.rank === 2 ? '🥈' : feature.rank === 3 ? '🥉' : ''}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        <div className="mb-8">
          <p className="text-sm text-slate-500 dark:text-slate-400 font-light mb-4 text-center">Quick presets</p>
          <div className="flex flex-wrap gap-2 justify-center">
            <button
              onClick={() => applyPreset('photography')}
              className="px-4 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-full hover:border-indigo-300 dark:hover:border-indigo-500 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 transition-all font-light"
            >
              Photography First
            </button>
            <button
              onClick={() => applyPreset('battery')}
              className="px-4 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-full hover:border-emerald-300 dark:hover:border-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-900/30 transition-all font-light"
            >
              Battery Life
            </button>
            <button
              onClick={() => applyPreset('performance')}
              className="px-4 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-full hover:border-blue-300 dark:hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/30 transition-all font-light"
            >
              Performance
            </button>
            <button
              onClick={() => applyPreset('privacy')}
              className="px-4 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-full hover:border-purple-300 dark:hover:border-purple-500 hover:bg-purple-50 dark:hover:bg-purple-900/30 transition-all font-light"
            >
              Privacy First
            </button>
            <button
              onClick={() => applyPreset('balanced')}
              className="px-4 py-2 text-sm bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-full hover:border-slate-300 dark:hover:border-slate-500 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-all font-light"
            >
              Balanced
            </button>
          </div>
        </div>

        <div className="bg-indigo-50 dark:bg-indigo-900/20 border border-indigo-200 dark:border-indigo-800 rounded-2xl p-4 mb-8">
          <p className="text-sm text-indigo-700 dark:text-indigo-300 text-center font-light">
            💡 Tip: Your #1 choice gets the highest weight in our recommendation algorithm
          </p>
        </div>

        <div className="flex gap-4">
          <button
            onClick={handleBack}
            className="flex items-center justify-center gap-2 px-8 py-5 rounded-2xl font-medium text-lg border-2 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all duration-300 hover:scale-[1.02]"
          >
            <IoChevronBack className="w-5 h-5" />
            Back
          </button>
          <button
            onClick={handleNext}
            className="flex-1 flex items-center justify-center gap-2 py-5 rounded-2xl font-medium text-lg bg-gradient-to-r from-indigo-600 to-purple-600 text-white hover:shadow-xl hover:shadow-indigo-200 transition-all duration-300 hover:scale-[1.02]"
          >
            Get Results
            <IoChevronForward className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
}

export default PrioritySelection;
