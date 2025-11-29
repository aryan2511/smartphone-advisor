/**
 * Phone Data Synchronization Script
 * 
 * This script:
 * 1. Fetches phone data from GSMArena API
 * 2. Transforms it to match our database schema
 * 3. Inserts/updates phones in the database
 * 
 * Run with: npm run sync
 */

const gsmarena = require('gsmarena-api');
const db = require('./db');

/**
 * List of phones we want to track
 * Format: { brand: 'brand-id-from-gsmarena', model: 'search-term' }
 */
// const PHONES_TO_SYNC = [
//   { brand: 'samsung-phones-9', model: 'Galaxy S24' },
//   { brand: 'oneplus-phones-95', model: 'OnePlus 12' },
//   { brand: 'apple-phones-48', model: 'iPhone 15' },
//   { brand: 'xiaomi-phones-80', model: 'Xiaomi 14 Pro' },
//   { brand: 'google-phones-107', model: 'Pixel 8 Pro' },
//   { brand: 'realme-phones-118', model: 'Realme GT 5 Pro' },
//   { brand: 'nothing-phones-130', model: 'Nothing Phone (2a)' },
//   { brand: 'vivo-phones-98', model: 'Vivo X100 Pro' }
// ];

/**
 * Extract display size from specs
 * Example: "6.7\"" -> "6.7\" AMOLED"
 */
function extractDisplay(detailSpec) {
  const displayCategory = detailSpec.find(cat => cat.category === 'Display');
  if (!displayCategory) return 'N/A';
  
  const sizeSpec = displayCategory.specifications.find(s => s.name === 'Size');
  const typeSpec = displayCategory.specifications.find(s => s.name === 'Type');
  
  if (sizeSpec && typeSpec) {
    return `${sizeSpec.value} ${typeSpec.value}`;
  }
  return sizeSpec?.value || 'N/A';
}

/**
 * Extract processor info
 */
function extractProcessor(detailSpec) {
  const platformCategory = detailSpec.find(cat => cat.category === 'Platform');
  if (!platformCategory) return 'N/A';
  
  const chipsetSpec = platformCategory.specifications.find(s => s.name === 'Chipset');
  return chipsetSpec?.value || 'N/A';
}

/**
 * Extract memory (RAM)
 */
function extractMemory(detailSpec) {
  const memoryCategory = detailSpec.find(cat => cat.category === 'Memory');
  if (!memoryCategory) return 'N/A';
  
  const internalSpec = memoryCategory.specifications.find(s => s.name === 'Internal');
  if (internalSpec) {
    // Extract RAM from "256GB 8GB RAM" format
    const match = internalSpec.value.match(/(\d+GB)\s+RAM/);
    return match ? match[1] : 'N/A';
  }
  return 'N/A';
}

/**
 * Extract storage
 */
function extractStorage(detailSpec) {
  const memoryCategory = detailSpec.find(cat => cat.category === 'Memory');
  if (!memoryCategory) return 'N/A';
  
  const internalSpec = memoryCategory.specifications.find(s => s.name === 'Internal');
  if (internalSpec) {
    // Extract storage from "256GB 8GB RAM" format
    const match = internalSpec.value.match(/^(\d+GB)/);
    return match ? match[1] : 'N/A';
  }
  return 'N/A';
}

/**
 * Extract battery capacity
 */
function extractBattery(detailSpec) {
  const batteryCategory = detailSpec.find(cat => cat.category === 'Battery');
  if (!batteryCategory) return 'N/A';
  
  const typeSpec = batteryCategory.specifications.find(s => s.name === 'Type');
  if (typeSpec) {
    // Extract mAh from "5000 mAh" format
    const match = typeSpec.value.match(/(\d+\s*mAh)/);
    return match ? match[1] : 'N/A';
  }
  return 'N/A';
}

/**
 * Extract camera info
 */
function extractCamera(detailSpec) {
  const cameraCategory = detailSpec.find(cat => cat.category === 'Main Camera');
  if (!cameraCategory) return 'N/A';
  
  const modulesSpec = cameraCategory.specifications.find(s => s.name === 'Modules' || s.name === 'Single' || s.name === 'Dual' || s.name === 'Triple' || s.name === 'Quad');
  return modulesSpec?.value || 'N/A';
}

/**
 * Search for a specific phone model
 */
async function findPhone(searchTerm) {
  console.log(`🔍 Searching for: ${searchTerm}`);
  
  try {
    const results = await gsmarena.search.search(searchTerm);
    
    if (results && results.length > 0) {
      console.log(`✅ Found ${results.length} results`);
      return results[0]; // Return first match
    }
    
    console.log(`❌ No results found for: ${searchTerm}`);
    return null;
  } catch (error) {
    console.error(`❌ Error searching for ${searchTerm}:`, error.message);
    return null;
  }
}

/**
 * Fetch detailed phone specifications
 */
