#!/usr/bin/env python3
"""
سكريبت استخراج البيانات الصيدلانية للتكامل مع PostgreSQL
Pharmaceutical data extraction script for PostgreSQL integration

Usage: python3 extract_with_postgresql.py <excel_file_path> [database_config]
Output: JSON array to stdout

يستخدم الجداول الموجودة:
- manufacturers (Manufacturer entity)
- forms (Form entity)
- master_product (MasterProduct entity)

Compatible with Docker Compose setup
"""

import pandas as pd
import json
import random
import string
import sys
import os
import logging
import psycopg2
import sqlite3
from typing import Dict, Optional, Any

# Configure logging to stderr to avoid interfering with JSON output
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)

class DatabaseManager:
    """Database manager for existing pharmaceutical database (PostgreSQL support)"""

    def __init__(self, db_config: Dict[str, Any]):
        self.db_config = db_config
        self.connection = None
        self.form_mapping = {}
        self.manufacturer_mapping = {}

    def connect(self):
        """Connect to database"""
        try:
            if self.db_config.get('type') == 'postgresql':
                self.connection = psycopg2.connect(
                    host=self.db_config.get('host', 'localhost'),
                    user=self.db_config.get('username'),
                    password=self.db_config.get('password'),
                    database=self.db_config.get('database'),
                    port=self.db_config.get('port', 5432)
                )
                self.connection.autocommit = True
            elif self.db_config.get('type') == 'mysql':
                import mysql.connector
                self.connection = mysql.connector.connect(
                    host=self.db_config.get('host', 'localhost'),
                    user=self.db_config.get('username'),
                    password=self.db_config.get('password'),
                    database=self.db_config.get('database'),
                    charset='utf8mb4'
                )
            else:
                # Default to SQLite for testing
                self.connection = sqlite3.connect(self.db_config.get('path', 'pharmaceutical.db'))

            logging.info("تم الاتصال بقاعدة البيانات / Database connected successfully")
            return True

        except Exception as e:
            logging.error(f"خطأ في الاتصال بقاعدة البيانات / Database connection error: {e}")
            return False

    def load_forms_mapping(self):
        """Load forms mapping from existing forms table"""
        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT id, name FROM forms")
            forms = cursor.fetchall()

            self.form_mapping = {}
            for form_id, name in forms:
                if name:
                    self.form_mapping[name.strip()] = form_id

            logging.info(f"تم تحميل {len(self.form_mapping)} شكل صيدلاني / Loaded {len(self.form_mapping)} pharmaceutical forms")
            cursor.close()

        except Exception as e:
            logging.error(f"خطأ في تحميل الأشكال الصيدلانية / Error loading forms: {e}")
            self.form_mapping = {}

    def load_manufacturers_mapping(self):
        """Load manufacturers mapping from existing manufacturers table"""
        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT id, name FROM manufacturers")
            manufacturers = cursor.fetchall()

            self.manufacturer_mapping = {}
            for manufacturer_id, name in manufacturers:
                if name:
                    self.manufacturer_mapping[name.strip()] = manufacturer_id

            logging.info(f"تم تحميل {len(self.manufacturer_mapping)} مصنع / Loaded {len(self.manufacturer_mapping)} manufacturers")
            cursor.close()

        except Exception as e:
            logging.error(f"خطأ في تحميل المصنعين / Error loading manufacturers: {e}")
            self.manufacturer_mapping = {}

    def get_form_id(self, form_name: str) -> int:
        """Get form ID by name, create if not exists"""
        if not form_name or not form_name.strip():
              return None  # Return None instead of default fallback

        form_name = form_name.strip()

        # Check existing mapping
        if form_name in self.form_mapping:
            return self.form_mapping[form_name]

        # Create new form if not exists
        try:
            cursor = self.connection.cursor()

            if self.db_config.get('type') == 'postgresql':
                cursor.execute("INSERT INTO forms (name) VALUES (%s) ON CONFLICT (name) DO NOTHING", (form_name,))
                cursor.execute("SELECT id FROM forms WHERE name = %s", (form_name,))
            elif self.db_config.get('type') == 'mysql':
                cursor.execute("INSERT IGNORE INTO forms (name) VALUES (%s)", (form_name,))
                cursor.execute("SELECT id FROM forms WHERE name = %s", (form_name,))
            else:
                cursor.execute("INSERT OR IGNORE INTO forms (name) VALUES (?)", (form_name,))
                cursor.execute("SELECT id FROM forms WHERE name = ?", (form_name,))

            result = cursor.fetchone()
            if result:
                form_id = result[0]
                self.form_mapping[form_name] = form_id
                if self.db_config.get('type') != 'postgresql':  # PostgreSQL autocommit is enabled
                    self.connection.commit()
                logging.info(f"تم إنشاء شكل صيدلاني جديد / Created new form: {form_name} (ID: {form_id})")
                cursor.close()
                return form_id

            cursor.close()

        except Exception as e:
            logging.error(f"خطأ في إنشاء الشكل الصيدلاني / Error creating form {form_name}: {e}")
            return None  # Return None instead of default fallback

    def get_manufacturer_id(self, manufacturer_name: str) -> int:
        """Get manufacturer ID by name, create if not exists"""
        if not manufacturer_name or not manufacturer_name.strip():
            return None  # Return None instead of default fallback

        manufacturer_name = manufacturer_name.strip()

        # Check existing mapping
        if manufacturer_name in self.manufacturer_mapping:
            return self.manufacturer_mapping[manufacturer_name]

        # Create new manufacturer if not exists
        try:
            cursor = self.connection.cursor()

            if self.db_config.get('type') == 'postgresql':
                cursor.execute("INSERT INTO manufacturers (name) VALUES (%s) ON CONFLICT (name) DO NOTHING", (manufacturer_name,))
                cursor.execute("SELECT id FROM manufacturers WHERE name = %s", (manufacturer_name,))
            elif self.db_config.get('type') == 'mysql':
                cursor.execute("INSERT IGNORE INTO manufacturers (name) VALUES (%s)", (manufacturer_name,))
                cursor.execute("SELECT id FROM manufacturers WHERE name = %s", (manufacturer_name,))
            else:
                cursor.execute("INSERT OR IGNORE INTO manufacturers (name) VALUES (?)", (manufacturer_name,))
                cursor.execute("SELECT id FROM manufacturers WHERE name = ?", (manufacturer_name,))

            result = cursor.fetchone()
            if result:
                manufacturer_id = result[0]
                self.manufacturer_mapping[manufacturer_name] = manufacturer_id
                if self.db_config.get('type') != 'postgresql':  # PostgreSQL autocommit is enabled
                    self.connection.commit()
                logging.info(f"تم إنشاء مصنع جديد / Created new manufacturer: {manufacturer_name} (ID: {manufacturer_id})")
                cursor.close()
                return manufacturer_id

            cursor.close()

        except Exception as e:
            logging.error(f"خطأ في إنشاء المصنع / Error creating manufacturer {manufacturer_name}: {e}")
            return None  # Return None instead of default fallback

    def close(self):
        """Close database connection"""
        if self.connection:
            self.connection.close()
            logging.info("تم إغلاق الاتصال بقاعدة البيانات / Database connection closed")

