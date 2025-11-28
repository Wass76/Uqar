-- Migration: Add next_retry_at to notification table
-- Description: Adds next_retry_at column for exponential backoff retry mechanism
-- Author: System
-- Date: 2025-01-16

-- Add next_retry_at column to notification table
ALTER TABLE notification 
ADD COLUMN IF NOT EXISTS next_retry_at TIMESTAMP;

-- Create index for efficient querying of notifications ready for retry
CREATE INDEX IF NOT EXISTS idx_notification_next_retry_at 
ON notification(status, next_retry_at) 
WHERE status = 'PENDING' AND next_retry_at IS NOT NULL;

-- Add comment
COMMENT ON COLUMN notification.next_retry_at IS 'Timestamp when the notification should be retried (for exponential backoff)';

