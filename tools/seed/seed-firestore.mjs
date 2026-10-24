// Uploads products.json into the `products` collection (document id = product id).
//
//   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
//   node seed-firestore.mjs
//
// Keep the service-account file OUTSIDE the repository.
import fs from 'node:fs/promises';
import admin from 'firebase-admin';

if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error('Set GOOGLE_APPLICATION_CREDENTIALS to your service-account JSON file.');
  process.exit(1);
}

admin.initializeApp({ credential: admin.credential.applicationDefault() });
const db = admin.firestore();

const products = JSON.parse(await fs.readFile(new URL('./products.json', import.meta.url), 'utf8'));
const valid = products.filter((p) => p.id && String(p.id).trim() !== '');
console.log(`${valid.length} valid of ${products.length} products`);

// Firestore batches hold up to 500 writes.
for (let i = 0; i < valid.length; i += 400) {
  const batch = db.batch();
  for (const product of valid.slice(i, i + 400)) {
    batch.set(db.collection('products').doc(String(product.id)), product);
  }
  await batch.commit();
  console.log(`committed ${Math.min(i + 400, valid.length)}/${valid.length}`);
}
console.log('done');