def generate_random_barcode():
    """Generate a random 13-digit barcode"""
    return ''.join(random.choices(string.digits, k=13))

def calculate_purchase_price(selling_price, tax_rate=15.0):
    """Calculate purchase price from selling price (selling_price - tax%)"""
    if pd.isna(selling_price) or selling_price == 0:
        return 0.0
    return round(selling_price * (1 - tax_rate / 100), 2)

def clean_text(text):
    """Clean and normalize text"""
    if pd.isna(text):
        return ""
    return str(text).strip()

def get_common_translations():
    """Get common pharmaceutical translations"""
    return {
        'OMEPRAZOLE': 'أوميبرازول',
        'ACETAZOLAMIDE': 'أسيتازولاميد',
        'AMOXICILLIN': 'أموكسيسيلين',
        'PARACETAMOL': 'باراسيتامول',
        'IBUPROFEN': 'إيبوبروفين',
        'ASPIRIN': 'أسبرين',
        'METFORMIN': 'ميتفورمين',
        'INSULIN': 'إنسولين',
        'WARFARIN': 'وارفارين',
        'DIGOXIN': 'ديجوكسين',
        'SODIUM VALPROATE': 'فالبروات الصوديوم',
        'ERYTHROMYCIN': 'إريثروميسين',
        'ASCORBIC ACID': 'حمض الأسكوربيك',
        'CEFUROXIME': 'سيفوروكسيم',
        'ORLISTAT': 'أورليستات',
        'DILTIAZEM HYDROCHLORIDE': 'ديلتيازيم هيدروكلوريد'
    }

