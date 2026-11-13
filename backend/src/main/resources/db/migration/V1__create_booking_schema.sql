CREATE TABLE hotels (
  id VARCHAR(40) PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  location VARCHAR(180) NOT NULL,
  area VARCHAR(80) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  rating DOUBLE NOT NULL,
  reviews INT NOT NULL,
  badge VARCHAR(80) NOT NULL
);
CREATE TABLE hotel_amenities (
  hotel_id VARCHAR(40) NOT NULL,
  amenity VARCHAR(80) NOT NULL,
  CONSTRAINT fk_amenity_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);
CREATE TABLE rooms (
  id VARCHAR(40) PRIMARY KEY,
  hotel_id VARCHAR(40) NOT NULL,
  room_type VARCHAR(120) NOT NULL,
  price_per_night DECIMAL(12,2) NOT NULL,
  total_rooms INT NOT NULL,
  capacity INT NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT fk_room_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
  INDEX idx_rooms_hotel (hotel_id)
);
CREATE TABLE bookings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  booking_reference VARCHAR(32) NOT NULL UNIQUE,
  room_id VARCHAR(40) NOT NULL,
  customer_name VARCHAR(160) NOT NULL,
  customer_email VARCHAR(254) NOT NULL,
  check_in DATE NOT NULL,
  check_out DATE NOT NULL,
  number_of_guests INT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms(id),
  INDEX idx_booking_room_dates_status (room_id,check_in,check_out,status),
  INDEX idx_booking_reference_email (booking_reference,customer_email),
  CONSTRAINT chk_booking_dates CHECK (check_out > check_in),
  CONSTRAINT chk_booking_guests CHECK (number_of_guests > 0)
);
