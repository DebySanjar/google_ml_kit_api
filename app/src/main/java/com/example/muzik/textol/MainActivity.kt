package com.example.muzik.textol

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.example.muzik.textol.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCopy.visibility = View.GONE
        binding.btnShare.visibility = View.GONE


        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.btnShare.setOnClickListener {
            shareText()
        }



        binding.btnCopy.setOnClickListener {
            copyTextToClipboard()
        }
    }

    private fun shareText() {
        val textToShare = binding.textViewResult.text.toString()
        if (textToShare.isNotEmpty()) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
            }
            startActivity(Intent.createChooser(shareIntent, "Matnni ulashish"))
        }
    }


    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data!!.data
                imageUri?.let {
                    binding.imageView.setImageURI(it)

                    // ✅ GLIDE bilan yuklash (ruxsatlarni muammosiz hal qiladi)
                    Glide.with(this).load(it).into(binding.imageView)


                    val image = InputImage.fromFilePath(this, it)
                    processTextRecognition(image)
                }
            }
        }

    private fun processTextRecognition(image: InputImage) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val handler = Handler(Looper.getMainLooper())

        // ⏳ ProgressBar'ni 500ms (0.5 sekund) kechiktirib chiqaramiz
        handler.postDelayed({
            binding.progressBar.visibility = View.VISIBLE
        }, 500)


        binding.textViewResult.text = "Matnni tahlil qilinmoqda..."

        recognizer.process(image).addOnSuccessListener { visionText ->
            val recognizedText = visionText.text

            handler.postDelayed({
                binding.textViewResult.text = recognizedText

                binding.btnCopy.visibility =
                    if (recognizedText.isNotEmpty()) View.VISIBLE else View.GONE
                binding.btnShare.visibility =
                    if (recognizedText.isNotEmpty()) View.VISIBLE else View.GONE
                binding.progressBar.visibility = View.GONE
            }, 2000)
        }.addOnFailureListener { e ->
            binding.textViewResult.text = "Xatolik: ${e.message}"
            binding.btnCopy.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun copyTextToClipboard() {
        val text = binding.textViewResult.text.toString()
        if (text.isNotEmpty()) {
            val clipboard = getSystemService(this, ClipboardManager::class.java)
            val clip = ClipData.newPlainText("Recognized Text", text)
            clipboard?.setPrimaryClip(clip)

            Toast.makeText(this, "Ajoyib! Nusxa olindi", Toast.LENGTH_SHORT).show()
        }
    }
}