async function getPhoneDetails(phoneId) {
  console.log(`📱 Fetching details for: ${phoneId}`);
  
  try {
    const device = await gsmarena.catalog.getDevice(phoneId);
    console.log(`✅ Got details for: ${device.name}`);
    return device;
  } catch (error) {
    console.error(`❌ Error fetching details for ${phoneId}:`, error.message);
    return null;
  }
}

/**
 * Insert or update phone in database
 */
async function upsertPhone(phoneData) {
  const {
    brand,
    model,
    storage,
    price,
    display,
    processor,
    ram,
    battery,
    frontcamera,
    rearcamera,
    expandablestorage,
    imageUrl,
    cameraScore,
    batteryScore,
    softwareScore,
    privacyScore,
    looksScore
  } = phoneData;

  // SQL query to insert or update
  // ON CONFLICT = if phone already exists (by brand+model), update it
  const query = `
    INSERT INTO phones (
      brand, model, price, 
      display, processor, ram, storage, battery, camera,
      image_url,
      camera_score, battery_score, software_score, privacy_score, looks_score
    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
    ON CONFLICT (brand, model) 
    DO UPDATE SET
      price = EXCLUDED.price,
      display = EXCLUDED.display,
      processor = EXCLUDED.processor,
      ram = EXCLUDED.ram,
      storage = EXCLUDED.storage,
      battery = EXCLUDED.battery,
      camera = EXCLUDED.camera,
      image_url = EXCLUDED.image_url,
      camera_score = EXCLUDED.camera_score,
      battery_score = EXCLUDED.battery_score,
      software_score = EXCLUDED.software_score,
      privacy_score = EXCLUDED.privacy_score,
      looks_score = EXCLUDED.looks_score
    RETURNING id, brand, model;
  `;

  const values = [
    brand, model, price,
    display, processor, ram, storage, battery, camera,
    imageUrl,
    cameraScore, batteryScore, softwareScore, privacyScore, looksScore
  ];

  try {
    const result = await db.query(query, values);
    console.log(`✅ Upserted phone: ${brand} ${model} (ID: ${result.rows[0].id})`);
    return result.rows[0];
  } catch (error) {
    console.error(`❌ Error upserting phone:`, error);
    throw error;
  }
}

/**
 * Main sync function
 */
async function syncPhones() {
  console.log('🚀 Starting phone data synchronization...\n');
  
  let successCount = 0;
  let errorCount = 0;

  for (const phoneConfig of PHONES_TO_SYNC) {
    console.log(`\n${'='.repeat(60)}`);
    console.log(`Processing: ${phoneConfig.model}`);
    console.log('='.repeat(60));
    
    try {
      // Step 1: Search for phone
      const searchResult = await findPhone(phoneConfig.model);
      if (!searchResult) {
        console.log(`⚠️  Skipping ${phoneConfig.model} - not found\n`);
        errorCount++;
        continue;
      }

      // Step 2: Get detailed specs
      const details = await getPhoneDetails(searchResult.id);
      if (!details) {
        console.log(`⚠️  Skipping ${phoneConfig.model} - couldn't fetch details\n`);
        errorCount++;
        continue;
      }

      // Step 3: Extract and transform data
      const phoneData = {
        brand: phoneConfig.brand.split('-')[0], // Extract brand name
        model: phoneConfig.model,
        price: 0, // We'll need to set this manually or fetch from another source
        display: extractDisplay(details.detailSpec),
        processor: extractProcessor(details.detailSpec),
        ram: extractMemory(details.detailSpec),
        storage: extractStorage(details.detailSpec),
        battery: extractBattery(details.detailSpec),
        camera: extractCamera(details.detailSpec),
        imageUrl: details.img,
        // Keep existing scores (we'll need to calculate these)
        cameraScore: 85,
        batteryScore: 85,
        softwareScore: 85,
        privacyScore: 85,
        looksScore: 85
      };

      console.log('\n📊 Extracted data:');
      console.log(JSON.stringify(phoneData, null, 2));

      // Step 4: Save to database
      await upsertPhone(phoneData);
      successCount++;

      // Be nice to GSMArena - wait longer between requests to avoid rate limiting
      // 429 error = Too Many Requests, so we increase delay
      console.log('⏳ Waiting 3 seconds before next phone...');
      await new Promise(resolve => setTimeout(resolve, 3000));

    } catch (error) {
      console.error(`❌ Error processing ${phoneConfig.model}:`, error.message);
      errorCount++;
    }
  }

  console.log(`\n${'='.repeat(60)}`);
  console.log('📊 Synchronization Summary');
  console.log('='.repeat(60));
  console.log(`✅ Success: ${successCount}`);
  console.log(`❌ Errors: ${errorCount}`);
  console.log(`📱 Total: ${PHONES_TO_SYNC.length}`);
  console.log('='.repeat(60) + '\n');
}

// Run the sync
syncPhones()
  .then(() => {
    console.log('✅ Sync completed successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Sync failed:', error);
    process.exit(1);
  });
