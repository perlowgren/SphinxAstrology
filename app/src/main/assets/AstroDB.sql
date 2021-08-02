
/* Contains version of database.
Only one row should be inserted. */
CREATE TABLE Database (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  version INTEGER,
  counter INTEGER,
  flags INTEGER,
  updated INTEGER,
  created INTEGER
);

/* First row is by default local user. */
CREATE TABLE User (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  userKey INTEGER,
  email TEXT,
  user TEXT,
  name TEXT,
  language TEXT,
  picture TEXT,
  flags INTEGER,
  updated INTEGER,
  created INTEGER
);
CREATE INDEX User_UserKey ON User (userKey);
CREATE INDEX User_Email ON User (email);

CREATE TABLE Profile (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  userId INTEGER,
  profileKey INTEGER,
  cat1 INTEGER,
  cat2 INTEGER,
  name TEXT,
  year INTEGER,
  month INTEGER,
  day INTEGER,
  hour INTEGER,
  minute INTEGER,
  second INTEGER,
  longitude INTEGER,
  latitude INTEGER,
  timeZone INTEGER,
  dst INTEGER,
  sun INTEGER,
  moon INTEGER,
  ascendant INTEGER,
  picture TEXT,
  flags INTEGER,
  updated INTEGER,
  created INTEGER
);
CREATE INDEX Profile_UserId ON Profile (userId);
CREATE INDEX Profile_ProfileKey ON Profile (profileKey);
CREATE INDEX Profile_Categories ON Profile (cat1,cat2);
CREATE INDEX Profile_Name ON Profile (name);

CREATE TABLE Location (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT,
  locality TEXT,
  country TEXT,
  countryCode TEXT,
  longitude INTEGER,
  latitude INTEGER,
  language TEXT,
  flags INTEGER,
  created INTEGER
);
CREATE INDEX Location_Name ON Location (name);
CREATE INDEX Location_Locality ON Location (locality);
CREATE INDEX location_Coordinates ON Location (longitude,latitude);

CREATE TABLE TimeZone (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT,
  longitude INTEGER,
  latitude INTEGER,
  timestamp INTEGER,
  offset INTEGER,
  dst INTEGER,
  language TEXT,
  flags INTEGER,
  created INTEGER
);
CREATE INDEX TimeZone_Name ON TimeZone (name,timestamp,language);
CREATE INDEX TimeZone_Coordinates ON TimeZone (longitude,latitude,timestamp,language);

CREATE TABLE Text (
  _id INTEGER PRIMARY KEY AUTOINCREMENT,
  userId INTEGER,
  textKey INTEGER,
  type INTEGER,
  profileKey INTEGER,
  symbol INTEGER,
  title TEXT,
  html BLOB,
  text BLOB,
  writer TEXT,
  votes INTEGER,
  rates INTEGER,
  language TEXT,
  flags INTEGER,
  updated INTEGER,
  created INTEGER
);
CREATE INDEX Text_UserId ON Text (userId);
CREATE INDEX Text_TextKey ON Text (textKey);
CREATE INDEX Text_Symbol ON Text (type,profileKey,symbol);

