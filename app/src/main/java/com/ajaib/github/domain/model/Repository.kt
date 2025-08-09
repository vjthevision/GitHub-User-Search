package com.ajaib.github.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Repository(
    val id: Int,
    val name: String,
    val fullName: String,
    val description: String?,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val watchersCount: Int,
    val size: Int,
    val defaultBranch: String,
    val openIssuesCount: Int,
    val isPrivate: Boolean,
    val isFork: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val pushedAt: String?
) : Parcelable
