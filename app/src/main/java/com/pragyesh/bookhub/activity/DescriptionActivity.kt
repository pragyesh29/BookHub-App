package com.pragyesh.bookhub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pragyesh.bookhub.R
import com.pragyesh.bookhub.database.BookDatabase
import com.pragyesh.bookhub.database.BookEntity
import com.pragyesh.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_description.*
import org.json.JSONObject
import org.w3c.dom.Text

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtbookName:TextView
    lateinit var txtbookAuthor:TextView
    lateinit var txtbookRating:TextView
    lateinit var imgbookImage:ImageView
    lateinit var txtbookPrice:TextView
    lateinit var progressLayout:RelativeLayout
    lateinit var progressBar:ProgressBar
    lateinit var btnAddToFav:Button
    lateinit var txtBookDesc:TextView
    lateinit var toolbar:Toolbar

    var bookId:String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)
        txtbookName = findViewById(R.id.txtBookName)
        txtbookAuthor = findViewById(R.id.txtBookAuthor)
        txtbookRating = findViewById(R.id.txtBookRating)
        imgbookImage = findViewById(R.id.imgBookImage)
        txtbookPrice = findViewById(R.id.txtBookPrice)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"

        if(intent!=null){
            bookId = intent.getStringExtra("book_id")
        }else{
            Toast.makeText(this@DescriptionActivity,"Some unexpected error occoured",Toast.LENGTH_SHORT).show()
        }

        if(bookId == "100"){
            finish()
            Toast.makeText(this@DescriptionActivity,"Some unexpected error occoured",Toast.LENGTH_SHORT).show()
        }
        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"
        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)
        if(ConnectionManager().checkConnectivity(this@DescriptionActivity)){
            val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {
                try{
                    val success = it.getBoolean("success")
                    if(success){
                        val bookJsonObject = it.getJSONObject("book_data")
                        progressLayout.visibility = View.GONE

                        val bookImgUrl = bookJsonObject.getString("image")

                        Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.default_book_cover).into(imgbookImage)
                        txtbookName.text = bookJsonObject.getString("name")
                        txtbookAuthor.text = bookJsonObject.getString("author")
                        txtBookDesc.text = bookJsonObject.getString("description")
                        txtbookRating.text = bookJsonObject.getString("rating")
                        txtbookPrice.text = bookJsonObject.getString("price")

                        val bookEntity = BookEntity(
                            bookId?.toInt() as Int,
                            txtbookName.text.toString(),
                            txtbookAuthor.text.toString(),
                            txtBookDesc.text.toString(),
                            txtbookPrice.text.toString(),
                            txtbookRating.text.toString(),
                            bookImgUrl
                        )

                        val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                        val isFav = checkFav.get()

                        if(isFav){
                            btnAddToFav.text = "Remove from Favorites"
                            val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavorite)
                            btnAddToFav.setBackgroundColor(favColor)
                        }else{
                            btnAddToFav.text = "Add To Favorites"
                            val noFavColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                            btnAddToFav.setBackgroundColor(noFavColor)
                        }

                        btnAddToFav.setOnClickListener{
                            if(!DBAsyncTask(applicationContext, bookEntity, 1).execute().get()){
                                val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                                val result = async.get()
                                if(result){
                                    Toast.makeText(this@DescriptionActivity,"Book added to Favorites", Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Remove from Favorites"
                                    val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavorite)
                                    btnAddToFav.setBackgroundColor(favColor)
                                }else{
                                    Toast.makeText(this@DescriptionActivity,"Some error occurred", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                                val result = async.get()
                                if(result){
                                    Toast.makeText(this@DescriptionActivity,"Book removed from Favorites", Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Add To Favorites"
                                    val noFavColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                    btnAddToFav.setBackgroundColor(noFavColor)
                                }else{
                                    Toast.makeText(this@DescriptionActivity,"Some error occurred", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }else{
                        Toast.makeText(this@DescriptionActivity, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                    }
                }catch (e: Exception){
                    Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occurred", Toast.LENGTH_SHORT).show()
                }
            },Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity, "Volley Error occurred", Toast.LENGTH_SHORT).show()
            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "c5b6606c9aa5af"
                    return headers
                }
            }
            queue.add(jsonRequest)
        }else{
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings"){text, listener->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode:Int): AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode){
                1 -> {
                    //Check DB if book is fav or not
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    //Save the book in the DB as fav
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    //Remove the fav book
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

}
