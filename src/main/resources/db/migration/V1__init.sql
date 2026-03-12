CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       last_login TIMESTAMP,

                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       created_by VARCHAR(255),
                       updated_by VARCHAR(255),
                       active BOOLEAN
);

CREATE TABLE customers (
                           id UUID PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255),
                           source VARCHAR(50),
                           notes TEXT,
                           assigned_to UUID,

                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL,
                           created_by VARCHAR(255),
                           updated_by VARCHAR(255),
                           active BOOLEAN,

                           CONSTRAINT fk_customer_user
                               FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE TABLE customer_users (
                                id UUID PRIMARY KEY,

                                user_id UUID NOT NULL,
                                customer_id UUID NOT NULL,

                                active BOOLEAN,

                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL,
                                created_by VARCHAR(255),
                                updated_by VARCHAR(255),

                                CONSTRAINT fk_customer_users_user
                                    FOREIGN KEY (user_id) REFERENCES users(id),

                                CONSTRAINT fk_customer_users_customer
                                    FOREIGN KEY (customer_id) REFERENCES customers(id)
);
