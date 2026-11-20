package cl.papelario.stock;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

final class ProductoRepositorio {
    private final CollectionReference productos;

    ProductoRepositorio(String uid) {
        productos = FirebaseFirestore.getInstance().collection("usuarios")
                .document(uid).collection("productos");
    }

    ListenerRegistration observar(EventListener<QuerySnapshot> listener) {
        return productos.orderBy("nombre").addSnapshotListener(MetadataChanges.INCLUDE, listener);
    }

    Task<Void> guardar(String id, String nombre, String categoria, long stock) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("categoria", categoria);
        datos.put("stock", stock);
        if (id == null) return productos.document().set(datos);
        return productos.document(id).update(datos);
    }

    Task<Void> eliminar(String id) {
        return productos.document(id).delete();
    }
}
