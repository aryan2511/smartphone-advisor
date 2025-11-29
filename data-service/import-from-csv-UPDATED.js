/**
 * CSV to Database Import Script
 * 
 * This script:
 * 1. Reads CSV file with phone data
 * 2. Imports all phones to Neon database
 * 3. Calculates feature scores based on specs
 * 
 * CSV Format (phone_data.csv):
 * brand, model, price, memory_and_storage, display, camera, processor, battery, image, url
 */

const fs = require('fs');
const csv = require('csv-parser');
const db = require('./db');

// CSV filename
const CSV_FILENAME = './phone_data.csv';

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
 * Parse price from CSV
 */
function parsePrice(priceStr) {
  if (!priceStr || priceStr === 'N/A' || priceStr === '') return null;
  const cleaned = priceStr.toString().replace(/[^0-9]/g, '');
  const price = parseInt(cleaned);
  return isNaN(price) ? null : price;
}

/**
 * Transform CSV row to database format
 */
function transformPhone(row) {
  return {
    brand: (row.brand || '').trim(),
    model: (row.model || '').trim(),
    price: parsePrice(row.price),
    memoryAndStorage: (row.memory_and_storage || '').trim(),
    display: (row.display || '').trim(),
    camera: (row.camera || '').trim(),
    processor: (row.processor || '').trim(),
    battery: (row.battery || '').trim(),
    imageUrl: (row.image || '').trim(),
    flipkartUrl: (row.url || '#').trim()
  };
}

/**
 * Calculate feature scores based on specs
 */
function calculateScores(phone) {
  // Camera score (based on MP)
  let cameraScore = 70;
  const cameraStr = phone.camera.toLowerCase();
  if (cameraStr.includes('200mp')) cameraScore = 95;
  else if (cameraStr.includes('108mp') || cameraStr.includes('100mp')) cameraScore = 92;
  else if (cameraStr.includes('64mp') || cameraStr.includes('50mp')) cameraScore = 85;
  else if (cameraStr.includes('48mp')) cameraScore = 82;
  else if (cameraStr.includes('12mp') && phone.brand.toLowerCase().includes('apple')) cameraScore = 90;
  
  // Battery score (based on mAh) - More granular scoring
  let batteryScore = 70;
  const batteryMatch = phone.battery.match(/[\d,]+/);
  if (batteryMatch) {
    const batteryMah = parseInt(batteryMatch[0].replace(/,/g, ''));
    
    // Premium tier (6500+ mAh)
    if (batteryMah >= 7000) batteryScore = 98;
    else if (batteryMah >= 6500) batteryScore = 95;
    // High capacity (6000-6499 mAh)
    else if (batteryMah >= 6250) batteryScore = 92;
    else if (batteryMah >= 6000) batteryScore = 90;
    // Good capacity (5500-5999 mAh)
    else if (batteryMah >= 5750) batteryScore = 88;
    else if (batteryMah >= 5500) batteryScore = 86;
    // Above average (5000-5499 mAh)
    else if (batteryMah >= 5250) batteryScore = 84;
    else if (batteryMah >= 5000) batteryScore = 82;
    // Average (4500-4999 mAh)
    else if (batteryMah >= 4750) batteryScore = 80;
    else if (batteryMah >= 4500) batteryScore = 78;
    // Below average (4000-4499 mAh)
    else if (batteryMah >= 4250) batteryScore = 76;
    else if (batteryMah >= 4000) batteryScore = 74;
    // Low (3500-3999 mAh)
    else if (batteryMah >= 3750) batteryScore = 72;
    else if (batteryMah >= 3500) batteryScore = 71;
  }
  
  // Software score (based on processor)
  let softwareScore = 75;
  const processor = phone.processor.toLowerCase();
  if (processor.includes('a18') || processor.includes('a17 pro')) softwareScore = 98;
  else if (processor.includes('a17') || processor.includes('a16')) softwareScore = 95;
  else if (processor.includes('a15')) softwareScore = 92;
  else if (processor.includes('a14')) softwareScore = 88;
  else if (processor.includes('a13')) softwareScore = 85;
  else if (processor.includes('8 gen 3')) softwareScore = 95;
  else if (processor.includes('8 gen 2')) softwareScore = 92;
  else if (processor.includes('8+ gen 1') || processor.includes('8 gen 1')) softwareScore = 88;
  else if (processor.includes('dimensity 9300')) softwareScore = 93;
  else if (processor.includes('dimensity 9200')) softwareScore = 90;
  else if (processor.includes('dimensity 9000')) softwareScore = 88;
  else if (processor.includes('dimensity 8')) softwareScore = 85;
  else if (processor.includes('dimensity 7')) softwareScore = 80;
  
  // Privacy score (based on brand)
  let privacyScore = 70;
  const brand = phone.brand.toLowerCase();
  if (brand.includes('apple')) privacyScore = 95;
  else if (brand.includes('google')) privacyScore = 88;
  else if (brand.includes('samsung')) privacyScore = 82;
  else if (brand.includes('oneplus')) privacyScore = 75;
  
  // Looks score (design - based on brand and price)
  let looksScore = 75;
  if (brand.includes('apple')) looksScore = 92;
  else if (brand.includes('nothing')) looksScore = 90;
  else if (brand.includes('samsung')) looksScore = 85;
  else if (brand.includes('oneplus')) looksScore = 82;
  
  // Adjust for premium phones
  if (phone.price > 80000) looksScore = Math.min(95, looksScore + 5);
  else if (phone.price > 50000) looksScore = Math.min(90, looksScore + 3);
  
  return {
    cameraScore: Math.min(98, Math.max(70, cameraScore)),
    batteryScore: Math.min(95, Math.max(70, batteryScore)),
    softwareScore: Math.min(98, Math.max(70, softwareScore)),
    privacyScore: Math.min(95, Math.max(70, privacyScore)),
    looksScore: Math.min(95, Math.max(70, looksScore))
  };
}

