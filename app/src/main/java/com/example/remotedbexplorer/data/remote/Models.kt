package com.example.remotedbexplorer.data.remote

/**
 * Information about a database table or view.
 *
 * @property name The table/view name.
 * @property type Either "TABLE" or "VIEW".
 */
data class TableInfo(
    val name: String,
    val type: String // "TABLE" or "VIEW"
)

/**
 * Metadata for a single column within a table.
 *
 * @property name Column name.
 * @property dataType SQL data type string (e.g. "VARCHAR", "INTEGER").
 * @property isPrimaryKey Whether this column is part of the primary key.
 * @property isNullable Whether this column allows NULL values.
 * @property ordinalPosition 1-based column position.
 */
data class ColumnInfo(
    val name: String,
    val dataType: String,
    val isPrimaryKey: Boolean = false,
    val isNullable: Boolean = true,
    val ordinalPosition: Int = 0
)

/**
 * Result of executing a database query.
 *
 * @property columns Column names in result order. Null for non-SELECT statements.
 * @property rows List of row data, each row is a list of cell values (as strings).
 *   Null for non-SELECT statements.
 * @property affectedRows Number of rows affected (for INSERT/UPDATE/DELETE).
 * @property isSelectResult True if this is a SELECT/query result.
 * @property errorMessage Error description if the query failed, null on success.
 */
data class QueryResult(
    val columns: List<String>? = null,
    val rows: List<List<String?>>? = null,
    val affectedRows: Int = 0,
    val isSelectResult: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** Factory for a successful SELECT result. */
        fun selectResult(columns: List<String>, rows: List<List<String?>>): QueryResult =
            QueryResult(
                columns = columns,
                rows = rows,
                isSelectResult = true
            )

        /** Factory for a non-SELECT DML/DDL result with affected row count. */
        fun updateResult(affectedRows: Int): QueryResult =
            QueryResult(
                affectedRows = affectedRows,
                isSelectResult = false
            )

        /** Factory for a failed query. */
        fun error(message: String): QueryResult =
            QueryResult(errorMessage = message)
    }
}
