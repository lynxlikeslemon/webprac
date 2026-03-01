CREATE TABLE company(
	company_id integer PRIMARY KEY,
	company_name varchar(64) NOT NULL
);

CREATE TABLE airport(
	airport_id char(3) PRIMARY KEY,
	airport_name varchar(64) NOT NULL,
	city varchar(64) NOT NULL
);

CREATE TABLE flight(
	flight_id varchar(8) PRIMARY KEY,
	company_id integer REFERENCES company NOT NULL,
	departure_airport_id char(3) REFERENCES airport NOT NULL,
	arrival_airport_id char(3) REFERENCES airport NOT NULL,
	departure_time timestamp NOT NULL,
	arrival_time timestamp NOT NULL,
	price numeric(10,2) NOT NULL CHECK (price >= 0),
	places_total integer NOT NULL,
	places_taken integer NOT NULL
	CHECK (places_taken <= places_total)
	CHECK (arrival_time >= departure_time)
);

CREATE TABLE client(
	client_id integer PRIMARY KEY,
	first_name text NOT NULL,
	last_name text NOT NULL,
	fathers_name text NOT NULL,
	phone_number varchar(16) UNIQUE NOT NULL,
	email text,
	address text
);

CREATE TABLE bonus_card(
	bonus_id integer PRIMARY KEY,
	company_id integer REFERENCES company NOT NULL,
	client_id integer REFERENCES client NOT NULL,
	amount numeric(10,2) NOT NULL CHECK (amount >= 0)
);

CREATE TABLE ticket(
	ticket_id integer PRIMARY KEY,
	flight_id varchar(8) REFERENCES flight NOT NULL,
	client_id integer REFERENCES client NOT NULL,
	price numeric(10,2) NOT NULL CHECK (price >= 0),
	bonus_card_used integer REFERENCES bonus_card,
	bonus_amount_used numeric(10, 2),
	is_paid_for boolean NOT NULL,
	booking_time timestamp NOT NULL,
	payment_time timestamp
	CHECK (NOT is_paid_for OR payment_time IS NOT NULL)
	CHECK (bonus_card_used IS NULL OR bonus_amount_used IS NOT NULL)
);