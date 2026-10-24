// Grants a role to an existing account – this is how you create your first admin.
//
//   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
//   node set-role.mjs someone@example.com ADMIN      (ADMIN | CUSTOMER | DELIVERY)
import admin from 'firebase-admin';

const [email, role] = process.argv.slice(2);
if (!process.env.GOOGLE_APPLICATION_CREDENTIALS || !email || !['ADMIN', 'CUSTOMER', 'DELIVERY'].includes(role)) {
  console.error('Usage: GOOGLE_APPLICATION_CREDENTIALS=key.json node set-role.mjs <email> <ADMIN|CUSTOMER|DELIVERY>');
  process.exit(1);
}

admin.initializeApp({ credential: admin.credential.applicationDefault() });
const user = await admin.auth().getUserByEmail(email);
await admin.firestore().collection('users').doc(user.uid).update({ role });
console.log(`${email} is now ${role}`);
