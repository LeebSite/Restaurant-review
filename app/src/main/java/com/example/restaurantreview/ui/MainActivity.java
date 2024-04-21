package com.example.restaurantreview.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.example.restaurantreview.R;
import com.example.restaurantreview.data.retrofit.ApiConfig;
import com.example.restaurantreview.data.retrofit.PostReviewResponse;
import com.example.restaurantreview.data.retrofit.Restaurant;
import com.example.restaurantreview.data.retrofit.RestaurantResponse;
import com.example.restaurantreview.databinding.ActivityMainBinding;
import com.example.restaurantreview.data.retrofit.CustomerReviewsItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final String RESTAURANT_ID = "uewq1zg2zlskfw1e867";
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvReview.setLayoutManager(layoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        binding.rvReview.addItemDecoration(itemDecoration);

        adapter = new ReviewAdapter(new ArrayList<>());
        binding.rvReview.setAdapter(adapter);

        findRestaurant();

        binding.btnSend.setOnClickListener(v -> {
            if (binding.edReview.getText() != null) {
                postReview(binding.edReview.getText().toString());
                binding.edReview.setText(""); // Clear input after adding review
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
        });
    }

    // Metode untuk menambahkan review ke RecyclerView
    private void addReview(String reviewText) {
        adapter.addReview(reviewText);
    }
    private void postReview(String review){
        showLoading(true);
        Call<PostReviewResponse> client = ApiConfig.getApiService().postReview(RESTAURANT_ID,"Leeb", review);
        client.enqueue(new Callback<PostReviewResponse>() {
            @Override
            public void onResponse(@NotNull Call<PostReviewResponse> call, @NotNull Response<PostReviewResponse> response) {
                showLoading(false);
                if(response.isSuccessful()){
                    if(response.body()!= null){
                        setReviewData(response.body().getCustomerReviews());
                    }
                } else {
                    if (response.errorBody() != null) {
                        Log.e(TAG, "onFailure: " + response.errorBody().toString());
                    }
                }
            }
            @Override
            public void onFailure(@NotNull Call<PostReviewResponse> call,@NotNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void findRestaurant() {
        showLoading(true);
        Call<RestaurantResponse> client = ApiConfig.getApiService().getRestaurant(RESTAURANT_ID);
        client.enqueue(new Callback<RestaurantResponse>() {
            @Override
            public void onResponse(@NotNull Call<RestaurantResponse> call, @NotNull Response<RestaurantResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        setRestaurantData(response.body().getRestaurant());
                        setReviewData(response.body().getRestaurant().getCustomerReviews());
                    } else {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "onResponse errorBody: " + response.errorBody().toString());
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<RestaurantResponse> call, @NotNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
    private void setRestaurantData(Restaurant restaurant) {
        binding.tvTitle.setText(restaurant.getName());
        binding.tvDescription.setText(restaurant.getDescription());

        Glide.with(MainActivity.this).load("https://restaurant-api.dicoding.dev/images/large/" + restaurant.getPictureId()).into(binding.ivPicture);
    }

    private void setReviewData(List<CustomerReviewsItem> customerReviews) {
        ArrayList<String> listReview = new ArrayList<>();
        for (CustomerReviewsItem review : customerReviews) {
            listReview.add(review.getReview() + "\n- " + review.getName());
        }
        adapter = new ReviewAdapter(listReview);
        binding.rvReview.setAdapter(adapter);
    }
    private void addReview(CustomerReviewsItem review) {
        String newReview = review.getReview() + "\n- " + review.getName();
        adapter.addReview(newReview);
    }

    private void showLoading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }
}
