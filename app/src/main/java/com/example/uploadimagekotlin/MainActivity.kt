package com.example.uploadimagekotlin

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.uploadimagekotlin.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE: Int = 1
    lateinit var listUri:ArrayList<Uri>
    lateinit var binding:ActivityMainBinding
    private lateinit var ImageUri:Uri
    var upload_Count = 0
    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listUri = ArrayList<Uri>()
        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setMessage("Image Uploading Plese Wait .........")
        binding.apply {
            MainChose.setOnClickListener {
                var intent_:Intent = Intent(Intent.ACTION_GET_CONTENT)
                intent_.type = "image/*"
                intent_.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
                startActivityForResult(intent_,PICK_IMAGE)
                
            }
            MainUpload.setOnClickListener {
                progressDialog.show()
                tvAlert.text = " If Loading Takes too long please Press the button again"

                var imageFolder:StorageReference = FirebaseStorage.getInstance().getReference().child("ImageFolder")
                for (i in 0..listUri.size-1)
                {
                    var IndividualImage:Uri = listUri.get(i)
                    var ImageName:StorageReference = imageFolder.child("Image"+IndividualImage.lastPathSegment)

                    ImageName.putFile(IndividualImage).addOnSuccessListener {
                        ImageName.downloadUrl.addOnSuccessListener{
                            var url:String = it.toString()
                            StoreLink(url)
                            Log.d("Link",url)
                        }
                    }
                }

            }
        }
    }

    private fun StoreLink(url: String) {
            var databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserOne")
            var map:HashMap<String,String> = HashMap()
            map.put("ImgLink",url);
            databaseReference.push().setValue(map)
            progressDialog.dismiss()
            binding.tvAlert.text = "Image Uploaded Succesfully "
            binding.MainUpload.visibility = View.GONE
//            listUri.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==PICK_IMAGE)
        {
            if (resultCode == RESULT_OK)
            {
                if (data?.clipData != null)
                {
                        var countClipData:Int = data.clipData!!.itemCount
                    var currentImageSlect = 0
                    while (currentImageSlect < countClipData)
                    {
                            ImageUri = data.clipData!!.getItemAt(currentImageSlect).uri
                        listUri.add(ImageUri)
                        currentImageSlect = currentImageSlect+1
                    }
                    binding.apply {
                        tvAlert.visibility = View.VISIBLE
                        tvAlert.setText("You have slected "+listUri.size +" Images")
                        MainChose.visibility = View.GONE;
                    }
                }
            }else
            {
                Toast.makeText(this@MainActivity,"Please Select Multiple Image",Toast.LENGTH_LONG).show()
            }
        }
    }
}