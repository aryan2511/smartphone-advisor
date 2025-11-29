/**
 * Update Battery Scores Script
 * Recalculates battery scores for all phones in the database
 */

const db = require('./db');

async function updateBatteryScores() {
  console.log('🔋 Updating battery scores...\n');
  
  try {
    // Get all phones
    const result = await db.query('SELECT id, brand, model, battery FROM phones');
    const phones = result.rows;
    
    console.log(`📱 Found ${phones.length} phones to update\n`);
    
    let updateCount = 0;
    
    for (const phone of phones) {
      // Calculate new battery score
      let batteryScore = 70;
      const batteryMatch = phone.battery?.match(/[\d,]+/);
      
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
        
        // Update the phone
        await db.query(
          'UPDATE phones SET battery_score = $1 WHERE id = $2',
          [batteryScore, phone.id]
        );
        
        updateCount++;
        
        if (updateCount % 50 === 0) {
          console.log(`✅ Updated ${updateCount}/${phones.length} phones...`);
        }
      }
    }
    
    console.log(`\n✅ Successfully updated ${updateCount} phone battery scores!`);
    process.exit(0);
    
  } catch (error) {
    console.error('❌ Error updating battery scores:', error);
    process.exit(1);
  }
}

updateBatteryScores();
