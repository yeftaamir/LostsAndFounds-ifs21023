package com.ifs21023.lostandfound.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21023.lostandfound.R
import com.ifs21023.lostandfound.adapter.LostfoundAdapter
import com.ifs21023.lostandfound.data.remote.MyResult
import com.ifs21023.lostandfound.data.remote.response.DelcomLostFoundsResponse
import com.ifs21023.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21023.lostandfound.databinding.ActivityMainBinding
import com.ifs21023.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21023.lostandfound.presentation.ViewModelFactory
import com.ifs21023.lostandfound.presentation.login.LoginActivity
import com.ifs21023.lostandfound.presentation.profile.ProfileActivity
import com.ifs21023.lostandfound.presentation.lostfound.LostFoundDetailActivity
import com.ifs21023.lostandfound.presentation.lostfound.LostFoundFavoriteActivity
import com.ifs21023.lostandfound.presentation.lostfound.LostFoundManageActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostFoundManageActivity.RESULT_CODE) {
            recreate()
        }

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu) // Sesuaikan dengan nama file XML menu Anda
        return true
    }

    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)

        binding.appbarMain.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)

        observeGetLostFounds()
    }

    private fun setupAction() {
        binding.appbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainMenuProfile -> {
                    openProfileActivity()
                    true
                }

                R.id.mainMenuLogout -> {
                    viewModel.logout()
                    openLoginActivity()
                    true
                }

                R.id.mainMenuSemuaData -> {
                    // Saat menu "All Data" diklik, panggil fungsi getLostFounds()
                    observeGetLostFounds()
                    true
                }
                R.id.mainMenuDataSaya -> {
                    // Saat menu "My Data" diklik, panggil fungsi getLostFound()
                    observeGetMyLostFounds()
                    true
                }
                R.id.mainMenuFavoriteTodos -> {
                    openFavoriteLostFoundActivity()
                    true
                }
                else -> false
            }
        }


        binding.fabMainAddLostFound.setOnClickListener {
            openAddLostFoundActivity()
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                openLoginActivity()
            } else {
                observeGetLostFounds()
            }
        }
    }

    private fun observeGetLostFounds() {
        viewModel.getLostFounds().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }

                    is MyResult.Success -> {
                        showLoading(false)
                        loadLostFoundsToLayout(result.data)
                    }

                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }

    private fun loadLostFoundsToLayout(response: DelcomLostFoundsResponse) {
        val lostfounds = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainLostFounds.addItemDecoration(itemDecoration)

        if (lostfounds.isEmpty()) {
            showEmptyError(true)
            binding.rvMainLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)

            val adapter = LostfoundAdapter()
            adapter.submitOriginalList(lostfounds)
            binding.rvMainLostFounds.adapter = adapter
            adapter.setOnItemClickCallback(object : LostfoundAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    lostfound: LostFoundsItemResponse,
                    isCompleted: Boolean
                ) {
                    adapter.filter(binding.svMain.query.toString())

                    viewModel.putLostFound(
                        lostfound.id,
                        lostfound.title,
                        lostfound.description,
                        isCompleted,
                        lostfound.status
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isCompleted) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal batal menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is MyResult.Success -> {
                                if (isCompleted) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil batal menyelesaikan lostfound: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            else -> {}
                        }
                    }
                }

                override fun onClickDetailListener(lostfoundId: Int) {
                    val intent = Intent(
                        this@MainActivity,
                        LostFoundDetailActivity::class.java
                    )
                    intent.putExtra(LostFoundDetailActivity.KEY_LOST_FOUND_ID, lostfoundId)
                    launcher.launch(intent)
                }
            })

            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainLostFounds.layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }

    private fun observeGetMyLostFounds() {
        // Panggil fungsi getLostFound() dengan menyertakan nilai isMe
        viewModel.getLostFound().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                    is MyResult.Success -> {
                        showLoading(false)
                        loadLostFoundsToLayout(result.data)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbMain.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun openProfileActivity() {
        val intent = Intent(applicationContext, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svMain.visibility =
            if (status) View.VISIBLE else View.GONE

        binding.rvMainLostFounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    private fun showEmptyError(isError: Boolean) {
        binding.tvMainEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    private fun openLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun openAddLostFoundActivity() {
        val intent = Intent(
            this@MainActivity,
            LostFoundManageActivity::class.java
        )
        intent.putExtra(LostFoundManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }

    private fun openFavoriteLostFoundActivity() {
        val intent = Intent(
            this@MainActivity,
            LostFoundFavoriteActivity::class.java
        )
        launcher.launch(intent)
    }
}