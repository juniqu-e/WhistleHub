package com.whistlehub.common.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.whistlehub.common.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)  // 🔹 사용자 정보 저장 (덮어쓰기)

    @Query("SELECT * FROM user_table")
    suspend fun getUser(): UserEntity?  // 🔹 사용자 정보 불러오기

    @Query("DELETE FROM user_table")
    suspend fun clearUser()  // 🔹 로그아웃 시 데이터 삭제
}