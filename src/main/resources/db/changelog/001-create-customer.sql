CREATE TABLE IF NOT EXISTS customer (
  id UUID PRIMARY KEY,
  last_name VARCHAR(100) NOT NULL,
  middle_name VARCHAR(100),
  first_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  mobile VARCHAR(50)
);
