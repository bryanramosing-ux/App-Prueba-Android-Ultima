# Conectar Papelario con Firebase

Necesitas una cuenta de Google, Android Studio y conexión a internet para descargar Gradle y los SDK.

1. **Crea un proyecto.** Entra en [Firebase Console](https://console.firebase.google.com/), elige crear un proyecto y dale un nombre. Analytics es opcional.
2. **Registra la aplicación.** En la descripción general del proyecto selecciona Android. Introduce exactamente `cl.papelario.stock`, el applicationId definido en `app/build.gradle`. Puedes omitir SHA-1 para correo y contraseña.
3. **Conecta tu copia local.** Descarga `google-services.json` de esa aplicación y cópialo a `app/google-services.json`, junto al Gradle del módulo. El repositorio ya declara el plugin y los SDK. Git excluye el archivo de configuración.
4. **Activa las cuentas.** Abre Authentication → Comenzar → Método de acceso. Habilita Correo electrónico/contraseña y guarda. El primer usuario se registra desde el teléfono.
5. **Crea Firestore.** Abre Firestore Database → Crear base de datos. Selecciona Standard, conserva el ID `(default)`, escoge la ubicación y empieza en modo de producción.
6. **Publica las reglas.** Abre [firestore.rules](../firestore.rules). En un proyecto nuevo, copia todo el archivo a Firestore Database → Reglas y pulsa Publicar. En un proyecto compartido integra el bloque con las reglas existentes y revisa que ninguna regla más amplia conceda acceso. Las reglas restringen los productos al UID autenticado y validan los campos.
7. **Ejecuta Android.** Abre la carpeta que contiene `settings.gradle`. Configura JDK 17 para Gradle e instala Android SDK Platform 35. Sincroniza, elige el módulo app y pulsa Run en un emulador o teléfono Android 6 o superior con internet.
8. **Verifica Authentication.** Registra una cuenta de prueba. Busca su correo y UID en Authentication → Usuarios. Cierra sesión y comprueba que puedes ingresar otra vez.
9. **Verifica Firestore.** Guarda «Regla metálica», categoría «Accesorios», stock 5. Espera «Confirmado en la nube». En Firestore → Datos abre `usuarios/{uid}/productos/{id}` y revisa los tres campos. La primera escritura crea el documento y la subcolección.
10. **Comprueba el aislamiento.** Agrega los demás productos del README, edita uno y reabre la app. Registra una segunda cuenta y comprueba que su lista está vacía. En el simulador de reglas prueba una lectura del documento de la primera cuenta con el UID de la segunda: debe denegarse.

## Si algo falla

| Síntoma | Qué comprobar |
|---|---|
| Falta google-services.json | Revisa la ubicación y que el archivo conserve el nombre exacto. |
| No matching client found | Descarga el JSON de la aplicación registrada con el paquete cl.papelario.stock. |
| Puedes ingresar pero falla el inventario | Comprueba la base (default), las reglas publicadas y que el JSON sea del mismo proyecto. |
| Cambios pendientes | Verifica internet en el teléfono y espera la confirmación de la nube. |

Documentación oficial de Firebase: [Android](https://firebase.google.com/docs/android/setup), [Authentication](https://firebase.google.com/docs/auth/android/password-auth) y [Firestore](https://firebase.google.com/docs/firestore/quickstart).
