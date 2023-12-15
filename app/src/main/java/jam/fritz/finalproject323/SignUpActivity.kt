package jam.fritz.finalproject323

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.Intent
import android.graphics.Canvas
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var imageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var captureButton: Button
    private var imageCapture: ImageCapture? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private lateinit var outputDirectory: File
    private lateinit var viewFinder: PreviewView
    private val MY_REQUEST_PERMISSIONS_CAMERA = 1
    private lateinit var layoutPreview: LinearLayout


    //Output directory for images
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageView.setImageURI(uri)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        //Find all views and make references to them
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        imageView = findViewById(R.id.profile)
        nameEditText = findViewById(R.id.name)
        emailEditText = findViewById(R.id.email)
        signUpButton = findViewById(R.id.signup)
        captureButton = findViewById(R.id.capture)
        passwordEditText = findViewById(R.id.password)
        viewFinder = findViewById(R.id.viewFinder)
        layoutPreview = findViewById(R.id.layoutPreview)

        //Load camera preview and button
        imageView.setOnClickListener { view ->
            showImagePickerDialog()
        }
        //Logic for sign up button
        signUpButton.setOnClickListener { view ->
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameEditText.text.toString()

            // Create a user in authentication, if they already exist log them in
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // Get a reference to the Firestore service
                        val db = FirebaseFirestore.getInstance()

                        val userMap = HashMap<String, Any>()
                        userMap["Email"] = email
                        userMap["Name"] = name

                        val storageRef = user.let {
                            it?.let { it1 ->
                                FirebaseStorage.getInstance().reference.child("profileImages/${user?.uid}") // Get path to save image
                                    .child(it1.uid)
                            }
                        }

                        //Compress image
                        val bitmap = Bitmap.createBitmap(
                            imageView.width,
                            imageView.height,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        imageView.draw(canvas)

                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()

                        var uploadTask = storageRef!!.putBytes(data)
                        uploadTask.addOnFailureListener {exception ->
                            Log.d("upload", "failing: ${exception.message}")
                        }.addOnSuccessListener { taskSnapshot ->
                            // Get the download URL
                            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                                userMap["Image"] = uri.toString()

                                user.let {
                                    // Add the user's data to the users collection in Firestore
                                    if (it != null) {
                                        db.collection("users").document(it.uid).set(userMap)
                                            .addOnSuccessListener {
                                                Log.d("Login", "DocumentSnapshot successfully written!")
                                                // Start MainActivity here
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w("Login", "Error writing document", e)
                                            }
                                    }
                                }
                            }
                        }
                    }
                    else {
                        // Sign in logic
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // If sign in fails, display a message to the user.
                                    println("Sign in: failure")
                                }
                            }
                    }
                }
        }


        //Takes photo
        captureButton.setOnClickListener{view ->
            takePhoto()
        }
    }

    //Dialog box
    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Camera")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> getContent.launch("image/*")
                1 -> startCamera()
            }
        }
        builder.show()
    }

    //Image preview, asks for camera permissions and gets access
    private fun startCamera() {

        layoutPreview.visibility = View.VISIBLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_REQUEST_PERMISSIONS_CAMERA)
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(this))
    }

    //Takes photo an duploads
    private fun takePhoto() {
        outputDirectory = getOutputDirectory()
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    imageView.setImageURI(savedUri)

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(this@SignUpActivity)
                    cameraProviderFuture.addListener({
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                    }, ContextCompat.getMainExecutor(this@SignUpActivity))
                    layoutPreview.visibility = View.GONE

                }
                override fun onError(exc: ImageCaptureException) {

                }
            })
    }

}
