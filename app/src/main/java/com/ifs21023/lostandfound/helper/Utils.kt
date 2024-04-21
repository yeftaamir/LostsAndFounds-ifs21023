package com.ifs21023.lostandfound.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ifs21023.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21023.lostandfound.data.remote.MyResult
import com.ifs21023.lostandfound.data.remote.response.AuthorLostFoundsResponse
import com.ifs21023.lostandfound.data.remote.response.LostFoundsItemResponse

class Utils {
    companion object{
        fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
            val observerWrapper = object : Observer<T> {
                override fun onChanged(value: T) {
                    observer(value)
                    if (value is MyResult.Success<*> ||
                        value is MyResult.Error
                    ) {
                        removeObserver(this)
                    }
                }
            }
            observeForever(observerWrapper)
        }
        fun entitiesToResponses(entities: List<DelcomLostFoundEntity>): List<LostFoundsItemResponse> {
            return entities.map {
                LostFoundsItemResponse(
                    cover = it.cover ?: "", // Menambahkan handling null, bila cover null
                    updatedAt = it.updatedAt,
                    userId = it.userId,
                    author = AuthorLostFoundsResponse(
                        name = "Unknown",
                        photo = ""
                    ),
                    description = it.description,
                    createdAt = it.createdAt,
                    id = it.id,
                    title = it.title,
                    isCompleted = it.isCompleted,
                    status = it.status
                )
            }
        }

    }
}