/**
 * Insert phone into database
 */
async function insertPhone(phone) {
  const query = `
    INSERT INTO phones (
      brand, model, price,
      memory_and_storage, display_info, camera, processor, battery,
      image_url,
      camera_score, battery_score, software_score, privacy_score, looks_score,
      affiliate_amazon, affiliate_flipkart
    ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
    RETURNING id;
  `;
  
  const scores = calculateScores(phone);
  
  const values = [
    phone.brand,
    phone.model,
    phone.price,
    phone.memoryAndStorage,
    phone.display,
    phone.camera,
    phone.processor,
    phone.battery,
    phone.imageUrl || 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400',
    scores.cameraScore,
    scores.batteryScore,
    scores.softwareScore,
    scores.privacyScore,
    scores.looksScore,
    '#', // Amazon affiliate link placeholder
    phone.flipkartUrl || '#' // Flipkart affiliate link from CSV
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
    console.log(`📖 Reading CSV file: ${CSV_FILENAME}`);
    const rawPhones = await readCSV(CSV_FILENAME);
    console.log(`✅ Found ${rawPhones.length} phones in CSV\n`);
    
    // Step 2: Transform data
    console.log('🔄 Transforming data...');
    const phones = rawPhones.map(transformPhone);
    
    // Step 3: Filter phones with valid data
    console.log('💰 Filtering phones...');
    const validPhones = phones.filter(phone => {
      const hasValidBrand = phone.brand && phone.brand !== '';
      const hasValidModel = phone.model && phone.model !== '';
      const hasValidPrice = phone.price && phone.price > 0;
      
      if (!hasValidBrand || !hasValidModel || !hasValidPrice) {
        console.log(`  ⚠️  Skipping invalid phone: ${phone.brand || 'N/A'} ${phone.model || 'N/A'}`);
        return false;
      }
      return true;
    });
    
    console.log(`✅ ${validPhones.length} valid phones ready for import\n`);
    
    // Show sample
    console.log('📊 Sample phones:');
    validPhones.slice(0, 5).forEach(p => {
      console.log(`  ${p.brand} ${p.model} - ₹${p.price?.toLocaleString()}`);
    });
    console.log();
    
    // Step 4: Insert into database
    console.log('💾 Inserting into database...\n');
    let successCount = 0;
    let errorCount = 0;
    
    for (const phone of validPhones) {
      const result = await insertPhone(phone);
      if (result) {
        successCount++;
        if (successCount % 10 === 0) {
          console.log(`  ✅ Processed ${successCount}/${validPhones.length} phones...`);
        }
      } else {
        errorCount++;
      }
    }
    
    console.log('\n' + '='.repeat(60));
    console.log('📊 Import Summary');
    console.log('='.repeat(60));
    console.log(`✅ Successfully imported: ${successCount} phones`);
    console.log(`❌ Errors: ${errorCount}`);
    console.log(`📱 Total processed: ${validPhones.length}`);
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
