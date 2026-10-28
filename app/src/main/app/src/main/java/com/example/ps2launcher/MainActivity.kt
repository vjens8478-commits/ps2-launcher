package com.example.ps2launcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val requestPickFolder = 1
    private val gameExtensions = listOf(".iso", ".bin", ".chd")
    private lateinit var listLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val pickButton = Button(this).apply {
            text = "Pilih folder game"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    )
                }
                startActivityForResult(intent, requestPickFolder)
            }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scroll = ScrollView(this).apply {
            addView(listLayout)
        }

        root.addView(pickButton)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        setContentView(root)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != requestPickFolder || resultCode != RESULT_OK) return
        val treeUri: Uri = data?.data ?: return

        contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        showGames(treeUri)
    }

    private fun showGames(treeUri: Uri) {
        listLayout.removeAllViews()

        val folderId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, folderId)
        val names = mutableListOf<String>()

        contentResolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0))
            }
        }

        val games = names
            .filter { name -> gameExtensions.any { name.lowercase().endsWith(it) } }
            .sorted()

        if (games.isEmpty()) {
            listLayout.addView(TextView(this).apply {
                text = "Tidak ada file .iso, .bin, atau .chd di folder ini."
            })
            return
        }

        for (name in games) {
            listLayout.addView(Button(this).apply {
                text = name
                setOnClickListener {
                    Toast.makeText(
                        this@MainActivity,
                        "Peluncuran belum dibuat: $name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}
