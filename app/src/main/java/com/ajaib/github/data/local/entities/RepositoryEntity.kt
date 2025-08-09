package com.ajaib.github.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "repositories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["login"],
            childColumns = ["owner_login"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("owner_login")]
)
data class RepositoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "html_url")
    val htmlUrl: String,

    @ColumnInfo(name = "language")
    val language: String?,

    @ColumnInfo(name = "stargazers_count")
    val stargazersCount: Int,

    @ColumnInfo(name = "forks_count")
    val forksCount: Int,

    @ColumnInfo(name = "watchers_count")
    val watchersCount: Int,

    @ColumnInfo(name = "size")
    val size: Int,

    @ColumnInfo(name = "default_branch")
    val defaultBranch: String,

    @ColumnInfo(name = "open_issues_count")
    val openIssuesCount: Int,

    @ColumnInfo(name = "is_private")
    val isPrivate: Boolean,

    @ColumnInfo(name = "is_fork")
    val isFork: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    @ColumnInfo(name = "pushed_at")
    val pushedAt: String?,

    @ColumnInfo(name = "owner_login")
    val ownerLogin: String,

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)
