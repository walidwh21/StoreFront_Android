// Rebuilds products.json from the public DummyJSON API (optional: a snapshot is already included).
//   node fetch-products.mjs
import fs from 'node:fs/promises';

const CATEGORIES = [
  'beauty', 'fragrances', 'skin-care',
  'womens-dresses', 'womens-shoes', 'womens-bags',
  'womens-watches', 'womens-jewellery',
];

async function fetchCategory(category) {
  const res = await fetch(`https://dummyjson.com/products/category/${category}`);
  if (!res.ok) throw new Error(`${category}: HTTP ${res.status}`);
  return (await res.json()).products;
}

const toProduct = (d) => ({
  id: String(d.id),
  title: d.title,
  description: d.description ?? null,
  price: Number(d.price),
  discountPercentage: d.discountPercentage ?? null,
  rating: d.rating ?? null,
  stock: d.stock,
  restockDate: null,
  brand: d.brand ?? '',
  category: d.category,
  thumbnail: d.thumbnail,
  images: Array.isArray(d.images) ? d.images : [],
});

const all = [];
for (const category of CATEGORIES) {
  console.log(`fetching ${category} ...`);
  all.push(...(await fetchCategory(category)).map(toProduct));
}
await fs.writeFile(new URL('./products.json', import.meta.url), JSON.stringify(all, null, 2));
console.log(`saved ${all.length} products to products.json`);
