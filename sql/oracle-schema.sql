CREATE TABLE customer
  (customer_id NUMBER PRIMARY KEY,
   customer_name VARCHAR2(1024)
  );
CREATE TABLE account
  (account_id VARCHAR2(256) PRIMARY KEY,
   balance NUMBER,
   account_type VARCHAR2(256),
   customer_id NUMBER,
   FOREIGN KEY (customer_id)
   REFERENCES customer(customer_id)
  );
CREATE TABLE transaction
  (transaction_id NUMBER PRIMARY KEY,
   amount NUMBER,
   transaction_description VARCHAR2(1024),
   transaction_timestamp DATE,
   transaction_type VARCHAR2(256),
   from_account VARCHAR2(256),
   to_account VARCHAR2(256),
   FOREIGN KEY (from_account)
   REFERENCES account(account_id),
   FOREIGN KEY (to_account)
   REFERENCES account(account_id)
  );
