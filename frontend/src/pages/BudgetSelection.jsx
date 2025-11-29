import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { IoChevronBack, IoChevronForward } from 'react-icons/io5';
import { HiSparkles, HiBanknotes } from 'react-icons/hi2';
import { TbPigMoney, TbDiamond, TbCrown } from 'react-icons/tb';
import { RiVipCrownFill } from 'react-icons/ri';

function BudgetSelection() {
  const [selectedBudget, setSelectedBudget] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const productType = location.state?.productType || 'smartphone';

  const budgetRanges = [
    { id: 'under-10', label: 'Entry Level', range: 'Under ₹10K', icon: TbPigMoney, color: 'from-emerald-400 to-teal-500' },
    { id: '10-15', label: 'Budget', range: '₹10K - ₹15K', icon: HiBanknotes, color: 'from-green-400 to-emerald-500' },
    { id: '15-20', label: 'Budget Plus', range: '₹15K - ₹20K', icon: HiBanknotes, color: 'from-cyan-400 to-blue-500' },
    { id: '20-25', label: 'Mid Range', range: '₹20K - ₹25K', icon: HiSparkles, color: 'from-blue-400 to-indigo-500' },
    { id: '25-30', label: 'Mid Premium', range: '₹25K - ₹30K', icon: HiSparkles, color: 'from-indigo-400 to-purple-500' },
    { id: '30-35', label: 'Premium', range: '₹30K - ₹35K', icon: TbDiamond, color: 'from-purple-400 to-pink-500' },
    { id: '35-40', label: 'Premium Plus', range: '₹35K - ₹40K', icon: TbDiamond, color: 'from-pink-400 to-rose-500' },
    { id: '40-50', label: 'High End', range: '₹40K - ₹50K', icon: TbCrown, color: 'from-rose-400 to-red-500' },
    { id: '50-60', label: 'Ultra Premium', range: '₹50K - ₹60K', icon: TbCrown, color: 'from-orange-400 to-amber-500' },
    { id: '60-75', label: 'Luxury', range: '₹60K - ₹75K', icon: RiVipCrownFill, color: 'from-amber-400 to-yellow-500' },
    { id: '75-plus', label: 'Flagship', range: '₹75K & Above', icon: RiVipCrownFill, color: 'from-yellow-400 to-orange-500' },
    { id: 'New Gen', label: 'Foldables', range: '₹95K & Above', icon: RiVipCrownFill, color: 'from-violet-400 to-purple-600' },
  ];

  const handleNext = () => {
    if (selectedBudget) {
      navigate('/priorities', {
        state: {
          productType,
          budget: selectedBudget
        }
      });
    }
  };

  const handleBack = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-900 dark:to-slate-800 py-12 px-6 transition-colors duration-300">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-5xl font-light text-slate-800 dark:text-white mb-3 tracking-tight">
            Your Budget
          </h1>
          <p className="text-slate-500 dark:text-slate-400 text-lg font-light">
            Select your preferred price range
          </p>
        </div>

        {/* Progress dots */}
        <div className="flex justify-center mb-12 gap-2">
          <div className="w-2 h-2 rounded-full bg-indigo-600"></div>
          <div className="w-2 h-2 rounded-full bg-indigo-600"></div>
          <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700"></div>
          <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700"></div>
        </div>

        {/* Budget grid - clean cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          {budgetRanges.map((budget) => {
            const IconComponent = budget.icon;
            return (
              <button
                key={budget.id}
                onClick={() => setSelectedBudget(budget.id)}
                className={`group p-6 rounded-2xl border-2 transition-all duration-300 text-left ${selectedBudget === budget.id
                    ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-900/20 scale-105 shadow-lg shadow-indigo-100 dark:shadow-none'
                    : 'border-slate-200 dark:border-slate-700 hover:border-indigo-300 dark:hover:border-indigo-500 hover:bg-slate-50 dark:hover:bg-slate-800 hover:scale-102'
                  }`}
              >
                <div className="flex items-start justify-between mb-3">
                  <div className={`w-12 h-12 rounded-xl flex items-center justify-center bg-gradient-to-br ${budget.color} ${selectedBudget === budget.id ? 'shadow-lg' : 'group-hover:shadow-md'
                    } transition-shadow`}>
                    <IconComponent className="w-6 h-6 text-white" />
                  </div>
                  {selectedBudget === budget.id && (
                    <div className="w-6 h-6 rounded-full bg-indigo-500 flex items-center justify-center">
                      <svg className="w-3.5 h-3.5 text-white" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                    </div>
                  )}
                </div>
                <h3 className="text-lg font-medium text-slate-800 dark:text-white mb-1">
                  {budget.label}
                </h3>
                <p className="text-slate-500 dark:text-slate-400 font-light text-sm">
                  {budget.range}
                </p>
              </button>
            );
          })}
        </div>

        {/* Navigation buttons */}
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
            disabled={!selectedBudget}
            className={`flex-1 flex items-center justify-center gap-2 py-5 rounded-2xl font-medium text-lg transition-all duration-300 ${selectedBudget
                ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white hover:shadow-xl hover:shadow-indigo-200 dark:hover:shadow-none hover:scale-[1.02]'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-600 cursor-not-allowed'
              }`}
          >
            Continue
            {selectedBudget && <IoChevronForward className="w-5 h-5" />}
          </button>
        </div>
      </div>
    </div>
  );
}

export default BudgetSelection;