def translate_to_arabic(text):
    """Simple translation function"""
    translations = get_common_translations()
    text_upper = text.upper().strip()
    return translations.get(text_upper, text)

def get_default_db_config():
    """Get default database configuration"""
    return {
        'type': 'postgresql',
        'host': 'localhost',
        'port': 5432,
        'username': 'postgres',
        'password': 'password',
        'database': 'Uqar'
    }

def parse_db_config(config_str: Optional[str]) -> Dict[str, Any]:
    """Parse database configuration from string"""
    if not config_str:
        return get_default_db_config()

    try:
        # Expected format: postgresql://username:password@host:port/database
        # or mysql://username:password@host:port/database
        # or sqlite:///path/to/database.db

        if config_str.startswith('postgresql://'):
            # Parse PostgreSQL connection string
            parts = config_str.replace('postgresql://', '').split('/')
            auth_host = parts[0]
            database = parts[1] if len(parts) > 1 else 'Uqar'

            if '@' in auth_host:
                auth, host_port = auth_host.split('@')
                username, password = auth.split(':')
                host = host_port.split(':')[0] if ':' in host_port else host_port
                port = int(host_port.split(':')[1]) if ':' in host_port else 5432
            else:
                host = auth_host
                username = 'postgres'
                password = 'password'
                port = 5432

            return {
                'type': 'postgresql',
                'host': host,
                'port': port,
                'username': username,
                'password': password,
                'database': database
            }

        elif config_str.startswith('mysql://'):
            # Parse MySQL connection string
            parts = config_str.replace('mysql://', '').split('/')
            auth_host = parts[0]
            database = parts[1] if len(parts) > 1 else 'pharmaceutical_db'

            if '@' in auth_host:
                auth, host_port = auth_host.split('@')
                username, password = auth.split(':')
                host = host_port.split(':')[0] if ':' in host_port else host_port
                port = int(host_port.split(':')[1]) if ':' in host_port else 3306
            else:
                host = auth_host
                username = 'root'
                password = ''
                port = 3306

            return {
                'type': 'mysql',
                'host': host,
                'port': port,
                'username': username,
                'password': password,
                'database': database
            }

        elif config_str.startswith('sqlite://'):
            return {
                'type': 'sqlite',
                'path': config_str.replace('sqlite://', '')
            }

        else:
            # Assume it's a SQLite path
            return {
                'type': 'sqlite',
                'path': config_str
            }

    except Exception as e:
        logging.error(f"خطأ في تحليل إعدادات قاعدة البيانات / Error parsing database config: {e}")
        return get_default_db_config()

