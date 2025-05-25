package com.example.recycleview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

interface NewsItemClicked {
    fun onItemClicked(item: News)
}

class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var mAdapter: NewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.modern_news)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = NewsListAdapter(this)
        recyclerView.adapter = mAdapter

        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            fetchData { swipeRefreshLayout.isRefreshing = false }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    fetchData()
                    true
                }
                R.id.navigation_categories -> {
                    Toast.makeText(this, "Categories Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_saved -> {
                    Toast.makeText(this, "Saved News Coming Soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        fetchData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                Toast.makeText(this, "Search Coming Soon", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_refresh -> {
                fetchData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchData(onComplete: (() -> Unit)? = null) {
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

//        yup thats my api key what u gonna do about it? :D

        val url = "https://newsdata.io/api/1/latest?apikey=pub_774e744edee14dd3952d435b154d3753&q=AI=example&language=en"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                val newsJSONArray = response.getJSONArray("results")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJSONArray.length()) {
                    val newsJSONObject = newsJSONArray.getJSONObject(i)
                    val news = News(
                        title = newsJSONObject.optString("title"),
                        author = extractCreator(newsJSONObject),
                        url = newsJSONObject.optString("link"),
                        urlToImage = newsJSONObject.optString("image_url").takeIf { it.isNotEmpty() },
                        description = newsJSONObject.optString("description").takeIf { it.isNotEmpty() },
                        source = newsJSONObject.optString("source_id").takeIf { it.isNotEmpty() }
                    )
                    newsArray.add(news)
                }
                mAdapter.updateNews(newsArray)
                progressBar.visibility = android.view.View.GONE
                onComplete?.invoke()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = android.view.View.GONE
                onComplete?.invoke()
            }
        )
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onItemClicked(item: News) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = item.url.toUri()
        }
        startActivity(intent)
    }

    private fun extractCreator(newsJSONObject: JSONObject): String? {
        return try {
            val creatorArray = newsJSONObject.optJSONArray("creator")
            if (creatorArray != null && creatorArray.length() > 0) {
                creatorArray.optString(0).takeIf { it.isNotEmpty() }
            } else {
                null
            }
        } catch (e: org.json.JSONException) {
            android.util.Log.e("MainActivity", "Error parsing creator: ${e.message}")
            null
        }
    }
}
