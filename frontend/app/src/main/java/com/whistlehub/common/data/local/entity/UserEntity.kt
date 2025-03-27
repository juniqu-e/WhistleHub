package com.whistlehub.common.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val memberId: Int,  // 항상 하나의 사용자만 저장 (덮어쓰기)
    val profileImage: String?,
    val nickname: String,
)