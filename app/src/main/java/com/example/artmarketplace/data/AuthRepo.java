package com.example.artmarketplace.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Wrapper around FirebaseAuth exposing simple sign-in helpers.
 */
public class AuthRepo {

    private final FirebaseAuth firebaseAuth;

    public AuthRepo() {
        this(FirebaseAuth.getInstance());
    }

    public AuthRepo(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public Task<AuthResult> signInAnonymously() {
        return firebaseAuth.signInAnonymously();
    }

    public Task<AuthResult> signInWithEmail(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}
