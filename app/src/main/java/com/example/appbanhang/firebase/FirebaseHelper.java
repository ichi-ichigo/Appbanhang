package com.example.appbanhang.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mFirestore;
    private static FirebaseStorage mStorage;

    public static FirebaseAuth getAuth() {
        if (mAuth == null) { mAuth = FirebaseAuth.getInstance(); }
        return mAuth;
    }

    public static FirebaseFirestore getFirestore() {
        if (mFirestore == null) { mFirestore = FirebaseFirestore.getInstance(); }
        return mFirestore;
    }

    public static FirebaseStorage getStorage() {
        if (mStorage == null) { mStorage = FirebaseStorage.getInstance(); }
        return mStorage;
    }
}