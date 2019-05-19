package com.example.shakib.track

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_share.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ShareActivity : AppCompatActivity() {

    private var btn: Button? = null
    private var imageview: ImageView? = null
    private val GALLERY = 1

    internal var storage: FirebaseStorage?=null
    internal var storageReference: StorageReference?=null

    internal var database: FirebaseDatabase?=null
    internal var databaseReference: DatabaseReference ?=null

    var lg:String = ""
    var lt:String = ""

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        lg = intent.getStringExtra("Longitude")
        lt = intent.getStringExtra("Latitude")
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        btn = findViewById<View>(R.id.btn) as Button
        imageview = findViewById<View>(R.id.iv) as ImageView

        btn!!.setOnClickListener {
            choosePhotoFromGallary()
        }


    }

    fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    public override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data!!.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    val path = saveImage(bitmap)
                    Toast.makeText(this@ShareActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
                    imageview!!.setImageBitmap(bitmap)


                    /***
                     *
                     *
                     *
                     * Image share
                     *
                     */
                    btnShare.setOnClickListener {
                        val imageReference = FirebaseStorage.getInstance().reference.child(lg + " " + lt)
                        imageReference.putFile(contentURI!!)
                                .addOnSuccessListener { taskSnapshot ->
                                    val uri =  taskSnapshot.downloadUrl.toString()
                                    //val Name =  taskSnapshot.metadata!!.name
                                    //val Path = taskSnapshot.metadata!!.path
                                    //val Size = taskSnapshot.metadata!!.sizeBytes

                                    databaseReference = FirebaseDatabase.getInstance().getReference()
                                    val kk = databaseReference!!.push().key
                                    val dReference: DatabaseReference = databaseReference!!.child(kk)

                                    val item = data()

                                    item.longitude = lg
                                    item.latitude = lt
                                    item.url = uri

                                    /***
                                     *
                                     * Image Information with downloadURL
                                     *
                                     */
                                    Log.d("longitude", item.longitude)
                                    Log.d("latitude", item.latitude)
                                    Log.d("IMAGE URL ", item.url)

                                    dReference.setValue(item)


                                }
                                .addOnFailureListener { exception ->
                                    // Handle unsuccessful uploads
                                }
                                .addOnProgressListener { taskSnapshot ->
                                    // taskSnapshot.bytesTransferred
                                    // taskSnapshot.totalByteCount
                                }
                                .addOnPausedListener { taskSnapshot ->
                                    // Upload is paused
                                }
                        Toast.makeText(this@ShareActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@ShareActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }


    /*****
     * Function for Save image to internal storage "/Track" folder
     */
    fun saveImage(myBitmap: Bitmap):String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
                (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists()) {   /// if directory doesn't exists
            wallpaperDirectory.mkdirs()
        }
        try {
            val f = File(wallpaperDirectory, (lg + "_" + lt + "_" + (Calendar.getInstance().getTimeInMillis()).toString() + ".jpg"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(f.getPath()), arrayOf("image/jpeg"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())
            return f.getAbsolutePath()
        }
        catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    companion object {
        private val IMAGE_DIRECTORY = "/Track"
    }
}
