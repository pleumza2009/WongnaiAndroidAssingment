package com.thanakorn.jaroensetthakul.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanakorn.jaroensetthakul.adapters.CoinAdapter
import com.thanakorn.jaroensetthakul.databinding.ActivityMainBinding
import com.thanakorn.jaroensetthakul.utilities.Constants.Companion.QUERY_PAGE_SIZE
import com.thanakorn.jaroensetthakul.utilities.Resource
import com.thanakorn.jaroensetthakul.viewmodels.CoinViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel : CoinViewModel by viewModels()
    private lateinit var coinAdapter: CoinAdapter

    val TAG = "BreakingNewsFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        fetchData()





    }



     fun fetchData(){
         viewModel.coins.observe(this, Observer { response ->
             when(response){
                 is Resource.Success -> {
                     hideProgressBar()
                     response.data.let { coinResponse ->
                         coinAdapter.differ.submitList(coinResponse?.data?.coins)
                     }
                 }
                 is Resource.Error -> {
                     hideProgressBar()
                     response.message?.let { message ->
                         Log.e(TAG, "An error occured: $message")
                     }
                 }
                 is Resource.Loading -> {
                     showProgressBar()
                 }
             }
         })

    }





    private fun hideProgressBar() {
        binding.ProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.ProgressBar.visibility = View.VISIBLE
    }


    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                viewModel.getCoins()
                isScrolling = false
            } else {
                binding.rvCoinRanking.setPadding(0, 0, 0, 0)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }






    fun  setupRecyclerView(){
        coinAdapter = CoinAdapter()
        binding.rvCoinRanking.apply {
            adapter = coinAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addOnScrollListener(this@MainActivity.scrollListener)
        }
    }
}