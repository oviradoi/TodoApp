CREATE TABLE lists ( 
    _id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    created_date DATE    NOT NULL 
);

CREATE TABLE todos ( 
    _id          INTEGER PRIMARY KEY AUTOINCREMENT,
    text         TEXT    NOT NULL,
    listid       INTEGER NOT NULL,
    checked      BOOLEAN NOT NULL,
    created_date DATE    NOT NULL,
    done_date    DATE,
    FOREIGN KEY ( listid ) REFERENCES lists ( _id ) ON DELETE CASCADE
                                                        ON UPDATE CASCADE 
);

INSERT INTO lists(_id, name, created_date) VALUES(NULL, "Todos list", julianday("now"));