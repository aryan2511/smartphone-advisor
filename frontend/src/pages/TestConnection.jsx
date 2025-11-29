import { useState, useEffect } from 'react';
import { checkHealth, getAllPhones } from '../services/api';

function TestConnection() {
  const [backendStatus, setBackendStatus] = useState('checking...');
  const [phoneCount, setPhoneCount] = useState(0);
  const [error, setError] = useState(null);

  useEffect(() => {
    const testConnection = async () => {
      try {
        // Test health endpoint
        const isHealthy = await checkHealth();
        setBackendStatus(isHealthy ? '✅ Connected' : '❌ Disconnected');

        if (isHealthy) {
          // Test phones endpoint
          const phones = await getAllPhones();
          setPhoneCount(phones.length);
        }
      } catch (err) {
        setBackendStatus('❌ Error');
        setError(err.message);
      }
    };

    testConnection();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-slate-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl border-2 border-slate-200 p-12 max-w-md w-full">
        <h1 className="text-3xl font-light text-slate-800 mb-8 text-center">
          Backend Connection Test
        </h1>
        
        <div className="space-y-4">
          <div className="flex justify-between items-center p-4 bg-slate-50 rounded-xl">
            <span className="text-slate-600">Backend Status:</span>
            <span className="font-semibold">{backendStatus}</span>
          </div>
          
          <div className="flex justify-between items-center p-4 bg-slate-50 rounded-xl">
            <span className="text-slate-600">Phones in Database:</span>
            <span className="font-semibold">{phoneCount}</span>
          </div>

          {error && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm">
              Error: {error}
            </div>
          )}

          <div className="pt-4 text-center text-sm text-slate-500">
            <p>Make sure Spring Boot backend is running on:</p>
            <p className="font-mono mt-1">http://localhost:8080</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default TestConnection;
