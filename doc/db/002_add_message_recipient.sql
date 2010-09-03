create sequence mailer.message_recipient_seq start 10000;

create table mailer.message_recipient (
	message_recipient_id int8 primary key,
	message_id int8 not null references mailer.message ( message_id ),
	email_address varchar(80) not null
);

-- simple conversion, this does not fix wrong recipients fields
insert into mailer.message_recipient 
	select nextval('mailer.message_recipient_seq'), message.message_id, message.recipients
		from mailer.message;

alter table mailer.message drop recipients;
