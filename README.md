# Papelario · Inventario personal de papelería

Una persona encargada de una papelería puede contar existencias y corregirlas desde su teléfono. Tras registrarse o ingresar, ve sus productos ordenados por nombre. Cada tarjeta permite editar y eliminar. El formulario solicita nombre (1–60 caracteres), categoría (1–35) y stock (entero de 0 a 99999). El identificador lo genera Firestore.

## Primera ejecución en Android Studio

1. Clona este repositorio o descarga y descomprime su ZIP. Abre en Android Studio la carpeta que contiene settings.gradle.
2. En https://console.firebase.google.com crea un proyecto para esta actividad. Registra una aplicación Android con el paquete exacto cl.papelario.stock. Descarga google-services.json y colócalo en app/google-services.json, junto a app/build.gradle.
3. En Authentication → Método de acceso habilita Correo electrónico/contraseña. Puedes dejar desactivado el acceso por enlace de correo.
4. En Firestore Database crea la base (default), elige su ubicación y el modo de producción. En la pestaña Reglas sustituye el contenido del proyecto nuevo por firestore.rules y pulsa Publicar. Si el proyecto ya tiene otras aplicaciones, integra el bloque de productos en sus reglas existentes.
5. En Android Studio utiliza JDK 17 para Gradle e instala Android SDK Platform 35. Sincroniza Gradle con conexión a Internet. Selecciona app y ejecuta ▶ en un teléfono o emulador con Android 6 o superior.
6. Pulsa Crear una cuenta. Tras el registro aparecerá la lista. Agrega un producto y espera «Confirmado en la nube». En la consola abre usuarios → UID → productos. El documento padre del UID puede aparecer en cursiva: los datos están en su subcolección.
7. Si la compilación indica que falta google-services.json, comprueba su ubicación y nombre. «No matching client found» indica que el paquete registrado difiere. Si puedes iniciar sesión pero falla la lectura, revisa la base (default), el proyecto del JSON y la publicación de las reglas.

## Datos para ingresar durante la prueba

| Nombre | Categoría | Stock |
| --- | --- | ---: |
| Cuaderno punteado | Cuadernos | 12 |
| Lápiz gel rosa | Escritura | 28 |
| Notas adhesivas | Organización | 9 |
| Regla metálica | Accesorios | 5 |
| Washi floral | Cintas | 16 |

## Demostración y capturas

1. Registra la cuenta A. Captura la pantalla de registro con los campos vacíos y la lista tras ingresar.
2. Ingresa los cinco productos de la tabla. Espera confirmación de la nube. Captura la lista (desplázate para mostrar los cinco) y la consola con cinco identificadores y un producto seleccionado mostrando sus tres campos.
3. Cambia el stock de Regla metálica de 5 a 7. Comprueba el cambio también en Firestore. Crea un sexto producto «Prueba», elimínalo y muestra que quedan cinco. Intenta guardar un nombre vacío y comprueba el mensaje del formulario.
4. Cierra y vuelve a abrir la app, espera «Confirmado en la nube» y comprueba las siete reglas metálicas. Cierra sesión. Ingresa de nuevo con A para demostrar el login independiente del registro.
5. Cierra sesión y registra B. Su inventario empieza vacío. En Authentication → Usuarios verifica ambas cuentas. Captura únicamente filas de prueba y oculta datos de terceros. En el simulador de reglas prueba una lectura a /usuarios/UID_A/productos/ID_REAL como UID_A (permitida), como UID_B (denegada) y sin autenticar (denegada).
6. Añade las capturas reales a docs/evidencias y vuelve a comprimir el proyecto. Usa docs/Presentacion.pptx como apoyo y el informe PDF para responder las preguntas.

## Explicar el código

- MainActivity.java presenta acceso, registro, listado y formulario. El observador de autenticación abre el inventario según la cuenta activa. Al cerrar sesión se retira la escucha anterior.
- ProductoRepositorio.java recibe el UID de Authentication. guardar usa document().set(datos) al crear y document(id).update(datos) al editar. observar recibe instantáneas y eliminar llama a delete().
- firestore.rules valida la identidad, los campos y el rango del stock en el servidor. Para cambiar el máximo de unidades, adapta tanto la validación del formulario como la regla y publica la nueva versión.
- Ejercicio de defensa: cambia orderBy("nombre") por orderBy("stock") en observar para ordenar las tarjetas por cantidad ascendente.

## Material para la evaluación

- [Configuración de Firebase paso a paso](docs/FIREBASE.md)
- [Informe de investigación](docs/Informe.pdf)
- [Presentación](docs/Presentacion.pptx)
- [Video de simulación](docs/Papelario-Simulacion.mp4)
