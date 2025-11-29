/**
 * API Service for Phone Advisor
 * Handles all backend communication
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

/**
 * Get phone recommendations based on user preferences
 * @param {Object} preferences - User preferences
 * @param {string} preferences.productType - Product type (e.g., 'smartphone')
 * @param {string} preferences.budget - Budget range (e.g., '20-25')
 * @param {Object} preferences.priorities - Priority rankings (1-5)
 */
export const getRecommendations = async (preferences) => {
  try {
    const response = await fetch(`${API_BASE_URL}/recommend`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(preferences),
    });

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error fetching recommendations:', error);
    throw error;
  }
};

/**
 * Health check - verify backend is running
 */
export const checkHealth = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/health`);
    return response.ok;
  } catch (error) {
    console.error('Backend health check failed:', error);
    return false;
  }
};

/**
 * Get all phones (for testing)
 */
export const getAllPhones = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/phones`);
    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching phones:', error);
    throw error;
  }
};

/**
 * Get phone count in budget range
 */
export const getPhoneCount = async (budget) => {
  try {
    const url = budget 
      ? `${API_BASE_URL}/phones/count?budget=${budget}`
      : `${API_BASE_URL}/phones/count`;
    
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`API error: ${response.status}`);
    }
    return await response.json();
  } catch (error) {
    console.error('Error fetching phone count:', error);
    throw error;
  }
};
