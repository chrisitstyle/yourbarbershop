INSERT INTO guest_order (
    id_guest_order,
    firstname,
    lastname,
    phonenumber,
    email,
    id_offer,
    booked_offer_name,
    booked_offer_price,
    order_date,
    visit_date,
    status
)
VALUES (
900001,
'Transaction',
'Test',
'123456789',
'transaction-test@example.com',
NULL,
'Strzyżenie testowe',
120.00,
'2026-08-03 12:00:00',
'2026-08-04 12:00:00',
'NOWE'
       );

INSERT INTO payment (
id_payment,
id_order,
id_guest_order,
payment_method,
payment_status,
stripe_checkout_session_id,
stripe_payment_intent_id,
amount,
currency,
created_at,
paid_at
)
VALUES (
900001,
NULL,
900001,
'KARTA_ONLINE',
'OCZEKUJE_NA_PLATNOSC',
NULL,
NULL,
120.00,
'PLN',
'2026-08-03 12:00:00',
NULL
);