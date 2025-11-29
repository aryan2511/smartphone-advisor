import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { HiDevicePhoneMobile, HiSparkles } from 'react-icons/hi2';
import { IoChevronForward } from 'react-icons/io5';
import { GrCatalog } from "react-icons/gr";

function ProductSelection() {
  const [selectedProduct, setSelectedProduct] = useState('');
  const navigate = useNavigate();

  const handleNext = () => {
    if (selectedProduct) {
      navigate('/budget', { state: { productType: selectedProduct } });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 dark:from-slate-900 dark:via-slate-900 dark:to-slate-800 flex items-center justify-center p-6 transition-colors duration-300">
      <div className="w-full max-w-2xl">
        {/* Minimal Logo/Brand */}
        <div className="text-center mb-12">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-purple-600 mb-4 shadow-lg shadow-indigo-200">
            <GrCatalog className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-5xl font-light text-slate-800 dark:text-white mb-3 tracking-tight">
            Find My Gadget
          </h1>
          <p className="text-slate-500 dark:text-slate-400 text-lg font-light">
            Find your perfect device in minutes
          </p>
        </div>

        {/* Progress dots - minimal */}
        <div className="flex justify-center mb-12 gap-2">
          <div className="w-2 h-2 rounded-full bg-indigo-600"></div>
          <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700"></div>
          <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700"></div>
          <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700"></div>
        </div>

        {/* Product selection card - clean and spacious */}
        <div className="bg-white dark:bg-slate-800 rounded-3xl shadow-sm border border-slate-100 dark:border-slate-700 p-8 mb-6 transition-all hover:shadow-md">
          <button
            onClick={() => setSelectedProduct('smartphone')}
            className={`w-full p-8 rounded-2xl border-2 transition-all duration-300 ${
              selectedProduct === 'smartphone'
                ? 'border-indigo-500 bg-indigo-50/50 dark:bg-indigo-900/20 scale-[1.02]'
                : 'border-slate-200 dark:border-slate-700 hover:border-indigo-300 dark:hover:border-indigo-500 hover:bg-slate-50 dark:hover:bg-slate-700/50'
            }`}
          >
            <div className="flex items-center gap-6">
              <div className={`w-16 h-16 rounded-2xl flex items-center justify-center transition-all ${
                selectedProduct === 'smartphone'
                  ? 'bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg shadow-indigo-200 dark:shadow-none'
                  : 'bg-slate-100 dark:bg-slate-700'
              }`}>
                <HiDevicePhoneMobile className={`w-8 h-8 ${
                  selectedProduct === 'smartphone' ? 'text-white' : 'text-slate-600 dark:text-slate-300'
                }`} />
              </div>
              <div className="flex-1 text-left">
                <h3 className="text-2xl font-medium text-slate-800 dark:text-white mb-1">Smartphone</h3>
                <p className="text-slate-500 dark:text-slate-400 font-light">
                  Personalized recommendations based on your needs
                </p>
              </div>
              {selectedProduct === 'smartphone' && (
                <div className="w-6 h-6 rounded-full bg-indigo-500 flex items-center justify-center flex-shrink-0">
                  <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                </div>
              )}
            </div>
          </button>
        </div>

        {/* Next button - minimal and clean */}
        <button
          onClick={handleNext}
          disabled={!selectedProduct}
          className={`w-full py-5 rounded-2xl font-medium text-lg transition-all duration-300 flex items-center justify-center gap-2 ${
            selectedProduct
              ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white hover:shadow-xl hover:shadow-indigo-200 dark:hover:shadow-none hover:scale-[1.02]'
              : 'bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-600 cursor-not-allowed'
          }`}
        >
          Continue
          {selectedProduct && <IoChevronForward className="w-5 h-5" />}
        </button>
      </div>
    </div>
  );
}

export default ProductSelection;
