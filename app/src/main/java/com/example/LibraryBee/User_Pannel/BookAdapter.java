package com.example.LibraryBee.User_Pannel;// BookAdapter.java
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.LibraryBee.R;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final List<Book> books;

    public BookAdapter(List<Book> books) {
        this.books = books == null? new ArrayList<>() : books;
    }
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView authorTextView;
        public final TextView yearTextView;
        public final ImageView imageView;

        public BookViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.book_title);
            authorTextView = itemView.findViewById(R.id.book_author);
            yearTextView = itemView.findViewById(R.id.book_year);
            imageView = itemView.findViewById(R.id.book_image);
        }
    }

    @Override
    public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.titleTextView.setText(book.getTitle());
        holder.authorTextView.setText(book.getAuthor());
        holder.yearTextView.setText(String.valueOf(book.getYear()));
        if (book.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(book.getImageUrl())
                    .error(R.drawable.bookdef)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // Handle the error here
                            Log.e("Glide", "Error loading image", e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(holder.imageView);
        } else {
            // Handle the case when the image URL is null
            holder.imageView.setImageResource(R.drawable.bookdef);
        }
    }

    @Override
    public int getItemCount() {
        return books == null? 0 : books.size();
    }

}

