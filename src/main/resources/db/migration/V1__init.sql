CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE delegation (
                            id BIGSERIAL PRIMARY KEY,
                            title VARCHAR(150) NOT NULL,
                            destination VARCHAR(150),
                            start_date DATE,
                            end_date DATE,
                            description TEXT,
                            user_id BIGINT NOT NULL,

                            CONSTRAINT fk_delegation_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users (id)
                                    ON DELETE CASCADE
);

CREATE TABLE expense (
                         id BIGSERIAL PRIMARY KEY,
                         title VARCHAR(150) NOT NULL,
                         amount NUMERIC(19, 2) NOT NULL,
                         category VARCHAR(50) NOT NULL,
                         expense_date DATE,
                         note TEXT,
                         receipt_object_name VARCHAR(500),
                         receipt_content_type VARCHAR(100),
                         delegation_id BIGINT NOT NULL,

                         CONSTRAINT fk_expense_delegation
                             FOREIGN KEY (delegation_id)
                                 REFERENCES delegation (id)
                                 ON DELETE CASCADE,

                         CONSTRAINT chk_expense_category
                             CHECK (category IN (
                                 'FOOD',
                                 'HOTEL',
                                 'TRANSPORT',
                                 'FUEL',
                                 'PARKING',
                                 'TAXI',
                                 'TICKETS',
                                 'OFFICE_SUPPLIES',
                                 'OTHER'
                                 ))
);
