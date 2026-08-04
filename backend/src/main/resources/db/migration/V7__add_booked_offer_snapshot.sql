-- Stores a snapshot of the booked offer so order history remains unchanged
-- even if the original offer is later edited or deleted.
ALTER TABLE user_order
    ADD COLUMN booked_offer_name VARCHAR(255),
    ADD COLUMN booked_offer_price DECIMAL(10, 2);

ALTER TABLE guest_order
    ADD COLUMN booked_offer_name VARCHAR(255),
    ADD COLUMN booked_offer_price DECIMAL(10, 2);


UPDATE user_order uo
    LEFT JOIN offer o
ON o.id_offer = uo.id_offer
    LEFT JOIN payment p
    ON p.id_order = uo.id_order
    SET uo.booked_offer_name = COALESCE(
        o.kind,
        'Usunięta oferta'
        ),
        uo.booked_offer_price = COALESCE(
        p.amount,
        o.cost
        );


UPDATE guest_order go
    LEFT JOIN offer o
ON o.id_offer = go.id_offer
    LEFT JOIN payment p
    ON p.id_guest_order = go.id_guest_order
    SET go.booked_offer_name = COALESCE(
        o.kind,
        'Usunięta oferta'
        ),
        go.booked_offer_price = COALESCE(
        p.amount,
        o.cost
        );


ALTER TABLE user_order
    MODIFY COLUMN booked_offer_name VARCHAR(255) NOT NULL,
    MODIFY COLUMN booked_offer_price DECIMAL(10, 2) NOT NULL;

ALTER TABLE guest_order
    MODIFY COLUMN booked_offer_name VARCHAR(255) NOT NULL,
    MODIFY COLUMN booked_offer_price DECIMAL(10, 2) NOT NULL;