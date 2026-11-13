-- Preserve V1/V2 history while moving amenities into the hotels row required by the API model.
ALTER TABLE hotels ADD COLUMN amenities JSON NULL AFTER badge;

UPDATE hotels h
SET amenities = COALESCE(
    (SELECT JSON_ARRAYAGG(a.amenity) FROM hotel_amenities a WHERE a.hotel_id = h.id),
    JSON_ARRAY()
);

ALTER TABLE hotels MODIFY amenities JSON NOT NULL;
DROP TABLE hotel_amenities;

CREATE INDEX idx_hotels_location_area ON hotels (location, area);
ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_price_positive CHECK (price_per_night > 0),
    ADD CONSTRAINT chk_rooms_inventory_positive CHECK (total_rooms > 0),
    ADD CONSTRAINT chk_rooms_capacity_positive CHECK (capacity > 0);

INSERT INTO hotels (id,name,location,area,description,image_url,rating,reviews,badge,amenities) VALUES
('h7','Conrad Pune','Mangalwar Peth, Pune, Maharashtra','Pune','A polished city retreat with gracious service, a restorative spa, and an easy connection to Pune’s historic heart.','https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=900&q=85',4.8,934,'City favourite',JSON_ARRAY('Pool','Spa','Gym','Breakfast')),
('h8','JW Marriott Hotel Pune','Senapati Bapat Road, Pune, Maharashtra','Pune','A contemporary high-rise stay with generous rooms, thoughtful dining, and sweeping views across the city.','https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=85',4.7,1106,'A little indulgence',JSON_ARRAY('Pool','Spa','Gym','Breakfast')),
('h9','The Leela Palace Bengaluru','Old Airport Road, Bengaluru, Karnataka','Bengaluru','A garden-framed palace stay bringing quiet elegance, warm hospitality, and a welcome pause from the city.','https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=900&q=85',4.9,1472,'Garden palace',JSON_ARRAY('Pool','Spa','Garden','Breakfast')),
('h10','Taj West End','Race Course Road, Bengaluru, Karnataka','Bengaluru','A storied urban sanctuary set among century-old trees, with intimate paths and gracious old-world charm.','https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=900&q=85',4.8,1217,'Heritage escape',JSON_ARRAY('Pool','Spa','Garden','Breakfast')),
('h11','Trident Nariman Point','Nariman Point, Mumbai, Maharashtra','Mumbai','A welcoming waterfront hotel with sweeping Marine Drive views, attentive service, and a calm place to return to.','https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=85',4.7,987,'Waterfront stay',JSON_ARRAY('Pool','Sea view','Gym','Breakfast')),
('h12','Taj Exotica Resort & Spa','Benaulim, South Goa, Goa','Goa','A secluded beachfront resort among palms, made for unhurried days and warm Goan hospitality.','https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=900&q=85',4.8,1430,'Beach retreat',JSON_ARRAY('Pool','Spa','Sea view','Breakfast')),
('h13','The Imperial New Delhi','Janpath, New Delhi','Delhi','A landmark New Delhi hotel blending graceful heritage architecture with thoughtful, modern comforts.','https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=900&q=85',4.8,1188,'Heritage icon',JSON_ARRAY('Pool','Spa','Garden','Breakfast'));

INSERT INTO rooms (id,hotel_id,room_type,price_per_night,total_rooms,capacity) VALUES
('h7-r1','h7','Deluxe Room',18500,8,2),('h7-r2','h7','Executive Suite',32000,3,3),
('h8-r1','h8','JW Deluxe Room',16500,10,2),('h8-r2','h8','Executive Suite',29500,4,3),
('h9-r1','h9','Grand Room',24500,8,2),('h9-r2','h9','Royal Suite',42000,3,3),
('h10-r1','h10','Luxury Room',20500,8,2),('h10-r2','h10','Heritage Suite',36000,3,3),
('h11-r1','h11','Deluxe Sea View',18000,7,2),('h11-r2','h11','Premier Suite',32000,3,3),
('h12-r1','h12','Garden Villa',26000,10,2),('h12-r2','h12','Sea View Villa',42000,4,3),
('h13-r1','h13','Art Deco Room',22000,9,2),('h13-r2','h13','Heritage Suite',38500,3,3);
