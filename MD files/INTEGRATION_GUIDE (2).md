# Frontend-Backend Integration Guide

## ✅ Integration Complete!

Your SmartPick application is now fully integrated. Here's how to test it:

---

## 🚀 Starting the Application

### 1. Start Backend (Spring Boot)
```bash
cd D:\phone-pick-helper\backend
mvn spring-boot:run
```
**Backend will run on:** `http://localhost:8080`

### 2. Start Frontend (React)
```bash
cd D:\phone-pick-helper\frontend
npm run dev
```
**Frontend will run on:** `http://localhost:5173`

---

## 🧪 Testing the Integration

### Step 1: Test Backend Connection
Visit: `http://localhost:5173/test`

This page will show:
- ✅ Backend connection status
- 📱 Number of phones in database

### Step 2: Complete User Flow
1. Visit: `http://localhost:5173/`
2. Select product type (Smartphone)
3. Choose budget range (e.g., ₹20K-₹25K)
4. Drag and drop to rank priorities:
   - Camera
   - Battery
   - Software
   - Privacy
   - Looks
5. Click "Get Recommendations"

### Step 3: View Recommendations
You should see:
- Top 5 phone recommendations
- Match scores (0-100%)
- Phone specs and prices
- Feature scores breakdown
- Flipkart affiliate links

---

## 📊 What Data Flows Through

### Frontend → Backend Request:
```json
{
  "productType": "smartphone",
  "budget": "20-25",
  "priorities": {
    "camera": 1,
    "battery": 2,
    "software": 3,
    "privacy": 4,
    "looks": 5
  }
}
```

### Backend → Frontend Response:
```json
[
  {
    "phone": {
      "id": 1,
      "brand": "Samsung",
      "model": "Galaxy A36 5G",
      "price": 26990,
      "memory_and_storage": "8 GB RAM | 128 GB ROM",
      "displayInfo": "17.02 cm (6.7 inch) Full HD+ Display",
      "cameraInfo": "50MP + 8MP | 12MP Front Camera",
      "processor": "Qualcomm Snapdragon 6 Gen 3",
      "battery": "5000 mAh",
      "imageUrl": "https://...",
      "cameraScore": 85,
      "batteryScore": 90,
      "softwareScore": 82,
      "privacyScore": 82,
      "looksScore": 85,
      "affiliateFlipkart": "https://..."
    },
    "totalScore": 86.4,
    "featureScore": 84.8,
    "sentimentScore": 0.0
  }
]
```

---

## ⚠️ Troubleshooting

### Backend Not Connecting?
1. Check if Spring Boot is running: `http://localhost:8080/api/health`
2. Check console for CORS errors
3. Verify database connection in `application.properties`

### No Recommendations Showing?
1. Check browser console for errors
2. Verify data exists in database (visit `/test` page)
3. Check network tab for API responses

### CORS Issues?
Already configured in `CorsConfig.java`:
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
}
```

---

## 📁 Files Modified/Created

### Frontend:
- ✅ `src/services/api.js` - API service layer
- ✅ `src/pages/Recommendations.jsx` - Updated to use API
- ✅ `src/pages/TestConnection.jsx` - New test page
- ✅ `src/App.jsx` - Added test route

### Backend:
- ✅ Already complete and ready!

---

## 🎯 Next Steps

1. **Test the complete flow** - Start both servers and go through the user journey
2. **Verify recommendations** - Check if phones match budget and priorities
3. **Test edge cases** - Try different budget ranges and priority combinations

---

## 📊 Token Usage: ~78K / 190K = 41% ✅
