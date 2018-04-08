package com.amsavarthan.hify.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.hify.R;
import com.amsavarthan.hify.models.Post;
import com.amsavarthan.hify.ui.activities.CommentsActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.like.LikeButton;
import com.like.OnAnimationEndListener;
import com.like.OnLikeListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;

    public PostsAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_post, parent, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {


        setupViews(holder, holder.getAdapterPosition());

        holder.like_btn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(final LikeButton likeButton) {

                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put("liked", true);

                mFirestore.collection("Posts")
                        .document(postList.get(holder.getAdapterPosition()).getUserId())
                        .collection("All Posts")
                        .document(postList.get(holder.getAdapterPosition()).postId)
                        .collection("Liked_Users")
                        .document(mCurrentUser.getUid())
                        .set(likeMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Liked post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error like", e.getMessage());
                            }
                        });

            }

            @Override
            public void unLiked(LikeButton likeButton) {

                Map<String, Object> likeMap = new HashMap<>();
                likeMap.put("liked", false);

                mFirestore.collection("Posts")
                        .document(postList.get(holder.getAdapterPosition()).getUserId())
                        .collection("All Posts")
                        .document(postList.get(holder.getAdapterPosition()).postId)
                        .collection("Liked_Users")
                        .document(mCurrentUser.getUid())
                        .set(likeMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "Unliked post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error unlike", e.getMessage());
                            }
                        });

            }
        });

        holder.fav_btn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                Map<String, Object> favMap = new HashMap<>();
                favMap.put("favourited", true);

                mFirestore.collection("Posts")
                        .document(postList.get(holder.getAdapterPosition()).getUserId())
                        .collection("All Posts")
                        .document(postList.get(holder.getAdapterPosition()).postId)
                        .collection("Favourited_Users")
                        .document(mCurrentUser.getUid())
                        .set(favMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("userId", postList.get(holder.getAdapterPosition()).getUserId());
                                postMap.put("timestamp", postList.get(holder.getAdapterPosition()).getTimestamp());
                                postMap.put("image", postList.get(holder.getAdapterPosition()).getImage());
                                postMap.put("description", postList.get(holder.getAdapterPosition()).getDescription());
                                postMap.put("color", postList.get(holder.getAdapterPosition()).getColor());

                                mFirestore.collection("Users")
                                        .document(mCurrentUser.getUid())
                                        .collection("Favourites")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .set(postMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Added to favourites, post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error add fav", e.getMessage());
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error fav", e.getMessage());
                            }
                        });

            }

            @Override
            public void unLiked(LikeButton likeButton) {

                Map<String, Object> favMap = new HashMap<>();
                favMap.put("favourited", false);

                mFirestore.collection("Posts")
                        .document(postList.get(holder.getAdapterPosition()).getUserId())
                        .collection("All Posts")
                        .document(postList.get(holder.getAdapterPosition()).postId)
                        .collection("Favourited_Users")
                        .document(mCurrentUser.getUid())
                        .set(favMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mFirestore.collection("Users")
                                        .document(mCurrentUser.getUid())
                                        .collection("Favourites")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Removed from favourites, post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Error remove fav", e.getMessage());
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error fav", e.getMessage());
                            }
                        });
            }
        });

        holder.love_btn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                Map<String, Object> favMap = new HashMap<>();
                favMap.put("loved", true);

                mFirestore.collection("Posts")
                        .document(postList.get(holder.getAdapterPosition()).getUserId())
                        .collection("All Posts")
                        .document(postList.get(holder.getAdapterPosition()).postId)
                        .collection("Loved_Users")
                        .document(mCurrentUser.getUid())
                        .set(favMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("userId", postList.get(holder.getAdapterPosition()).getUserId());
                                postMap.put("timestamp", postList.get(holder.getAdapterPosition()).getTimestamp());
                                postMap.put("image", postList.get(holder.getAdapterPosition()).getImage());
                                postMap.put("description", postList.get(holder.getAdapterPosition()).getDescription());
                                postMap.put("color", postList.get(holder.getAdapterPosition()).getColor());

                                mFirestore.collection("Users")
                                        .document(mCurrentUser.getUid())
                                        .collection("Loved")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .set(postMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Added to loved, post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error add luv", e.getMessage());
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error luv", e.getMessage());
                            }
                        });

            }

            @Override
            public void unLiked(LikeButton likeButton) {

                Map<String, Object> favMap = new HashMap<>();
                favMap.put("loved", false);

                mFirestore.collection("Posts")
                        .document(postList.get(holder.getAdapterPosition()).getUserId())
                        .collection("All Posts")
                        .document(postList.get(holder.getAdapterPosition()).postId)
                        .collection("Loved_Users")
                        .document(mCurrentUser.getUid())
                        .set(favMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                mFirestore.collection("Users")
                                        .document(mCurrentUser.getUid())
                                        .collection("Loved")
                                        .document(postList.get(holder.getAdapterPosition()).postId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(context, "Removed from loved, post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Error remove luv", e.getMessage());
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Error luv", e.getMessage());
                            }
                        });
            }
        });

        holder.comment_btn.setOnAnimationEndListener(new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(LikeButton likeButton) {
                holder.comment_btn.setLiked(true);
                CommentsActivity.startActivity(context, postList, holder.getAdapterPosition(), "", "");

            }
        });

        holder.share_btn.setOnAnimationEndListener(new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(LikeButton likeButton) {
                if (postList.get(holder.getAdapterPosition()).getImage().equals("no_image")) {

                    Intent intent = new Intent(Intent.ACTION_SEND)
                            .setType("image/*");
                    //ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(getBitmap(holder.mImageholder), holder, "hify_user_" + postList.get(holder.getAdapterPosition()).getUserId()));
                    try {
                        context.startActivity(Intent.createChooser(intent, "Share using..."));
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {

                    Intent intent = new Intent(Intent.ACTION_SEND)
                            .setType("image/*");
                    //ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(getBitmap(holder.post_image), holder, "hify_user_" + postList.get(holder.getAdapterPosition()).getUserId()));
                    try {
                        context.startActivity(Intent.createChooser(intent, "Share using..."));
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mFirestore.collection("Users")
                .document(postList.get(position).getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {

                        holder.user_name.setText(documentSnapshot.getString("name"));

                        Glide.with(context)
                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                .load(documentSnapshot.getString("image"))
                                .into(holder.user_image);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                });

    }

    private Uri getBitmapUri(Bitmap bitmap, ViewHolder holder, String name) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, postList.get(holder.getAdapterPosition()).postId, "Post by " + name);
        return Uri.parse(path);
    }

    private Bitmap getBitmap(FrameLayout view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#212121"));
        }
        view.draw(canvas);

        return bitmap;
    }

    private Bitmap getBitmap(ImageView view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#212121"));
        }
        view.draw(canvas);

        return bitmap;
    }

    private void setupViews(final ViewHolder holder, int position) {

        holder.user_name.setText("Username");

        //String converted_epoch=new SimpleDateFormat("E, dd MMMM yyyy HH:mm a",Locale.ENGLISH).format(new Date(Long.parseLong(postList.get(position).getTimestamp())));

        String timeAgo = TimeAgo.using(Long.parseLong(postList.get(position).getTimestamp()));
        holder.timestamp.setText(String.format("Posted %s", timeAgo));

        holder.mReactLayout.setVisibility(View.VISIBLE);
        holder.mReactLayout.setAlpha(0.0f);

        holder.mReactLayout.animate()
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationEnd(animation);
                        getLikeandFav(holder);
                    }
                }).start();

        getLikeandFav(holder);

        if (postList.get(position).getImage().equals("no_image")) {

            holder.mCard.setVisibility(View.VISIBLE);
            setmImageHolderBg(holder.getAdapterPosition(), holder.mImageholder);
            holder.post_text.setVisibility(View.VISIBLE);
            holder.post_text.setText(postList.get(holder.getAdapterPosition()).getDescription());

        } else {

            holder.mCard.setVisibility(View.VISIBLE);

            holder.post_image.setVisibility(View.VISIBLE);
            holder.post_desc.setVisibility(View.VISIBLE);
            holder.post_desc.setText(postList.get(holder.getAdapterPosition()).getDescription());

            Glide.with(context)
                    .load(postList.get(holder.getAdapterPosition()).getImage())
                    .listener(new RequestListener<Drawable>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                      holder.mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_10));
                                      if (e != null) {
                                          Log.e("Post Error", e.getMessage());
                                      }
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                      holder.post_image.setImageDrawable(resource);
                                      return true;
                                  }
                              }
                    ).into(holder.post_image);

        }
    }

    private void getLikeandFav(final ViewHolder holder) {

        //forLiked
        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).getUserId())
                .collection("All Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Liked_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            boolean liked = documentSnapshot.getBoolean("liked");
                            if (liked) {
                                holder.like_btn.setLiked(true);
                            } else {
                                holder.like_btn.setLiked(false);
                            }
                        } else {
                            Log.e("Like", "No document found");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Like", e.getMessage());
                    }
                });

        //forFavourited
        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).getUserId())
                .collection("All Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Favourited_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            boolean fav = documentSnapshot.getBoolean("favourited");
                            if (fav) {
                                holder.fav_btn.setLiked(true);
                            } else {
                                holder.fav_btn.setLiked(false);
                            }
                        } else {
                            Log.e("Fav", "No document found");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Fav", e.getMessage());
                    }
                });

        //forLoved
        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).getUserId())
                .collection("All Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Loved_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            boolean fav = documentSnapshot.getBoolean("loved");
                            if (fav) {
                                holder.love_btn.setLiked(true);
                            } else {
                                holder.love_btn.setLiked(false);
                            }
                        } else {
                            Log.e("Luv", "No document found");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Luv", e.getMessage());
                    }
                });

    }

    private void setmImageHolderBg(int position, FrameLayout mImageholder) {

        switch (postList.get(position).getColor()) {

            case "1":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_9));
                return;

            case "2":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_7));
                return;

            case "3":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_8));
                return;

            case "4":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_4));
                return;

            case "5":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_1));
                return;

            case "6":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_3));
                return;

            case "7":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_2));
                return;

            case "8":
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_11));
                return;

            default:
                mImageholder.setBackground(context.getResources().getDrawable(R.drawable.gradient_2));
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private LinearLayout mReactLayout;
        private CircleImageView user_image;
        private TextView user_name, timestamp, post_desc, likes, favs, post_text;
        private ImageView post_image;
        private LikeButton love_btn, fav_btn, like_btn, share_btn, comment_btn;
        private FrameLayout mImageholder;
        private CardView mCard;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            user_image = mView.findViewById(R.id.post_user_image);
            user_name = mView.findViewById(R.id.post_username);
            timestamp = mView.findViewById(R.id.post_timestamp);
            post_desc = mView.findViewById(R.id.post_desc);
            post_image = mView.findViewById(R.id.post_image);
            post_text = mView.findViewById(R.id.post_text);
            like_btn = mView.findViewById(R.id.thumb_button);
            love_btn = mView.findViewById(R.id.like_button);
            comment_btn = mView.findViewById(R.id.comment_button);
            share_btn = mView.findViewById(R.id.share_button);
            fav_btn = mView.findViewById(R.id.favourites_button);
            mImageholder = mView.findViewById(R.id.image_holder);
            mCard = mView.findViewById(R.id.card_image);
            mReactLayout = mView.findViewById(R.id.react_layout);

        }
    }

}
