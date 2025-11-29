/**
 * Test Database Connection
 * 
 * Simple script to verify your database connection works
 * Run with: npm run test
 */

const db = require('./db');

async function testConnection() {
  console.log('🔍 Testing database connection...\n');

  try {
    // Test query - get current database time
    const result = await db.query('SELECT NOW() as current_time, version() as pg_version');
    
    console.log('✅ Database connection successful!');
    console.log('📊 Database info:');
    console.log(`   Time: ${result.rows[0].current_time}`);
    console.log(`   PostgreSQL: ${result.rows[0].pg_version}\n`);

    // Count existing phones
    const countResult = await db.query('SELECT COUNT(*) as phone_count FROM phones');
    console.log(`📱 Current phones in database: ${countResult.rows[0].phone_count}\n`);

    console.log('✅ Test completed successfully');
    process.exit(0);
    
  } catch (error) {
    console.error('❌ Database connection failed:');
    console.error(error.message);
    console.error('\n💡 Check your .env file and make sure DATABASE_URL is correct\n');
    process.exit(1);
  }
}

testConnection();
