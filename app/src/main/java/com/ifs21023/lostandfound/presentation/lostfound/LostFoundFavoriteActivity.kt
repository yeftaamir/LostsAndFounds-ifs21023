package com.ifs21023.lostandfound.presentation.lostfound

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21023.lostandfound.R
import com.ifs21023.lostandfound.adapter.LostfoundAdapter
import com.ifs21023.lostandfound.data.local.entity.DelcomLostFoundEntity
import com.ifs21023.lostandfound.data.remote.MyResult
import com.ifs21023.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21023.lostandfound.databinding.ActivityLostFoundFavoriteBinding
import com.ifs21023.lostandfound.helper.Utils.Companion.entitiesToResponses
import com.ifs21023.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21023.lostandfound.presentation.ViewModelFactory

class LostFoundFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostFoundFavoriteBinding
    private val viewModel by viewModels<LostFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostFoundDetailActivity.RESULT_CODE) {
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    LostFoundDetailActivity.KEY_IS_CHANGED,
                    false
                )
                if (isChanged) {
                    recreate()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostFoundFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupAction() {
        binding.appbarLostFoundFavorite.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(LostFoundDetailActivity.KEY_IS_CHANGED, true)
            setResult(LostFoundDetailActivity.RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }
    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        binding.appbarLostFoundFavorite.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)
        observeGetLostFounds()
    }
    private fun observeGetLostFounds() {
        viewModel.getLocalLostFounds().observe(this) { lostfounds ->
            loadLostFoundsToLayout(lostfounds)
        }
    }
    private fun loadLostFoundsToLayout(lostfounds: List<DelcomLostFoundEntity>?) {
        showLoading(false)
        val layoutManager = LinearLayoutManager(this)
        binding.rvLFoundFavorite.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvLFoundFavorite.addItemDecoration(itemDecoration)
        if (lostfounds.isNullOrEmpty()) {
            showEmptyError(true)
            binding.rvLFoundFavorite.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = LostfoundAdapter()
            adapter.submitOriginalList(entitiesToResponses(lostfounds))
            binding.rvLFoundFavorite.adapter = adapter
            adapter.setOnItemClickCallback(
                object : LostfoundAdapter.OnItemClickCallback {
                    override fun onCheckedChangeListener(
                        lostfound: LostFoundsItemResponse,
                        isChecked: Boolean
                    ) {
                        adapter.filter(binding.svLFFavorite.query.toString())
                        val newLostFound = DelcomLostFoundEntity(
                            id = lostfound.id,
                            title = lostfound.title,
                            description = lostfound.description,
                            isCompleted = lostfound.isCompleted, // Sesuaikan dengan isCompleted
                            cover = lostfound.cover,
                            createdAt = lostfound.createdAt,
                            updatedAt = lostfound.updatedAt,
                            status = "", // disesuaikan dengan nilai default untuk status
                            userId = 0 // disesuaikan dengan nilai default untuk userId
                        )

                        viewModel.putLostFound(
                            lostfound.id,
                            lostfound.title,
                            lostfound.description,
                            lostfound.status,
                            isChecked
                        ).observeOnce {
                            when (it) {
                                is MyResult.Error -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Gagal menyelesaikan LostFound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Gagal batal menyelesaikan LostFound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                is MyResult.Success -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Berhasil menyelesaikan LostFound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@LostFoundFavoriteActivity,
                                            "Berhasil batal menyelesaikan lostfound: " + lostfound.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    viewModel.insertLocalLostFound(newLostFound)
                                }
                                else -> {}
                            }
                        }
                    }
                    override fun onClickDetailListener(lostfoundId: Int) {
                        val intent = Intent(
                            this@LostFoundFavoriteActivity,
                            LostFoundDetailActivity::class.java
                        )
                        intent.putExtra(LostFoundDetailActivity.KEY_LOST_FOUND_ID, lostfoundId)
                        launcher.launch(intent)
                    }
                })
            binding.svLFFavorite.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvLFoundFavorite
                            .layoutManager?.scrollToPosition(0)

                        return true
                    }
                })
        }
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svLFFavorite.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvLFoundFavorite.visibility =
            if (status) View.VISIBLE else View.GONE
    }
    private fun showEmptyError(isError: Boolean) {
        binding.tvLFFavoriteEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }
    private fun showLoading(isLoading: Boolean) {
        binding.pbLFFavorite.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

}