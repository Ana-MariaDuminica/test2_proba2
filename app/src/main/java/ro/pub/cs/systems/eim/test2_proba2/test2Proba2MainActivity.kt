package ro.pub.cs.systems.eim.test2_proba2

import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import okhttp3.OkHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private lateinit var resultText: TextView

    private val ACTION_AUTOCOMPLETE = "ro.pub.cs.systems.eim.test2_proba2.AUTOCOMPLETE"
    private val EXTRA_SUGGESTION = "suggestion"

    private val autocompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val suggestion = intent.getStringExtra(EXTRA_SUGGESTION) ?: return
            resultText.text = suggestion
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test2_proba2)

        val inputEditText = findViewById<EditText>(R.id.input_edit_text)
        val searchButton = findViewById<Button>(R.id.search_button)
        resultText = findViewById(R.id.result_text_view)

        searchButton.setOnClickListener {
            val query = inputEditText.text.toString()
            if (query.isNotEmpty()) {
                fetchSuggestions(query)
            }
        }


    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            autocompleteReceiver,
            IntentFilter(ACTION_AUTOCOMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    override fun onStop() {
        super.onStop()
        unregisterReceiver(autocompleteReceiver)
    }

    private fun fetchSuggestions(query: String) {
        val url =
            "https://www.google.com/complete/search?client=chrome&q=$query"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("AutocompleteService", "Request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string() ?: return

                // 3.a (deja)
                Log.d("AutocompleteService", "Response: $responseString")

                // 3.b: parsare + afișare intrarea a 3-a
                try {
                    val json = org.json.JSONArray(responseString)
                    val suggestions = json.getJSONArray(1)  // lista de autocomplete-uri

                    if (suggestions.length() >= 3) {
                        val third = suggestions.getString(2) // intrarea a 3-a (index 2)
                        Log.d("AutocompleteService", "3rd suggestion: $third")

                        val intent = Intent(ACTION_AUTOCOMPLETE).apply {
                            setPackage(packageName)
                            putExtra(EXTRA_SUGGESTION, third)
                        }
                        sendBroadcast(intent)


                    } else {
                        Log.d("AutocompleteService", "Less than 3 suggestions: ${suggestions.length()}")
                    }
                } catch (e: Exception) {
                    Log.e("AutocompleteService", "Parse error", e)
                }
            }
        })
    }
}
