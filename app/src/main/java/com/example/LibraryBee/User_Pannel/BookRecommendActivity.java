package com.example.LibraryBee.User_Pannel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LibraryBee.R;
import com.example.LibraryBee.ml.BookRecommendationModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookRecommendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> recommendedBooks;

    private ProgressDialog progressDialog;
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_recommend);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading book recommendations...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString().trim();
                loadRecommendedBooks(searchQuery);
            }
        });

        loadRecommendedBooks(""); // Load random recommendations initially
    }

    private void loadRecommendedBooks(String searchQuery) {
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    recommendedBooks = getRecommendedBooks(BookRecommendActivity.this, searchQuery);
                } catch (IOException | CsvValidationException e) {
                    Log.e("BookRecommendActivity", "Error loading book recommendations", e);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        bookAdapter = new BookAdapter(recommendedBooks);
                        recyclerView.setAdapter(bookAdapter);
                    }
                });
            }
        }).start();


    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(context.getFilesDir() + "/book_recommendation_model.tflite");
             FileChannel fileChannel = fileInputStream.getChannel()) {
            long startOffset = 0;
            long declaredLength = fileChannel.size();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private List<Book> getRecommendedBooks(Context context, String searchQuery) throws IOException, CsvValidationException {
        List<Book> books = new ArrayList<>();

        try (InputStream inputStream = context.getAssets().open("cleaned_Books.csv");
             CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream))) {
            // Skip the header row
            csvReader.readNext();

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                try {
                    // Sanitize and parse the data
                    String title = nextLine[1].trim();
                    String author = nextLine[2].trim();
                    int year = parseYear(nextLine[3].trim());
                    String imageUrl = nextLine[6].trim();

                    // Check if the search query matches any of the book features
                    if (searchQuery.isEmpty() || title.contains(searchQuery) || author.contains(searchQuery) || String.valueOf(year).contains(searchQuery)) {
                        // Create a Book object and add it to the list if year is valid
                        if (year != -1) {
                            Book book = new Book(title, author, year, imageUrl);
                            books.add(book);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Log the error for rows with unexpected format
                    Log.e("BookRecommendActivity", "Row format issue: " + String.join(",", nextLine), e);
                }
            }

            return recommendBooks(context, books, searchQuery);

        }
    }

    private List<Book> recommendBooks(Context context, List<Book> books, String searchQuery) {
        List<Book> recommendedBooks = new ArrayList<>();
        if (!searchQuery.isEmpty()) {
            try {
                // Initialize the TFLite model
                BookRecommendationModel model = BookRecommendationModel.newInstance(context);

                // Create the input buffer for the model
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 3005}, DataType.FLOAT32);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 3005);
                byteBuffer.order(ByteOrder.nativeOrder());

                // Fill the ByteBuffer with data (replace with your actual preprocessing)
                byteBuffer.putFloat(searchQuery.hashCode()); // Example data; replace with actual encoding
                inputFeature0.loadBuffer(byteBuffer);

                // Run model inference
                BookRecommendationModel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                // Extract recommendations from the model's output
                float[] outputArray = outputFeature0.getFloatArray();
                // Interpret the output to get book recommendations (example logic)
                if (!books.isEmpty()) {
                    for (int i = 0; i < Math.min(outputArray.length, 5); i++) {
                        if (i < books.size()) {
                            Book book = books.get(i);
                            recommendedBooks.add(book);
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BookRecommendActivity.this, "No books found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                model.close();
            } catch (IOException e) {
                Log.e("BookRecommendActivity", "Error loading model", e);
            }

        } else {
            // Randomly select 5 books
            Random random = new Random();
            int booksSize = books.size();
            for (int i = 0; i < 5 && booksSize > 0; i++) {
                int randomIndex = random.nextInt(booksSize);
                recommendedBooks.add(books.get(randomIndex));
                books.remove(randomIndex); // Remove selected book to avoid duplicates
                booksSize = books.size();
            }
        }

        return recommendedBooks;
    }

    private int parseYear(String yearString) {
        try {
            return Integer.parseInt(yearString.trim());
        } catch (NumberFormatException e) {
            Log.e("BookRecommendActivity", "Invalid year format: " + yearString, e);
            return -1; // Return -1 to indicate an invalid year
        }
    }
}
