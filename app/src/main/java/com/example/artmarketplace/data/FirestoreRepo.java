package com.example.artmarketplace.data;

import com.example.artmarketplace.model.ArtItem;
import com.example.artmarketplace.model.Order;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

/**
 * Abstraction over Firestore collections used by the app.
 */
public class FirestoreRepo {

    private static final String COLLECTION_ART = "art";
    private static final String COLLECTION_ORDERS = "orders";

    private final FirebaseFirestore firestore;

    public FirestoreRepo() {
        this(FirebaseFirestore.getInstance());
    }

    public FirestoreRepo(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Creates a new art listing document.
     *
     * @param artItem populated art item to persist
     * @return task resolving to the created document reference
     */
    public Task<DocumentReference> createArt(ArtItem artItem) {
        return firestore.collection(COLLECTION_ART).add(artItem);
    }

    /**
     * Fetches all available art listings ordered by recency.
     *
     * @return query task producing the latest snapshot of art documents
     */
    public Task<QuerySnapshot> listArt() {
        return firestore.collection(COLLECTION_ART)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Fetches art listings owned by the supplied provider uid.
     *
     * @param providerUid provider identifier tied to the authenticated user
     * @return query task returning provider-specific art sorted by creation time
     */
    public Task<QuerySnapshot> listArtByProvider(String providerUid) {
        return firestore.collection(COLLECTION_ART)
                .whereEqualTo("providerUid", providerUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Updates an existing art listing using the provided payload.
     *
     * @param artId   Firestore document id
     * @param artItem updated values to merge with the existing document
     * @return task representing completion of the write
     */
    public Task<Void> updateArt(String artId, ArtItem artItem) {
        return firestore.collection(COLLECTION_ART)
                .document(artId)
                .set(artItem, SetOptions.merge());
    }

    /**
     * Deletes the targeted art listing document.
     *
     * @param artId Firestore document id
     * @return task signaling deletion completion
     */
    public Task<Void> deleteArt(String artId) {
        return firestore.collection(COLLECTION_ART)
                .document(artId)
                .delete();
    }

    /**
     * Persists a new order document.
     *
     * @param order order details to create in Firestore
     * @return task resolving to the created document reference
     */
    public Task<DocumentReference> createOrder(Order order) {
        return firestore.collection(COLLECTION_ORDERS).add(order);
    }

    /**
     * Updates the status field of an order while preserving other data.
     *
     * @param orderId Firestore document id
     * @param status  new status value to set
     * @return task representing completion of the update
     */
    public Task<Void> setOrderStatus(String orderId, String status) {
        return firestore.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update("status", status);
    }

    /**
     * Lists all orders created by the specified buyer.
     *
     * @param buyerUid authenticated buyer uid
     * @return query task returning orders sorted by creation time descending
     */
    public Task<QuerySnapshot> listOrdersByBuyer(String buyerUid) {
        return firestore.collection(COLLECTION_ORDERS)
                .whereEqualTo("buyerUid", buyerUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lists all orders addressed to the specified provider.
     *
     * @param providerUid authenticated provider uid
     * @return query task returning provider orders sorted by creation time descending
     */
    public Task<QuerySnapshot> listOrdersByProvider(String providerUid) {
        return firestore.collection(COLLECTION_ORDERS)
                .whereEqualTo("providerUid", providerUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }
}
