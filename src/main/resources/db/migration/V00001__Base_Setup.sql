CREATE TABLE ds_user (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    FULL_NAME VARCHAR(500),
    EMAIL VARCHAR(255),
    RESET_PASSWORD_TOKEN VARCHAR(500),
    ACCOUNT_VERIFICATION_TOKEN VARCHAR(500),
    ACCOUNT_VERIFIED BOOLEAN,
    PASSWORD_HASH VARCHAR(500)
);

CREATE TABLE ds_user_role (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    USER_ID INT,
    USER_ROLE VARCHAR(255),
    FOREIGN KEY (USER_ID) REFERENCES ds_user(ID)
);

CREATE TABLE ds_user_active_session (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    USER_ID INT,
    TOKEN VARCHAR(750),
    VALIDITY TIMESTAMP,
    FOREIGN KEY (USER_ID) REFERENCES ds_user(ID)
);

CREATE TABLE ds_address (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TYPE VARCHAR(10),
    LINE_ONE VARCHAR(100),
    LINE_TWO VARCHAR(100),
    TOWN_OR_CITY VARCHAR(50),
    STATE VARCHAR(50),
    COUNTRY VARCHAR(50),
    PIN_CODE VARCHAR(10)
);

CREATE TABLE ds_person (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(500),
    EMAIL VARCHAR(255),
    MOBILE_NUMBER VARCHAR(15),
    DATE_OF_BIRTH DATE,
    COUNTRY VARCHAR(50)
);

CREATE TABLE ds_person_address (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    PERSON_ID INT,
    ADDRESS_ID INT,
    FOREIGN KEY (PERSON_ID) REFERENCES ds_person(ID),
    FOREIGN KEY (ADDRESS_ID) REFERENCES ds_address(ID)
);

CREATE TABLE ds_organization (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NAME VARCHAR(500),
    EMAIL VARCHAR(255),
    MOBILE_NUMBER VARCHAR(15),
    CONTACT_NUMBER VARCHAR(15),
    COUNTRY VARCHAR(50)
);

CREATE TABLE ds_organization_address (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ORGANIZATION_ID INT,
    ADDRESS_ID INT,
    TYPE VARCHAR(10),
    FOREIGN KEY (ORGANIZATION_ID) REFERENCES ds_organization(ID),
    FOREIGN KEY (ADDRESS_ID) REFERENCES ds_address(ID)
);

/*  References ds_person,  id = ds_person.id */
CREATE TABLE ds_director (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ORGANIZATION_ID INT,
    STATUS VARCHAR(10),
    FOREIGN KEY (ORGANIZATION_ID) REFERENCES ds_organization(ID)
);

/*  References ds_person,  id = ds_person.id */
CREATE TABLE ds_pilot (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    RESOURCE_OWNER_ID INT,
    STATUS VARCHAR(10),
    DRONE_CATEGORY VARCHAR(20),
    TRAINING_CERTIFICATE_DOC_NAME VARCHAR(200)
);

/*  References ds_person,  id = ds_person.id */
CREATE TABLE ds_individual_operator (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    RESOURCE_OWNER_ID INT,
    STATUS VARCHAR(10)
);


/*  References ds_organization,  id = ds_organization.id */
CREATE TABLE ds_organization_operator (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    RESOURCE_OWNER_ID INT,
    STATUS VARCHAR(10)
);

/*  References ds_user,  id = ds_user.id */
CREATE TABLE ds_drone_type (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    CREATED_BY_ID INT,
    CREATED_DATE DATE,
    LAST_MODIFIED_BY_ID INT,
    LAST_MODIFIED_DATE DATE,
    MODEL_NAME VARCHAR(100),
    MANUFACTURER VARCHAR(500),
    MANUFACTURER_ADDRESS_TYPE VARCHAR(50),
    MANUFACTURER_ADDRESS_LINE_ONE VARCHAR(100),
    MANUFACTURER_ADDRESS_LINE_TWO VARCHAR(100),
    MANUFACTURER_ADDRESS_TOWN_OR_CITY VARCHAR(50),
    MANUFACTURER_ADDRESS_STATE VARCHAR(50),
    MANUFACTURER_ADDRESS_COUNTRY VARCHAR(50),
    MANUFACTURER_ADDRESS_PIN_CODE VARCHAR(10),
    MANUFACTURER_NATIONALITY VARCHAR(50),
    MODEL_NO VARCHAR(50),
    SERIAL_NO VARCHAR(50),
    DATE_OF_MANUFACTURE DATE,
    WING_TYPE VARCHAR(50),
    MAX_TAKE_OFF_WEIGHT FLOAT(8,3),
    MAX_HEIGHT_ATTAINABLE FLOAT(8,3),
    DRONE_CATEGORY_TYPE ENUM('MICRO', 'SMALL', 'MEDIUM', 'LARGE'),
    COMPATIBLE_PAYLOAD VARCHAR(500),
    PURPOSE_OF_OPERATION VARCHAR(100),
    PROPOSED_BASE_OF_OPERATION VARCHAR(100),
    ENGINE_TYPE VARCHAR(50),
    ENGINE_POWER FLOAT(8,3),
    ENGINE_COUNT INT,
    FUEL_CAPACITY FLOAT(8,2),
    PROPELLER_DETAILS VARCHAR(500),
    LENGTH_OF_DRONE FLOAT(8,3),
    BREADTH_OF_DRONE FLOAT(8,3),
    HEIGHT_OF_DRONE FLOAT(8,3),
    MAX_ENDURANCE INT,
    MAX_RANGE FLOAT(8,3),
    MAX_SPEED FLOAT(7,2),
    MAX_HEIGHT_OF_OPERATION FLOAT(7,2),
    HAS_GNSS TINYINT(1),
    HAS_AUTONOMOUS_FLIGHT_TERMINATION_SYSTEM TINYINT(1),
    HAS_FLASHING_COLLISION_STROBE_LIGHTS TINYINT(1),
    HAS_RFID_GSM_SIM_CARD TINYINT(1),
    HAS_FLIGHT_CONTROLLER TINYINT(1),
    OP_MANUAL_DOC_NAME VARCHAR(50),
    MAINTENANCE_GUIDELINES_DOC_NAME VARCHAR(50),
    FOREIGN KEY (CREATED_BY_ID) REFERENCES ds_user(ID),
    FOREIGN KEY (LAST_MODIFIED_BY_ID) REFERENCES ds_user(ID)
);

/*  References ds_drone_type,  id = ds_drone_type.id */
CREATE TABLE ds_operator_drone (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    DRONE_TYPE_ID INT,
    OPERATOR_ID INT,
    OPERATOR_TYPE ENUM('INDIVIDUAL','ORGANISATION','GOVERNMENT'),
    ACQUISITION_APPLICATION_ID VARCHAR(30),
    IS_IMPORTED TINYINT(1),
    UIN_APPLICATION_ID VARCHAR(30),
    REGISTERED_DATE DATE,
    OPERATOR_DRONE_STATUS ENUM('UIN_NOT_APPLIED','UIN_DRAFT','UIN_SUBMITTED','UIN_APPROVED','UIN_REJECTED'),
    FOREIGN KEY (DRONE_TYPE_ID) REFERENCES ds_drone_type(ID)
);

CREATE TABLE ds_blog (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TITLE VARCHAR(200),
    CONTENT MEDIUMTEXT,
    CREATED_BY_ID INT,
    UPDATED_BY_ID INT,
    CREATED_TIMESTAMP TIMESTAMP,
    UPDATED_TIMESTAMP TIMESTAMP
);


