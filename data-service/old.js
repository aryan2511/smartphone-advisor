/**
 * CSV to Database Import Script - Updated for 2025 Dataset
 */

const fs = require('fs');
const csv = require('csv-parser');
const db = require('./db');

// Budget categories (in rupees)
const BUDGET_CATEGORIES = [
  { name: 'under_15k', min: 0, max: 15000 },
  { name: '15k_20k', min: 15000, max: 20000 },
  { name: '20k_25k', min: 20000, max: 25000 },
  { name: '25k_30k', min: 25000, max: 30000 },
  { name: '30k_35k', min: 30000, max: 35000 },
  { name: '35k_40k', min: 35000, max: 40000 },
  { name: '40k_50k', min: 40000, max: 50000 },
  { name: '50k_60k', min: 50000, max: 60000 },
  { name: '60k_70k', min: 60000, max: 70000 },
  { name: '70k_80k', min: 70000, max: 80000 },
  { name: '80k_90k', min: 80000, max: 90000 },
  { name: '90k_plus', min: 90000, max: 999999 }
];

// Popular brands to focus on
const FOCUS_BRANDS = [
  'samsung', 'apple', 'oneplus', 'xiaomi', 'realme', 
  'google', 'motorola', 'vivo', 'oppo', 'nothing', 'poco', 'iqoo'
];

/**
 * Read and parse CSV file
 */
async function readCSV(filename) {
  return new Promise((resolve, reject) => {
    const phones = [];
    
    fs.createReadStream(filename)
      .pipe(csv())
      .on('data', (row) => phones.push(row))
      .on('end', () => resolve(phones))
      .on('error', (error) => reject(error));
  });
}

/**
 * Parse price from CSV (handles comma-separated format)
 */
function parsePrice(priceStr) {
  if (!priceStr || priceStr === 'N/A' || priceStr === '') return null;
  // Remove commas and non-numeric characters except digits
  const cleaned = priceStr.toString().replace(/[^0-9]/g, '');
  const price = parseInt(cleaned);
  return isNaN(price) ? null : price;
}

/**
 * Parse RAM (extract number)
 */
function parseRAM(ramStr) {
  if (!ramStr) return 'N/A';
  const match = ramStr.match(/(\d+)\s*GB/i);
  return match ? `${match[1]}GB` : ramStr;
}

/**
 * Parse storage from model name (common in India)
 */
function parseStorage(modelName) {
  const match = modelName.match(/(\d+)\s*GB/i);
  return match ? `${match[1]}GB` : '128GB';
}

/**
 * Transform CSV row to our format
 */
function transformPhone(row) {
  const price = parsePrice(row['Launched Price (India)']);
  
  return {
    brand: row['Company Name'] || 'Unknown',
    model: row['Model Name'] || 'Unknown',
    price: price,
    year: parseInt(row['Launched Year']) || 2024,
    display: row['Screen Size'] || 'N/A',
    processor: row['Processor'] || 'N/A',
    ram: parseRAM(row['RAM']),
    storage: parseStorage(row['Model Name']),
    battery: row['Battery Capacity'] || 'N/A',
    camera: row['Back Camera'] || 'N/A',
    frontCamera: row['Front Camera'] || 'N/A',
    weight: row['Mobile Weight'] || 'N/A'
  };
}

/**
 * Calculate feature scores based on specs
 */
