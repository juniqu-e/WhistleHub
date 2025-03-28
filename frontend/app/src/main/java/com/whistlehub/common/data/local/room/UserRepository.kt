package com.whistlehub.common.data.local.room

import com.whistlehub.common.data.local.dao.UserDao
import com.whistlehub.common.data.local.entity.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun saveUser(user: UserEntity) {
        userDao.saveUser(user)
    }

    suspend fun getUser(): UserEntity? {
        return userDao.getUser()
    }

    suspend fun clearUser() {
        userDao.clearUser()
    }
}