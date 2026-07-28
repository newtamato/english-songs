const CACHE = 'food-songs-v1';
const ASSETS = [
  '/',
  '/index.html',
  '/manifest.json',
  '/audio/01_Apples_and_Bananas.m4a',
  '/audio/02_Apple_Tree.m4a',
  '/audio/03_水果食物名称.m4a',
  '/audio/04_Do_You_Like_Broccoli_Ice_Cream.m4a',
  '/audio/05_Do_You_Like_Spaghetti_Yogurt.m4a',
  '/audio/06_Are_You_Hungry.m4a',
  '/audio/07_Hot_Cross_Buns.m4a',
  '/audio/08_Pat-A-Cake.m4a',
  '/audio/09_The_Muffin_Man.m4a',
  '/audio/10_Who_Took_The_Cookie.m4a',
  '/audio/11_The_Ice_Cream_Song.m4a'
];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(cache => cache.addAll(ASSETS))
  );
  self.skipWaiting();
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(
      keys.filter(k => k !== CACHE).map(k => caches.delete(k))
    ))
  );
  self.clients.claim();
});

self.addEventListener('fetch', e => {
  e.respondWith(
    caches.match(e.request).then(cached => cached || fetch(e.request))
  );
});
