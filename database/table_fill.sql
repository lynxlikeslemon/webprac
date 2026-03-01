INSERT INTO company(company_id, company_name) VALUES
	(1, 'YeetusAir'),
	(2, 'Oil Trick Airways'),
	(3, 'PigsFly'),
	(4, 'NESDuckTailsMoonTheme Airlines');

INSERT INTO airport(airport_id, airport_name, city) VALUES
	('SVO', 'Шереметьево', 'Москва'),
	('VKO', 'Внуково', 'Москва'),
	('LED', 'Пулково', 'Санкт-Петербург'),
	('OVB', 'Толмачёво', 'Новосибирск');

INSERT INTO flight(
	flight_id, 
	company_id, 
	departure_airport_id,
	arrival_airport_id,
	departure_time,
	arrival_time,
	price,
	places_total,
	places_taken
) VALUES 
	('AB‑1111', 1, 'SVO', 'LED', '01/06/26 10:00:00', '01/06/26 11:30:00', 5000, 150, 2),
	('CD-2222', 2, 'VKO', 'OVB', '03/06/26 22:10:00', '04/06/26 2:30:00', 8000, 200, 1),
	('EF-3333', 4, 'LED', 'OVB', '05/06/26 13:00:00', '05/06/26 17:30:00', 10000, 100, 0),
	('GH-4444', 3, 'OVB', 'VKO', '04/06/26 15:30:00', '04/06/26 20:00:00', 6000, 111, 1),
	('IJ-5555', 1, 'LED', 'VKO', '02/06/26 11:30:00', '02/06/26 13:00:00', 3000, 222, 1);

INSERT INTO client(
	client_id,
	last_name,
	first_name,
	fathers_name,
	phone_number,
	email,
	address
) VALUES
	(1, 'Иванов', 'Иван', 'Иванович', '+7(777)777-77-77', 'ivanov@ivan.ov', 'г. Москва, ул. Ильинка, 4, 109012'),
	(2, 'Сергеев', 'Сергей', 'Сергеевич', '+7(123)456-78-90', NULL, NULL),
	(3, 'Петров', 'Пётр', 'Петрович', '+7(800)555-35-35', NULL, 'г. Москва, ул. Шухова, 10, стр. 1'),
	(4, 'Михайлов', 'Михаил', 'Михайлович', '+7(000)000-00-00', 'mail@ma.il', NULL);
	

INSERT INTO bonus_card(bonus_id, company_id, client_id, amount) VALUES
	(42, 1, 1, 4200),
	(666, 3, 1, 600),
	(0, 2, 3, 0),
	(12, 3, 4, 5678);
	

INSERT INTO ticket(
	ticket_id,
	flight_id,
	client_id,
	price,
	bonus_card_used,
	bonus_amount_used,
	is_paid_for,
	booking_time,
	payment_time
) VALUES
	(1, 'AB‑1111', 1, 5000, 42, 2000, 'true', '23/02/26 10:00:00', '23/02/26 10:05:00'),
	(2, 'IJ-5555', 1, 3000, NULL, NULL, 'false', '23/02/26 11:00:00', NULL),
	(3, 'AB‑1111', 2, 5000, NULL, NULL, 'true', '23/02/26 12:00:00', '23/02/26 13:00:00'),
	(4, 'CD-2222', 3, 8000, NULL, NULL, 'false', '23/02/26 13:00:00', NULL),
	(5, 'GH-4444', 4, 6000, 12, 1000, 'true', '23/02/26 14:00:00', '23/02/26 15:00:00');

	