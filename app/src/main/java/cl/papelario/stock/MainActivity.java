package cl.papelario.stock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

public final class MainActivity extends Activity {
    private final int tinta = 0xff472637, acento = 0xff863957;
    private FirebaseAuth auth;
    private ProductoRepositorio repo;
    private ListenerRegistration escucha;
    private LinearLayout pagina, filas;
    private TextView estado;
    private AlertDialog dialogo;
    private String uid;
    private boolean registro;
    private final FirebaseAuth.AuthStateListener sesion = servicio -> {
        FirebaseUser cuenta = servicio.getCurrentUser();
        detenerEscucha();
        if (dialogo != null) dialogo.dismiss();
        if (cuenta == null) {
            uid = null;
            repo = null;
            acceso(false);
        } else {
            uid = cuenta.getUid();
            repo = new ProductoRepositorio(uid);
            inventario(cuenta.getEmail());
        }
    };

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        auth = FirebaseAuth.getInstance();
    }
    @Override protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(sesion);
    }
    @Override protected void onStop() {
        auth.removeAuthStateListener(sesion);
        detenerEscucha();
        super.onStop();
    }
    private void detenerEscucha() {
        if (escucha != null) escucha.remove();
        escucha = null;
    }
    private int dp(int valor) {
        return Math.round(getResources().getDisplayMetrics().density * valor);
    }
    private LinearLayout columna(int margen) {
        LinearLayout bloque = new LinearLayout(this);
        bloque.setOrientation(LinearLayout.VERTICAL);
        bloque.setPadding(dp(margen), dp(margen), dp(margen), dp(margen));
        return bloque;
    }
    private void pantalla(String titulo) {
        ScrollView scroll = new ScrollView(this);
        pagina = columna(20);
        scroll.addView(pagina);
        setContentView(scroll);
        texto(pagina, "P A P E L A R I O", 14);
        TextView h = texto(pagina, titulo, 32);
        h.setTypeface(Typeface.SERIF, Typeface.BOLD);
    }
    private TextView texto(LinearLayout padre, String valor, int size) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(size);
        t.setTextColor(tinta);
        t.setPadding(0, dp(5), 0, dp(5));
        padre.addView(t);
        return t;
    }
    private Button boton(LinearLayout padre, String valor, boolean principal, Runnable accion) {
        Button b = new Button(this);
        b.setText(valor);
        b.setAllCaps(false);
        b.setTextColor(principal ? -1 : tinta);
        b.setBackgroundTintList(ColorStateList.valueOf(principal ? acento : 0xffffe4eb));
        b.setOnClickListener(v -> accion.run());
        padre.addView(b);
        return b;
    }
    private EditText campo(LinearLayout padre, String etiqueta, int tipo, String valor) {
        TextView label = texto(padre, etiqueta, 15);
        EditText e = new EditText(this);
        e.setId(View.generateViewId());
        label.setLabelFor(e.getId());
        e.setSingleLine();
        e.setInputType(tipo);
        e.setText(valor);
        padre.addView(e);
        return e;
    }
    private String leer(EditText e) { return e.getText().toString().trim(); }
    private void error(EditText e, String mensaje) { e.setError(mensaje); e.requestFocus(); }
    private void aviso(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    private void acceso(boolean nueva) {
        registro = nueva;
        pantalla(nueva ? "Crear cuenta" : "Mi inventario");
        texto(pagina, nueva ? "Registra tu correo para abrir tu inventario." :
                "Ingresa para consultar el stock de tu papelería.", 17);
        EditText correo = campo(pagina, "Correo electrónico", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, "");
        EditText clave = campo(pagina, "Contraseña", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, "");
        clave.setSaveEnabled(false);
        Button enviar = new Button(this);
        enviar.setText(nueva ? "Registrar cuenta" : "Iniciar sesión");
        pagina.addView(enviar);
        Button cambiar = boton(pagina, nueva ? "Ya tengo cuenta" : "Crear una cuenta",
                false, () -> acceso(!nueva));
        TextView resultado = texto(pagina, "", 16);
        resultado.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        enviar.setOnClickListener(v -> {
            String email = leer(correo), password = clave.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                error(correo, "Ingresa un correo válido."); return;
            }
            if (password.isEmpty() || (nueva && password.length() < 6)) {
                error(clave, nueva ? "Usa al menos 6 caracteres." : "Ingresa tu contraseña."); return;
            }
            enviar.setEnabled(false);
            cambiar.setEnabled(false);
            resultado.setText("Conectando…");
            Task<AuthResult> tarea = nueva ? auth.createUserWithEmailAndPassword(email, password)
                    : auth.signInWithEmailAndPassword(email, password);
            tarea.addOnCompleteListener(this, t -> {
                if (t.isSuccessful()) { aviso(nueva ? "Cuenta creada" : "Sesión iniciada"); return; }
                enviar.setEnabled(true);
                cambiar.setEnabled(true);
                resultado.setText(nueva ? "No se pudo registrar. Revisa el correo, la política de contraseña y la conexión."
                        : "No se pudo ingresar. Revisa tus credenciales y la conexión.");
            });
        });
    }

    private void inventario(String email) {
        pantalla("Mis productos");
        texto(pagina, email, 16);
        boton(pagina, "Agregar producto", true, () -> formulario(null));
        boton(pagina, "Cerrar sesión", false, () -> auth.signOut());
        estado = texto(pagina, "Consultando inventario…", 16);
        filas = columna(0);
        pagina.addView(filas);
        final ProductoRepositorio origen = repo;
        escucha = repo.observar((snap, fallo) -> {
            if (repo != origen || auth.getCurrentUser() == null) return;
            filas.removeAllViews();
            if (fallo != null || snap == null) {
                estado.setText("No se pudo leer el inventario. Revisa la conexión y las reglas.");
                return;
            }
            String fuente = snap.getMetadata().hasPendingWrites() ? "Cambios pendientes" :
                    snap.getMetadata().isFromCache() ? "Datos locales" : "Confirmado en la nube";
            estado.setText(snap.size() + " productos · " + fuente);
            if (snap.isEmpty()) texto(filas, "Agrega tu primer producto para comenzar.", 17);
            for (DocumentSnapshot p : snap.getDocuments()) tarjeta(p);
        });
    }

    private void tarjeta(DocumentSnapshot p) {
        LinearLayout card = columna(12);
        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(0xffffffff);
        fondo.setCornerRadius(dp(14));
        fondo.setStroke(dp(1), 0xffead6dc);
        card.setBackground(fondo);
        LinearLayout.LayoutParams medidas = new LinearLayout.LayoutParams(-1, -2);
        medidas.topMargin = dp(8);
        filas.addView(card, medidas);
        texto(card, p.getString("nombre"), 22).setTypeface(Typeface.SERIF, Typeface.BOLD);
        texto(card, p.getString("categoria") + "   ·   " + p.getLong("stock") + " unidades", 16);
        LinearLayout acciones = new LinearLayout(this);
        card.addView(acciones);
        boton(acciones, "Editar", false, () -> formulario(p));
        boton(acciones, "Eliminar", false, () -> {
            dialogo = new AlertDialog.Builder(this).setTitle("Eliminar producto")
                    .setMessage("¿Eliminar “" + p.getString("nombre") + "” del inventario?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Eliminar", (d, w) -> informar(repo.eliminar(p.getId()), "Producto eliminado"))
                    .show();
        });
    }

    private void informar(Task<Void> tarea, String exito) {
        final String propietario = uid;
        aviso("Esperando confirmación de Firestore…");
        tarea.addOnCompleteListener(this, t -> {
            if (propietario.equals(uid)) aviso(t.isSuccessful() ? exito :
                    "Firestore rechazó la operación. Revisa las reglas y vuelve a intentarlo.");
        });
    }

    private void formulario(DocumentSnapshot p) {
        LinearLayout campos = columna(18);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(campos);
        EditText nombre = campo(campos, "Nombre del producto", InputType.TYPE_CLASS_TEXT, p == null ? "" : p.getString("nombre"));
        EditText categoria = campo(campos, "Categoría", InputType.TYPE_CLASS_TEXT, p == null ? "" : p.getString("categoria"));
        EditText stock = campo(campos, "Unidades disponibles", InputType.TYPE_CLASS_NUMBER,
                p == null ? "" : String.valueOf(p.getLong("stock")));
        dialogo = new AlertDialog.Builder(this).setTitle(p == null ? "Nuevo producto" : "Editar producto")
                .setView(scroll).setNegativeButton("Cancelar", null).setPositiveButton("Guardar", null).show();
        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String n = leer(nombre), c = leer(categoria), unidades = leer(stock);
            if (n.isEmpty() || n.length() > 60) { error(nombre, "Escribe entre 1 y 60 caracteres."); return; }
            if (c.isEmpty() || c.length() > 35) { error(categoria, "Escribe entre 1 y 35 caracteres."); return; }
            if (!unidades.matches("[0-9]{1,5}")) { error(stock, "Ingresa un entero entre 0 y 99999."); return; }
            informar(repo.guardar(p == null ? null : p.getId(), n, c, Long.parseLong(unidades)),
                    p == null ? "Producto creado" : "Producto actualizado");
            dialogo.dismiss();
        });
    }
    @Override public void onBackPressed() {
        if (uid == null && registro) acceso(false);
        else super.onBackPressed();
    }
}
