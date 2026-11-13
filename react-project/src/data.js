export const hotels = [
  { id:'h1', name:'The Leela Palace', location:'Udaipur, Rajasthan', area:'Udaipur', price:28500, rating:4.9, reviews:1284, badge:'Guest favourite', image:'photo-1566073771259-6a8506099945', description:'A lakeside palace where the romance of Rajasthan meets warm, thoughtful hospitality.', amenities:['Pool','Spa','Lake view','Breakfast'], rooms:[{name:'Palace Room',capacity:2,price:28500},{name:'Royal Suite',capacity:3,price:46500}] },
  { id:'h2', name:'Taj Mahal Palace', location:'Colaba, Mumbai', area:'Mumbai', price:22000, rating:4.8, reviews:2051, badge:'Iconic stay', image:'photo-1542314831-068cd1dbfeeb', description:'An icon on the waterfront, known for gracious service and unforgettable city views.', amenities:['Pool','Spa','Sea view','Breakfast'], rooms:[{name:'Luxury Room',capacity:2,price:22000},{name:'Harbour Suite',capacity:3,price:39000}] },
  { id:'h3', name:'The Oberoi', location:'Gurugram, Delhi NCR', area:'Delhi', price:19500, rating:4.9, reviews:876, badge:'Top rated', image:'photo-1571896349842-33c89424de2d', description:'A serene urban retreat with contemporary design, fine dining and impeccable service.', amenities:['Pool','Gym','Spa','Breakfast'], rooms:[{name:'Premier Room',capacity:2,price:19500},{name:'Luxury Suite',capacity:3,price:34000}] },
  { id:'h4', name:'Ahilya by the Sea', location:'Nerul, Goa', area:'Goa', price:16800, rating:4.8, reviews:642, badge:'Hidden gem', image:'photo-1571896349842-33c89424de2d', description:'A beautifully intimate coastal hideaway tucked between palms and the Arabian Sea.', amenities:['Pool','Sea view','Breakfast','Garden'], rooms:[{name:'Garden Room',capacity:2,price:16800},{name:'Sea View Suite',capacity:3,price:29000}] },
  { id:'h5', name:'Wildflower Hall', location:'Shimla, Himachal Pradesh', area:'Shimla', price:32000, rating:4.9, reviews:511, badge:'Mountain escape', image:'photo-1510798831971-661eb04b3739', description:'A mountain sanctuary above the clouds, surrounded by cedar forest and Himalayan air.', amenities:['Pool','Spa','Mountain view','Breakfast'], rooms:[{name:'Deluxe Room',capacity:2,price:32000},{name:'Executive Suite',capacity:3,price:51000}] },
  { id:'h6', name:'The Tamara Coorg', location:'Yevakapadi, Coorg', area:'Coorg', price:24500, rating:4.7, reviews:728, badge:'Nature retreat', image:'photo-1445019980597-93fa8acb246c', description:'Slow down in a hillside hideaway framed by coffee estates and misty green valleys.', amenities:['Pool','Spa','Mountain view','Garden'], rooms:[{name:'Luxury Cottage',capacity:2,price:24500},{name:'Eden Lotus',capacity:3,price:39500}] },
]
export const destinations = [
  { name:'Udaipur', note:'Palaces by the lake', image:'photo-1599661046827-dacff0c0f09a' },
  { name:'Goa', note:'Your own pace by the sea', image:'photo-1512343879784-a960bf40e7f2' },
  { name:'Shimla', note:'A breath of mountain air', image:'photo-1500530855697-b586d89ba3ee' },
  { name:'Coorg', note:'Into the green', image:'photo-1441974231531-c6227db76b6e' },
]
export const photo = (id, w=900) => id?.startsWith('http') ? id : `https://images.unsplash.com/${id}?auto=format&fit=crop&w=${w}&q=85`
export const handleHotelImageError = event => { event.currentTarget.onerror=null; event.currentTarget.src='/hotel-placeholder.svg' }

