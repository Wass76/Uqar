-- Migration: Create notification system tables
-- Description: Creates device_token and notification tables for Firebase push notifications
-- Author: System
-- Date: 2025-01-15

-- Create sequence for device_token table
CREATE SEQUENCE IF NOT EXISTS device_token_id_seq START WITH 1 INCREMENT BY 1;

-- Create device_token table
CREATE TABLE IF NOT EXISTS device_token (
    id BIGINT PRIMARY KEY DEFAULT nextval('device_token_id_seq'),
    user_id BIGINT NOT NULL,
    device_token TEXT NOT NULL,
    device_type VARCHAR(20), -- ANDROID, IOS, WEB
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_by BIGINT,
    
    -- Indexes
    CONSTRAINT idx_device_token_user_id FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT idx_device_token_token UNIQUE (device_token)
);

-- Create indexes for device_token
CREATE INDEX IF NOT EXISTS idx_device_token_user_id ON device_token(user_id);
CREATE INDEX IF NOT EXISTS idx_device_token_is_active ON device_token(is_active);
CREATE INDEX IF NOT EXISTS idx_device_token_user_active ON device_token(user_id, is_active);

-- Create sequence for notification table
CREATE SEQUENCE IF NOT EXISTS notification_id_seq START WITH 1 INCREMENT BY 1;

-- Create notification table
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY DEFAULT nextval('notification_id_seq'),
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    notification_type VARCHAR(50), -- STOCK_LOW, DEBT_CREATED, etc.
    data JSONB, -- معلومات إضافية
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    retry_count INTEGER DEFAULT 0, -- عدد محاولات إعادة الإرسال
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    last_modified_by BIGINT,
    
    -- Foreign key
    CONSTRAINT fk_notification_user_id FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- Create indexes for notification
CREATE INDEX IF NOT EXISTS idx_notification_user_id ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_user_created ON notification(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_read_at ON notification(user_id, read_at) WHERE read_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_notification_type ON notification(notification_type);
CREATE INDEX IF NOT EXISTS idx_notification_status ON notification(status);
CREATE INDEX IF NOT EXISTS idx_notification_user_type ON notification(user_id, notification_type);

-- Add comments
COMMENT ON TABLE device_token IS 'Table for storing FCM device tokens for push notifications';
COMMENT ON TABLE notification IS 'Table for storing notification history and status';
COMMENT ON COLUMN device_token.device_type IS 'Device type: ANDROID, IOS, or WEB';
COMMENT ON COLUMN notification.notification_type IS 'Type of notification: STOCK_LOW, DEBT_CREATED, etc.';
COMMENT ON COLUMN notification.status IS 'Notification status: PENDING, SENT, or FAILED';
COMMENT ON COLUMN notification.data IS 'Additional data in JSON format';

