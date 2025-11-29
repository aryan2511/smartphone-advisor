require('dotenv').config();
const express = require('express');
const cors = require('cors');

const recommendationsRouter = require('../recommendation-service/recommendations');
const phonesRouter = require('../recommendation-service/phones');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.get('/', (req, res) => {
  res.json({ 
    message: 'SmartPick Recommendation API',
    endpoints: {
      recommendations: 'GET /api/recommendations?budget=60000&priorities=camera,battery,performance,privacy,design',
      phone: 'GET /api/phones/:id',
      reviews: 'GET /api/phones/:id/reviews'
    }
  });
});

app.use('/api/recommendations', recommendationsRouter);
app.use('/api/phones', phonesRouter);

// Error handling
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Something went wrong!' });
});

app.listen(PORT, () => {
  console.log(`🚀 SmartPick API running on http://localhost:${PORT}`);
  console.log(`📊 Endpoints:`);
  console.log(`   GET /api/recommendations?budget=X&priorities=...`);
  console.log(`   GET /api/phones/:id`);
  console.log(`   GET /api/phones/:id/reviews`);
});
