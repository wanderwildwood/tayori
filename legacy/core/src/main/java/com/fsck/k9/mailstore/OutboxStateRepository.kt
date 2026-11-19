package com.fsck.k9.mailstore

import android.content.ContentValues
import app.k9mail.core.android.common.database.getIntOrThrow
import app.k9mail.core.android.common.database.getLongOrThrow
import app.k9mail.core.android.common.database.getStringOrNull
import app.k9mail.core.android.common.database.getStringOrThrow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class OutboxStateRepository
@OptIn(ExperimentalTime::class)
constructor(
    private val database: LockableDatabase,
    private val clock: Clock,
) {

    fun getOutboxState(messageId: Long): OutboxState {
        return database.execute(false) { db ->
            db.query(
                TABLE_NAME,
                COLUMNS,
                "$COLUMN_MESSAGE_ID = ?",
                arrayOf(messageId.toString()),
                null,
                null,
                null,
            ).use { cursor ->
                if (!cursor.moveToFirst()) {
                    throw IllegalStateException("No outbox_state entry for message with id $messageId")
                }

                val sendStateString = cursor.getStringOrThrow(COLUMN_SEND_STATE)
                val numberOfSendAttempts = cursor.getIntOrThrow(COLUMN_NUMBER_OF_SEND_ATTEMPTS)
                val sendErrorTimestamp = cursor.getLongOrThrow(COLUMN_ERROR_TIMESTAMP)
                val sendError = cursor.getStringOrNull(COLUMN_ERROR)

                val sendState = SendState.fromDatabaseName(sendStateString)

                OutboxState(sendState, numberOfSendAttempts, sendError, sendErrorTimestamp)
            }
        }
    }

    fun initializeOutboxState(messageId: Long) {
        database.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put(COLUMN_MESSAGE_ID, messageId)
                put(COLUMN_SEND_STATE, SendState.READY.databaseName)
            }

            db.insert(TABLE_NAME, null, contentValues)
        }
    }

    fun removeOutboxState(messageId: Long) {
        database.execute(false) { db ->
            db.delete(TABLE_NAME, "$COLUMN_MESSAGE_ID = ?", arrayOf(messageId.toString()))
        }
    }

    fun incrementSendAttempts(messageId: Long) {
        database.execute(false) { db ->
            db.execSQL(
                "UPDATE $TABLE_NAME " +
                    "SET $COLUMN_NUMBER_OF_SEND_ATTEMPTS = $COLUMN_NUMBER_OF_SEND_ATTEMPTS + 1 " +
                    "WHERE $COLUMN_MESSAGE_ID = ?",
                arrayOf(messageId.toString()),
            )
        }
    }

    fun decrementSendAttempts(messageId: Long) {
        database.execute(false) { db ->
            db.execSQL(
                "UPDATE $TABLE_NAME " +
                    "SET $COLUMN_NUMBER_OF_SEND_ATTEMPTS = $COLUMN_NUMBER_OF_SEND_ATTEMPTS - 1 " +
                    "WHERE $COLUMN_MESSAGE_ID = ?",
                arrayOf(messageId.toString()),
            )
        }
    }

    fun setSendAttemptError(messageId: Long, errorMessage: String) {
        @OptIn(ExperimentalTime::class)
        val sendErrorTimestamp = clock.now().toEpochMilliseconds()

        database.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put(COLUMN_SEND_STATE, SendState.ERROR.databaseName)
                put(COLUMN_ERROR_TIMESTAMP, sendErrorTimestamp)
                put(COLUMN_ERROR, errorMessage)
            }

            db.update(TABLE_NAME, contentValues, "$COLUMN_MESSAGE_ID = ?", arrayOf(messageId.toString()))
        }
    }

    fun setSendAttemptsExceeded(messageId: Long) {
        @OptIn(ExperimentalTime::class)
        val sendErrorTimestamp = clock.now().toEpochMilliseconds()

        database.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put(COLUMN_SEND_STATE, SendState.RETRIES_EXCEEDED.databaseName)
                put(COLUMN_ERROR_TIMESTAMP, sendErrorTimestamp)
            }

            db.update(TABLE_NAME, contentValues, "$COLUMN_MESSAGE_ID = ?", arrayOf(messageId.toString()))
        }
    }

    /**
     * Keeps why the last try failed, without changing whether the message will be tried again, so
     * the Outbox can say why a message is still there.
     */
    fun setLastError(messageId: Long, errorMessage: String?) {
        @OptIn(ExperimentalTime::class)
        val timestamp = clock.now().toEpochMilliseconds()

        database.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put(COLUMN_ERROR_TIMESTAMP, timestamp)
                if (errorMessage.isNullOrBlank()) putNull(COLUMN_ERROR) else put(COLUMN_ERROR, errorMessage)
            }

            db.update(TABLE_NAME, contentValues, "$COLUMN_MESSAGE_ID = ?", arrayOf(messageId.toString()))
        }
    }

    /** The messages whose last try failed, by message id, with why when it is known. */
    fun getFailedMessages(): Map<Long, OutboxState> {
        return database.execute(false) { db ->
            db.query(
                TABLE_NAME,
                arrayOf(COLUMN_MESSAGE_ID) + COLUMNS,
                "$COLUMN_ERROR_TIMESTAMP > 0 OR $COLUMN_SEND_STATE != ?",
                arrayOf(SendState.READY.databaseName),
                null,
                null,
                null,
            ).use { cursor ->
                buildMap {
                    while (cursor.moveToNext()) {
                        val messageId = cursor.getLongOrThrow(COLUMN_MESSAGE_ID)
                        val sendState = SendState.fromDatabaseName(cursor.getStringOrThrow(COLUMN_SEND_STATE))
                        put(
                            messageId,
                            OutboxState(
                                sendState,
                                cursor.getIntOrThrow(COLUMN_NUMBER_OF_SEND_ATTEMPTS),
                                cursor.getStringOrNull(COLUMN_ERROR),
                                cursor.getLongOrThrow(COLUMN_ERROR_TIMESTAMP),
                            ),
                        )
                    }
                }
            }
        }
    }

    /**
     * Gives every message in the Outbox a fresh start, as when it was first sent: asked for by
     * choosing Send messages, so a message given up on is tried again. Tries made on their own
     * still stop after [com.fsck.k9.K9.MAX_SEND_ATTEMPTS].
     */
    fun resetAll() {
        database.execute(false) { db ->
            val contentValues = ContentValues().apply {
                put(COLUMN_SEND_STATE, SendState.READY.databaseName)
                put(COLUMN_NUMBER_OF_SEND_ATTEMPTS, 0)
                put(COLUMN_ERROR_TIMESTAMP, 0)
                putNull(COLUMN_ERROR)
            }

            db.update(TABLE_NAME, contentValues, null, null)
        }
    }

    companion object {
        private const val TABLE_NAME = "outbox_state"
        private const val COLUMN_MESSAGE_ID = "message_id"
        private const val COLUMN_SEND_STATE = "send_state"
        private const val COLUMN_NUMBER_OF_SEND_ATTEMPTS = "number_of_send_attempts"
        private const val COLUMN_ERROR_TIMESTAMP = "error_timestamp"
        private const val COLUMN_ERROR = "error"

        private val COLUMNS = arrayOf(
            COLUMN_SEND_STATE,
            COLUMN_NUMBER_OF_SEND_ATTEMPTS,
            COLUMN_ERROR_TIMESTAMP,
            COLUMN_ERROR,
        )
    }
}