function calculateScores(phone) {
  // Camera score (based on MP and features)
  let cameraScore = 70;
  const cameraStr = phone.camera.toLowerCase();
  if (cameraStr.includes('200mp')) cameraScore = 95;
  else if (cameraStr.includes('108mp') || cameraStr.includes('100mp')) cameraScore = 92;
  else if (cameraStr.includes('64mp') || cameraStr.includes('50mp')) cameraScore = 85;
  else if (cameraStr.includes('48mp')) cameraScore = 82;
  
  // Battery score (based on mAh)
  let batteryScore = 70;
  const batteryMatch = phone.battery.match(/[\d,]+/);
  if (batteryMatch) {
    const batteryMah = parseInt(batteryMatch[0].replace(/,/g, ''));
    if (batteryMah >= 5500) batteryScore = 95;
    else if (batteryMah >= 5000) batteryScore = 90;
    else if (batteryMah >= 4500) batteryScore = 85;
    else if (batteryMah >= 4000) batteryScore = 80;
    else if (batteryMah >= 3500) batteryScore = 75;
  }
  
  // Performance score (based on processor)
  let performanceScore = 75;
  const processor = phone.processor.toLowerCase();
  if (processor.includes('a18') || processor.includes('a17 pro')) performanceScore = 98;
  else if (processor.includes('a17') || processor.includes('a16')) performanceScore = 95;
  else if (processor.includes('8 gen 3')) performanceScore = 95;
  else if (processor.includes('8 gen 2')) performanceScore = 92;
  else if (processor.includes('8+ gen 1') || processor.includes('8 gen 1')) performanceScore = 88;
  else if (processor.includes('dimensity 9300')) performanceScore = 93;
  else if (processor.includes('dimensity 9200')) performanceScore = 90;
  else if (processor.includes('dimensity 9000')) performanceScore = 88;
  else if (processor.includes('dimensity 8')) performanceScore = 85;
  else if (processor.includes('dimensity 7')) performanceScore = 80;
  
  // Privacy score (based on brand)
  let privacyScore = 70;
  const brand = phone.brand.toLowerCase();
  if (brand.includes('apple')) privacyScore = 95;
  else if (brand.includes('google')) privacyScore = 88;
  else if (brand.includes('samsung')) privacyScore = 82;
  else if (brand.includes('oneplus')) privacyScore = 75;
  
  // Design score (based on brand and year)
  let designScore = 75;
  if (phone.year >= 2024) designScore += 10;
  else if (phone.year >= 2023) designScore += 5;
  
  if (brand.includes('apple')) designScore += 10;
  else if (brand.includes('nothing')) designScore += 15;
  else if (brand.includes('samsung')) designScore += 8;
  else if (brand.includes('oneplus')) designScore += 5;
  
  return {
    cameraScore: Math.min(98, Math.max(70, cameraScore)),
    batteryScore: Math.min(95, Math.max(70, batteryScore)),
    softwareScore: Math.min(98, Math.max(70, performanceScore)),
    privacyScore: Math.min(95, Math.max(70, privacyScore)),
    looksScore: Math.min(95, Math.max(70, designScore))
  };
}

/**
 * Filter phones by budget and select top 5 per category
 */
function selectPhonesByBudget(phones) {
  const selected = {};
  
  // Group phones by budget category
  BUDGET_CATEGORIES.forEach(category => {
    const phonesInBudget = phones.filter(phone => {
      const price = phone.price || 0;
      return price >= category.min && price < category.max;
    });
    
    // Sort and take top 5
    const top5 = phonesInBudget
      .sort((a, b) => {
        // Prefer newer phones
        if (a.year !== b.year) return b.year - a.year;
        
        // Prefer popular brands
        const brandScoreA = FOCUS_BRANDS.indexOf(a.brand.toLowerCase()) !== -1 ? 100 : 0;
        const brandScoreB = FOCUS_BRANDS.indexOf(b.brand.toLowerCase()) !== -1 ? 100 : 0;
        if (brandScoreA !== brandScoreB) return brandScoreB - brandScoreA;
        
        // Then by specs quality (higher price in same category = better specs)
        return (b.price || 0) - (a.price || 0);
      })
      .slice(0, 5);
    
    selected[category.name] = top5;
  });
  
  return selected;
}

/**
 * Insert phone into database
 */
