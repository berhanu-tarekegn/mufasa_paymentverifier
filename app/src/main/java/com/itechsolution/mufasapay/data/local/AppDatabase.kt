package com.itechsolution.mufasapay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.itechsolution.mufasapay.data.local.dao.DeliveryLogDao
import com.itechsolution.mufasapay.data.local.dao.SenderDao
import com.itechsolution.mufasapay.data.local.dao.SenderTemplateDao
import com.itechsolution.mufasapay.data.local.dao.SmsMessageDao
import com.itechsolution.mufasapay.data.local.dao.WebhookConfigDao
import com.itechsolution.mufasapay.data.local.entity.DeliveryLogEntity
import com.itechsolution.mufasapay.data.local.entity.SenderEntity
import com.itechsolution.mufasapay.data.local.entity.SenderTemplateEntity
import com.itechsolution.mufasapay.data.local.entity.SmsMessageEntity
import com.itechsolution.mufasapay.data.local.entity.WebhookConfigEntity

@Database(
    entities = [
        SmsMessageEntity::class,
        SenderEntity::class,
        WebhookConfigEntity::class,
        DeliveryLogEntity::class,
        SenderTemplateEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun senderDao(): SenderDao
    abstract fun senderTemplateDao(): SenderTemplateDao
    abstract fun webhookConfigDao(): WebhookConfigDao
    abstract fun deliveryLogDao(): DeliveryLogDao

    companion object {
        /**
         * Migration from v1 to v2:
         * 1. Create the sender_templates table
         * 2. Migrate existing pattern data from senders into sender_templates
         * 3. Remove the pattern column from senders (via table rebuild)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create sender_templates table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sender_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `label` TEXT NOT NULL,
                        `pattern` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`senderId`) REFERENCES `senders`(`senderId`) ON DELETE CASCADE
                    )
                """)

                // 2. Create index
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS `idx_sender_templates_sender_id` ON `sender_templates` (`senderId`)
                """)

                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS `idx_sms_unique_content` ON `sms_messages` (`sender`, `message`, `timestamp`)
                """)

                // 3. Migrate existing patterns from senders to sender_templates
                db.execSQL("""
                    INSERT INTO `sender_templates` (`senderId`, `label`, `pattern`, `isEnabled`, `createdAt`)
                    SELECT `senderId`, 'Default', `pattern`, 1, `addedAt`
                    FROM `senders`
                    WHERE `pattern` IS NOT NULL AND `pattern` != ''
                """)

                // 4. Remove pattern column from senders (SQLite requires table rebuild)
                db.execSQL("""
                    CREATE TABLE `senders_new` (
                        `senderId` TEXT NOT NULL PRIMARY KEY,
                        `displayName` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `addedAt` INTEGER NOT NULL,
                        `lastMessageAt` INTEGER,
                        `messageCount` INTEGER NOT NULL DEFAULT 0
                    )
                """)

                db.execSQL("""
                    INSERT INTO `senders_new` (`senderId`, `displayName`, `isEnabled`, `addedAt`, `lastMessageAt`, `messageCount`)
                    SELECT `senderId`, `displayName`, `isEnabled`, `addedAt`, `lastMessageAt`, `messageCount`
                    FROM `senders`
                """)

                db.execSQL("DROP TABLE `senders`")
                db.execSQL("ALTER TABLE `senders_new` RENAME TO `senders`")

                // 5. Recreate index on senders
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS `idx_senders_enabled` ON `senders` (`isEnabled`)
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `sms_messages` ADD COLUMN `amount` REAL")
                db.execSQL("ALTER TABLE `sms_messages` ADD COLUMN `transactionId` TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `webhook_config_new` (
                        `id` INTEGER NOT NULL,
                        `uploadUrl` TEXT NOT NULL,
                        `deleteUrlTemplate` TEXT NOT NULL,
                        `method` TEXT NOT NULL,
                        `headers` TEXT NOT NULL,
                        `authType` TEXT NOT NULL,
                        `authValue` TEXT,
                        `timeout` INTEGER NOT NULL,
                        `retryEnabled` INTEGER NOT NULL,
                        `maxRetries` INTEGER NOT NULL,
                        `retryDelayMs` INTEGER NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """
                )

                db.execSQL(
                    """
                    INSERT INTO `webhook_config_new` (
                        `id`,
                        `uploadUrl`,
                        `deleteUrlTemplate`,
                        `method`,
                        `headers`,
                        `authType`,
                        `authValue`,
                        `timeout`,
                        `retryEnabled`,
                        `maxRetries`,
                        `retryDelayMs`,
                        `isEnabled`,
                        `updatedAt`
                    )
                    SELECT
                        `id`,
                        CASE
                            WHEN TRIM(`url`) = '' THEN ''
                            WHEN RTRIM(`url`, '/') LIKE '%/v1/transactions' THEN RTRIM(`url`, '/')
                            ELSE RTRIM(`url`, '/') || '/v1/transactions'
                        END,
                        CASE
                            WHEN TRIM(`url`) = '' THEN ''
                            WHEN RTRIM(`url`, '/') LIKE '%/v1/transactions' THEN RTRIM(`url`, '/') || '/{transaction_id}'
                            ELSE RTRIM(`url`, '/') || '/v1/transactions/{transaction_id}'
                        END,
                        `method`,
                        `headers`,
                        `authType`,
                        `authValue`,
                        `timeout`,
                        `retryEnabled`,
                        `maxRetries`,
                        `retryDelayMs`,
                        `isEnabled`,
                        `updatedAt`
                    FROM `webhook_config`
                    """
                )

                db.execSQL("DROP TABLE `webhook_config`")
                db.execSQL("ALTER TABLE `webhook_config_new` RENAME TO `webhook_config`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `webhook_config_new` (
                        `id` INTEGER NOT NULL,
                        `uploadUrl` TEXT NOT NULL,
                        `deleteUrlTemplate` TEXT NOT NULL,
                        `headers` TEXT NOT NULL,
                        `authType` TEXT NOT NULL,
                        `authValue` TEXT,
                        `timeout` INTEGER NOT NULL,
                        `retryEnabled` INTEGER NOT NULL,
                        `maxRetries` INTEGER NOT NULL,
                        `retryDelayMs` INTEGER NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """
                )

                db.execSQL(
                    """
                    INSERT INTO `webhook_config_new` (
                        `id`,
                        `uploadUrl`,
                        `deleteUrlTemplate`,
                        `headers`,
                        `authType`,
                        `authValue`,
                        `timeout`,
                        `retryEnabled`,
                        `maxRetries`,
                        `retryDelayMs`,
                        `isEnabled`,
                        `updatedAt`
                    )
                    SELECT
                        `id`,
                        `uploadUrl`,
                        `deleteUrlTemplate`,
                        `headers`,
                        `authType`,
                        `authValue`,
                        `timeout`,
                        `retryEnabled`,
                        `maxRetries`,
                        `retryDelayMs`,
                        `isEnabled`,
                        `updatedAt`
                    FROM `webhook_config`
                    """
                )

                db.execSQL("DROP TABLE `webhook_config`")
                db.execSQL("ALTER TABLE `webhook_config_new` RENAME TO `webhook_config`")
            }
        }
    }
}
