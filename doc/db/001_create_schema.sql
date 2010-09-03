create schema mailer;

CREATE SEQUENCE mailer.message_seq start 10000;

CREATE TABLE mailer.message (
    message_id int8 NOT NULL,
    sender character varying(80) NOT NULL,
    recipients character varying(200) NOT NULL,
    subject character varying(200) NOT NULL,
    body text NOT NULL,
    sent_date timestamp without time zone,
    failure_count integer DEFAULT 0 NOT NULL,
    create_ts timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT message_failure_count_check CHECK (failure_count >= 0),
    CONSTRAINT message_pkey PRIMARY KEY (message_id)
);