async function insertPhone(phone) {
  const query = `
    INSERT INTO phones (
      brand, model, price,
      display, processor, ram, storage, battery, camera,
      image_url,
      camera_score, battery_score, software_score, privacy_score, looks_score,
      affiliate_amazon, affiliate_flipkart
    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17)
    ON CONFLICT (brand, model) DO UPDATE SET
      price = EXCLUDED.price,
      display = EXCLUDED.display,
      processor = EXCLUDED.processor,
      ram = EXCLUDED.ram,
      storage = EXCLUDED.storage,
      battery = EXCLUDED.battery,
      camera = EXCLUDED.camera,
      camera_score = EXCLUDED.camera_score,
      battery_score = EXCLUDED.battery_score,
      software_score = EXCLUDED.software_score,
      privacy_score = EXCLUDED.privacy_score,
      looks_score = EXCLUDED.looks_score
    RETURNING id;
  `;
  
  const scores = calculateScores(phone);
  
  const values = [
    phone.brand,
    phone.model,
    phone.price,
    phone.display,
    phone.processor,
    phone.ram,
    phone.storage,
    phone.battery,
    phone.camera,
    scores.cameraScore,
    scores.batteryScore,
    scores.softwareScore,
    scores.privacyScore,
    scores.looksScore,
    '#', // We'll add affiliate links later
    '#'
  ];
  
  try {
    const result = await db.query(query, values);
    return result.rows[0];
  } catch (error) {
    console.error(`❌ Error inserting ${phone.brand} ${phone.model}:`, error.message);
    return null;
  }
}

/**
 * Main import function
 */
async function importPhones() {
  console.log('🚀 Starting phone import from CSV...\n');
  
  try {
    // Step 1: Read CSV
    console.log('📖 Reading CSV file...');
    const rawPhones = await readCSV('./phones_dataset.csv');
    console.log(`✅ Found ${rawPhones.length} phones in CSV\n`);
    
    // Step 2: Transform data
    console.log('🔄 Transforming data...');
    const allPhones = rawPhones.map(transformPhone);
    
    // Step 3: Filter phones with valid prices and recent years
    console.log('💰 Filtering phones...');
    const filtered = allPhones.filter(phone => {
      const brand = phone.brand.toLowerCase();
      const hasValidPrice = phone.price && phone.price > 0;
      const isRecentYear = phone.year >= 2023;
      const isFocusBrand = FOCUS_BRANDS.some(b => brand.includes(b));
      
      return hasValidPrice && isRecentYear && isFocusBrand;
    });
    
    console.log(`✅ Filtered to ${filtered.length} relevant phones\n`);
    
    // Show sample of what we're working with
    console.log('📊 Sample phones:');
    filtered.slice(0, 3).forEach(p => {
      console.log(`  ${p.brand} ${p.model} - ₹${p.price?.toLocaleString()} (${p.year})`);
    });
    console.log();
    
    // Step 4: Select top 5 per budget
    console.log('📊 Selecting top 5 phones per budget category...');
    const selected = selectPhonesByBudget(filtered);
    
    let totalSelected = 0;
    Object.entries(selected).forEach(([category, phones]) => {
      if (phones.length > 0) {
        console.log(`  ${category}: ${phones.length} phones`);
        totalSelected += phones.length;
      }
    });
    console.log(`\n✅ Selected ${totalSelected} phones total\n`);
    
    // Step 5: Insert into database
    console.log('💾 Inserting into database...\n');
    let successCount = 0;
    let errorCount = 0;
    
    for (const [category, phones] of Object.entries(selected)) {
      if (phones.length === 0) continue;
      
      console.log(`📱 ${category.replace(/_/g, ' ').toUpperCase()}:`);
      for (const phone of phones) {
        const result = await insertPhone(phone);
        if (result) {
          console.log(`  ✅ ${phone.brand} ${phone.model} - ₹${phone.price.toLocaleString()}`);
          successCount++;
        } else {
          errorCount++;
        }
      }
      console.log();
    }
    
    console.log('='.repeat(60));
    console.log('📊 Import Summary');
    console.log('='.repeat(60));
    console.log(`✅ Successfully imported: ${successCount} phones`);
    console.log(`❌ Errors: ${errorCount}`);
    console.log('='.repeat(60) + '\n');
    
    console.log('✅ Import completed successfully!');
    process.exit(0);
    
  } catch (error) {
    console.error('❌ Import failed:', error);
    process.exit(1);
  }
}

// Run the import
importPhones();
