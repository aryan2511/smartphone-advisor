import { Fragment, useEffect } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { HiXMark } from 'react-icons/hi2';

export default function ComparisonModal({ isOpen, closeModal, phones }) {
  // Debug: Log the phones data structure
  useEffect(() => {
    if (phones && phones.length > 0) {
      console.log('Comparison Modal - Phone Data:', phones);
      console.log('First Phone Structure:', {
        id: phones[0].id,
        price: phones[0].price,
        specs: phones[0].specs,
        scores: phones[0].scores
      });
    }
  }, [phones]);

  if (!phones || phones.length === 0) return null;

  // Helper function to safely get nested values from the phone object
  const getValue = (phone, key) => {
    switch (key) {
      case 'price':
        return phone.price;
      case 'cameraScore':
        return phone.scores?.camera;
      case 'batteryScore':
        return phone.scores?.battery;
      case 'softwareScore':
        return phone.scores?.software;
      case 'displayInfo':
        return phone.specs?.display;
      case 'processor':
        return phone.specs?.processor;
      case 'battery':
        return phone.specs?.battery;
      default:
        return null;
    }
  };

  // Features to compare
  const features = [
    { label: 'Price', key: 'price', format: (val) => val ? `₹${val.toLocaleString('en-IN')}` : '-' },
    { label: 'Camera', key: 'cameraScore', format: (val) => val ? `${val}/100` : '-' },
    { label: 'Battery', key: 'batteryScore', format: (val) => val ? `${val}/100` : '-' },
    { label: 'Performance', key: 'softwareScore', format: (val) => val ? `${val}/100` : '-' },
    { label: 'Display', key: 'displayInfo' },
    { label: 'Processor', key: 'processor' },
    { label: 'Battery Specs', key: 'battery' },
  ];

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={closeModal}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/25 backdrop-blur-sm" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4 text-center">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-4xl transform overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 text-left align-middle shadow-xl transition-all">
                <div className="flex justify-between items-center mb-6">
                  <Dialog.Title
                    as="h3"
                    className="text-2xl font-light leading-6 text-slate-900 dark:text-white"
                  >
                    Compare Phones
                  </Dialog.Title>
                  <button
                    onClick={closeModal}
                    className="rounded-full p-1 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                  >
                    <HiXMark className="w-6 h-6 text-slate-500 dark:text-slate-400" />
                  </button>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr>
                        <th className="p-4 border-b dark:border-slate-700 bg-slate-50 dark:bg-slate-900 min-w-[150px] text-slate-900 dark:text-white font-medium">
                          Feature
                        </th>
                        {phones.map((phone) => (
                          <th key={phone.id} className="p-4 border-b dark:border-slate-700 min-w-[200px]">
                            <div className="flex flex-col items-center gap-2">
                              <img 
                                src={phone.image || 'https://via.placeholder.com/100'} 
                                alt={phone.model} 
                                className="w-16 h-20 object-contain"
                              />
                              <span className="font-medium text-slate-900 dark:text-white text-center">
                                {phone.model}
                              </span>
                            </div>
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {features.map((feature) => (
                        <tr key={feature.key} className="border-b dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors">
                          <td className="p-4 font-medium text-slate-700 dark:text-slate-300 bg-slate-50/50 dark:bg-slate-900/50">
                            {feature.label}
                          </td>
                          {phones.map((phone) => {
                            const value = getValue(phone, feature.key);
                            const displayValue = feature.format ? feature.format(value) : (value || '-');
                            return (
                              <td key={`${phone.id}-${feature.key}`} className="p-4 text-slate-800 dark:text-slate-200 text-center">
                                {displayValue}
                              </td>
                            );
                          })}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}
