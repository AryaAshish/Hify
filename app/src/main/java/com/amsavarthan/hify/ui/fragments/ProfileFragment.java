package com.amsavarthan.hify.ui.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.amsavarthan.hify.utils.CardAdapter;
import com.amsavarthan.hify.ui.activities.ImagePreviewSave;
import com.amsavarthan.hify.ui.activities.LoginActivity;
import com.amsavarthan.hify.ui.activities.ProfileEdit;
import com.amsavarthan.hify.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.amsavarthan.hify.ui.activities.MainActivity.userId;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private Button logoutBtn,updateBtn;
    private View view;
    private FirebaseAuth mAuth;
    private CircleImageView profile_pic;
    private TextView userName;
    private FirebaseFirestore firebaseFirestore;
    private String imageUri;
    private String name;
    private CardView mCardView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public CardView getCardView() {
        return mCardView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_profile, container, false);

        if(view.getContext()!=null) {
            mAuth = FirebaseAuth.getInstance();
            firebaseFirestore = FirebaseFirestore.getInstance();

            logoutBtn = (Button) view.findViewById(R.id.logout);
            updateBtn = (Button) view.findViewById(R.id.update);
            profile_pic = (CircleImageView) view.findViewById(R.id.profile_pic);
            userName = (TextView) view.findViewById(R.id.username);

            mCardView = (CardView) view.findViewById(R.id.cardView);
            mCardView.setMaxCardElevation(mCardView.getCardElevation()
                    * CardAdapter.MAX_ELEVATION_FACTOR);

            mCardView.setVisibility(View.VISIBLE);
            mCardView.setAlpha(0.0f);
            mCardView.animate()
                    .translationY(mCardView.getHeight())
                    .alpha(1.0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            try {
                                firebaseFirestore.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        name = documentSnapshot.getString("name");
                                        imageUri = documentSnapshot.getString("image");

                                        RequestOptions placeholderOprions = new RequestOptions();
                                        placeholderOprions.placeholder(getResources().getDrawable(R.mipmap.profile_black));

                                        userName.setText(name);

                                        Glide.with(container.getContext())
                                                .setDefaultRequestOptions(placeholderOprions)
                                                .load(Uri.parse(imageUri))
                                                .into(profile_pic);

                                    }
                                });
                            } catch (Exception e) {
                                Log.e("Error:", e.getLocalizedMessage());
                            }

                        }
                    });


            profile_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ImagePreviewSave.class)
                            .putExtra("url", imageUri)
                            .putExtra("uri", "")
                            .putExtra("sender_name", name);
                    startActivity(intent);
                }
            });

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateClick(view);
                }
            });


            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Map<String, Object> tokenMapRemove = new HashMap<>();
                    tokenMapRemove.put("token_id", "");

                    firebaseFirestore.collection("Users").document(userId).update(tokenMapRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mAuth.signOut();
                            LoginActivity.startActivity(container.getContext());
                            getActivity().finish();
                        }
                    });


                }
            });
        }

        return view;
    }

    public void updateClick(View view) {
        ProfileEdit.startActivity(view.getContext());
    }
}