def extract_pharmaceutical_data(excel_file_path: str, db_config: Dict[str, Any]):
    """Extract pharmaceutical data from Excel using existing database"""

    # Initialize database manager
    db_manager = DatabaseManager(db_config)

    if not db_manager.connect():
        logging.error("فشل في الاتصال بقاعدة البيانات / Failed to connect to database")
        return None

    try:
        # Load existing mappings
        db_manager.load_forms_mapping()
        db_manager.load_manufacturers_mapping()

        logging.info(f"معالجة الملف / Processing file: {excel_file_path}")

        # Read the Excel file
        df = pd.read_excel(excel_file_path)

        logging.info(f"معالجة {len(df)} سجل / Processing {len(df)} records...")

        # Prepare the output data
        pharmaceutical_data = []

        # Validate required columns
        required_columns = ['الاسم التجاري', 'التركيب ', 'العيار', ' العبوة', 'المعمل', ' الشكل الصيدلاني', 'السعر للعموم', 'السعر للصيدلاني']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            logging.error(f"Missing required columns: {missing_columns}")
            return None

        # Process first 100 rows only for testing
        max_rows = min(100, len(df))
        logging.info(f"Processing first {max_rows} rows out of {len(df)} total rows")
        
        processed_count = 0
        logging.info(f"Starting to process {max_rows} records...")
        for index, row in df.head(max_rows).iterrows():
            processed_count += 1
            if processed_count % 10 == 0:  # Log every 10 records
                logging.info(f"Processed {processed_count}/{max_rows} records")
            try:
                # Extract basic information with safe access
                trade_name = clean_text(row.get('الاسم التجاري', ''))
                scientific_name = clean_text(row.get('التركيب ', ''))
                concentration = clean_text(row.get('العيار', ''))
                size = clean_text(row.get(' العبوة', ''))
                manufacturer = clean_text(row.get('المعمل', ''))
                form = clean_text(row.get(' الشكل الصيدلاني', ''))

                # Skip empty rows
                if not trade_name and not scientific_name:
                    continue

                # Handle prices with safe access
                selling_price = row.get('السعر للعموم', 0.0) if not pd.isna(row.get('السعر للعموم', 0.0)) else 0.0
                purchase_price = row.get('السعر للصيدلاني', 0.0) if not pd.isna(row.get('السعر للصيدلاني', 0.0)) else calculate_purchase_price(selling_price)

                # Get form and manufacturer IDs from existing database
                try:
                    form_id = db_manager.get_form_id(form)
                    if form_id is None:
                        form_id = 1  # Default form ID
                        logging.warning(f"Using default form ID 1 for: {form}")
                except Exception as e:
                    form_id = 1  # Default form ID
                    logging.warning(f"Error getting form ID for {form}: {e}, using default form ID 1")
                
                try:
                    manufacturer_id = db_manager.get_manufacturer_id(manufacturer)
                    if manufacturer_id is None:
                        manufacturer_id = 1  # Default manufacturer ID
                        logging.warning(f"Using default manufacturer ID 1 for: {manufacturer}")
                except Exception as e:
                    manufacturer_id = 1  # Default manufacturer ID
                    logging.warning(f"Error getting manufacturer ID for {manufacturer}: {e}, using default manufacturer ID 1")

                # Create the pharmaceutical record for master_product table
                pharmaceutical_record = {
                    "tradeName": trade_name,
                    "scientificName": scientific_name,
                    "concentration": concentration,
                    "size": size,
                    "refPurchasePrice": float(purchase_price),
                    "refSellingPrice": float(selling_price),
                    "notes": f"دواء من إنتاج {manufacturer} - {form}",
                    "tax": 15.0,
                    "barcode": generate_random_barcode(),
                    "requiresPrescription": False,  # Default to False, can be updated later
                    "typeId": 1,  # Default pharmaceutical type ID
                    "formId": form_id,
                    "manufacturerId": manufacturer_id,
                    "categoryIds": [1],  # Default pharmaceutical category
                    "translations": [
                        {
                            "tradeName": translate_to_arabic(trade_name),
                            "scientificName": translate_to_arabic(scientific_name),
                            "lang": "ar"
                        }
                    ]
                }

                pharmaceutical_data.append(pharmaceutical_record)

            except Exception as e:
                logging.error(f"خطأ في معالجة السجل {index} / Error processing row {index}: {e}")
                continue

        logging.info(f"تم استخراج {len(pharmaceutical_data)} سجل بنجاح / Successfully extracted {len(pharmaceutical_data)} records")
        logging.info(f"Finished processing {processed_count} rows")
        return pharmaceutical_data

    except Exception as e:
        logging.error(f"خطأ في معالجة الملف / Error processing file: {e}")
        return None

    finally:
        db_manager.close()

def main():
    """Main function"""
    if len(sys.argv) < 2:
        logging.error("Usage: python3 extract_for_spring_boot.py <excel_file_path> [database_config]")
        logging.error("Database config examples:")
        logging.error("  postgresql://username:password@localhost:5432/Uqar")
        logging.error("  postgresql://postgres:password@postgres:5432/Uqar (Docker)")
        logging.error("  mysql://username:password@localhost:3306/pharmaceutical_db")
        logging.error("  sqlite:///path/to/database.db")
        sys.exit(1)

    excel_file_path = sys.argv[1]
    db_config_str = sys.argv[2] if len(sys.argv) > 2 else None

    # Check if file exists
    if not os.path.exists(excel_file_path):
        logging.error(f"الملف غير موجود / File not found: {excel_file_path}")
        sys.exit(1)

    # Parse database configuration
    db_config = parse_db_config(db_config_str)
    logging.info(f"استخدام قاعدة البيانات / Using database: {db_config['type']}")

    # Extract data
    data = extract_pharmaceutical_data(excel_file_path, db_config)

    if data is None:
        logging.error("فشل في استخراج البيانات / Failed to extract data")
        sys.exit(1)

    # Output JSON to stdout
    try:
        json_output = json.dumps(data, ensure_ascii=False, separators=(',', ':'))
        print(json_output)
        sys.exit(0)
    except Exception as e:
        logging.error(f"خطأ في إنتاج JSON / Error generating JSON output: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

