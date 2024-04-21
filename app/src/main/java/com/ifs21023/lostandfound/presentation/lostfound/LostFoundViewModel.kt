package com.ifs21023.lostandfound.presentation.lostfound

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ifs21023.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21023.lostandfound.data.remote.MyResult
import com.ifs21023.lostandfound.data.remote.response.DataAddLostFoundResponse
import com.ifs21023.lostandfound.data.remote.response.DelcomLostFoundResponse
import com.ifs21023.lostandfound.data.remote.response.DelcomResponse
import com.ifs21023.lostandfound.data.repository.LocalLostFoundRepository
import com.ifs21023.lostandfound.data.repository.LostFoundRepository
import com.ifs21023.lostandfound.presentation.ViewModelFactory

class LostFoundViewModel(
    private val lostfoundRepository: LostFoundRepository,
    private val localLostFoundRepository: LocalLostFoundRepository
) : ViewModel() {

    fun getLostFound(lostfoundId: Int): LiveData<MyResult<DelcomLostFoundResponse>>{
        return lostfoundRepository.getLostFound(lostfoundId).asLiveData()
    }

    fun postLostFound(
        title: String,
        description: String,
        status: String
    ): LiveData<MyResult<DataAddLostFoundResponse>>{
        return lostfoundRepository.postLostFound(
            title,
            description,
            status
        ).asLiveData()
    }

    fun putLostFound(
        lostfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ): LiveData<MyResult<DelcomResponse>> {
        return lostfoundRepository.putLostFound(
            lostfoundId,
            title,
            description,
            isCompleted,
            status
        )
    }




    fun deleteLostFound(lostfoundId: Int): LiveData<MyResult<DelcomResponse>> {
        return lostfoundRepository.deleteLostFound(lostfoundId).asLiveData()
    }

    fun getLocalLostFounds(): LiveData<List<DelcomLostFoundEntity>?> {
        return localLostFoundRepository.getAllLostFounds()
    }

    fun getLocalLostFound(lostfoundId: Int): LiveData<DelcomLostFoundEntity?> {
        return localLostFoundRepository.get(lostfoundId)
    }
    fun insertLocalLostFound(lostfound: DelcomLostFoundEntity) {
        localLostFoundRepository.insert(lostfound)
    }
    fun deleteLocalLostFound(lostfound: DelcomLostFoundEntity) {
        localLostFoundRepository.delete(lostfound)
    }

    companion object {
        @Volatile
        private var INSTANCE: LostFoundViewModel? = null
        fun getInstance(
            lostfoundRepository: LostFoundRepository,
            localLostFoundRepository: LocalLostFoundRepository,
        ): LostFoundViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = LostFoundViewModel(
                    lostfoundRepository,
                    localLostFoundRepository
                )
            }
            return INSTANCE as LostFoundViewModel
        }
    }
}
