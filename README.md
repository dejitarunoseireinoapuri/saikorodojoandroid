# Saikoro Dojo

## a) Descripción general del proyecto

**Saikoro Dojo** es un videojuego de puzles para Android centrado en la resolución de objetivos con dados, cartas y minijuegos.

La experiencia principal se basa en:

- Un **modo principal por niveles** donde el jugador debe cumplir condiciones sobre una tirada de dados (por ejemplo combinaciones, sumas, restricciones de selección, etc.).
- Un sistema de **cartas de apoyo** que permite alterar el estado de los dados (repetir tiradas, ajustar valores, invertir caras, fijar valores, etc.).
- Un conjunto de **minijuegos independientes** (Odd/Even, Sequence, Higher/Lower, Blackjack) que funcionan como vía adicional para conseguir recompensas y mejorar el progreso.

---

## b) Stack tecnológico utilizado

### Lenguaje y plataforma
- **Kotlin** (Kotlin 2.3.10)
- **Android SDK**
  - `minSdk = 30`
  - `targetSdk = 36`
  - `compileSdk = 36`

### UI y arquitectura
- **Jetpack Compose** para la interfaz
- **Material 3** para componentes visuales
- **Navigation Compose** para navegación
- **MVVM** para estado y lógica de presentación
- **Clean Architecture** para separación de responsabilidades
- Enfoque técnico con capas `presentation`, `domain` y `data` para mantener una base escalable y mantenible

### Concurrencia y serialización
- **Kotlin Coroutines / Flow**
- **kotlinx.serialization (JSON)**

### Monetización y consentimiento
- **Google Mobile Ads SDK** (`play-services-ads`)
- **Google User Messaging Platform (UMP)** para consentimiento de anuncios

### Testing
- **JUnit 4** (unit tests)
- **kotlinx-coroutines-test**
- **AndroidX Test + Espresso** (instrumentation tests)
- **Compose UI Test**

### Build tools
- **Gradle (Kotlin DSL)**
- **Android Gradle Plugin 9.0.0**

---

## c) Información sobre instalación y ejecución

> Requisitos recomendados:
>
> - Android Studio reciente (con soporte para AGP 9)
> - JDK 11
> - Android SDK instalado y configurado

### 1. Clonar el repositorio

```bash
git clone git@github.com:dejitarunoseireinoapuri/saikorodojoandroid.git
cd saikorodojoandroid
```

### 2. Configurar propiedades locales

Crea o edita tu `local.properties` (si Android Studio no lo hace automáticamente) para apuntar al SDK:

```properties
sdk.dir=/ruta/a/Android/Sdk
```

### 3. Configurar claves de AdMob (opcional pero recomendado para integración completa)

El proyecto espera propiedades Gradle para inyectar IDs de anuncios:

- `ADMOB_APP_ID`
- `ADMOB_REWARDED_UNIT_ID`
- `ADMOB_INTERS`

Puedes declararlas en `~/.gradle/gradle.properties` (recomendado para no subir secretos al repo):

```properties
ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
ADMOB_REWARDED_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
ADMOB_INTERS=ca-app-pub-xxxxxxxxxxxxxxxx/aaaaaaaaaa
```

Si no se configuran, la app compila con valores vacíos para esos campos.

### 4. Abrir y ejecutar

#### Opción Android Studio
1. Abrir la carpeta del proyecto.
2. Esperar sincronización de Gradle.
3. Seleccionar un emulador/dispositivo Android (API 30+).
4. Ejecutar la configuración `app`.

#### Opción terminal
```bash
./gradlew installDebug
```

Para ejecutar tests/lint (flujo habitual de CI):

```bash
./gradlew test
./gradlew lint
```

---

## d) Estructura del proyecto

```text
saikorodojoandroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dejitarunoseireinoapuri/saikorodojo/
│   │   │   │   ├── feature/
│   │   │   │   │   ├── game/
│   │   │   │   │   ├── cards/
│   │   │   │   │   ├── oddeven/
│   │   │   │   │   ├── sequence/
│   │   │   │   │   ├── higherlower/
│   │   │   │   │   ├── blackjack/
│   │   │   │   │   ├── menu/
│   │   │   │   │   ├── rules/
│   │   │   │   │   ├── settings/
│   │   │   │   │   ├── sound/
│   │   │   │   │   └── ads/
│   │   │   │   ├── navigation/
│   │   │   │   ├── presentation/
│   │   │   │   ├── ui/theme/
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   │       ├── values*/
│   │   │       ├── raw/ (efectos de sonido)
│   │   │       └── mipmap*/
│   │   ├── test/ (unit tests)
│   │   └── androidTest/ (instrumentation/UI tests)
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Organización interna por capas (por feature)

Dentro de las features se aplica una distribución por responsabilidades:

- `presentation`: composables, ViewModels, estado/eventos/efectos de UI.
- `domain`: entidades y casos de uso puros (sin dependencias Android).
- `data`: repositorios concretos, fuentes de datos y adaptadores.

Regla de dependencias:
- `presentation -> domain`
- `data -> domain`
- `domain -> (sin dependencias de capas superiores)`

---

## e) Funcionalidades principales

### 1) Juego principal por niveles
- Progresión por niveles con objetivos variables.
- Evaluación de condiciones sobre subconjuntos de dados seleccionados.
- Dificultad creciente mediante combinación de tipos/cantidad de dados y reglas de objetivo.

### 2) Sistema de cartas
- Inventario de cartas con efectos tácticos.
- Acciones como ajustar valor, invertir cara, relanzar uno o varios dados, fijar valor o repetir última carta.
- Las cartas permiten resolver niveles con mayor flexibilidad estratégica.

### 3) Minijuegos integrados
- **Odd/Even**
- **Higher/Lower**
- **Sequence**
- **Blackjack**

Estos minijuegos amplían la jugabilidad y actúan como mecánica de recompensa para obtener más cartas.

### 4) Persistencia de sesión y estado
- Soporte de continuación de partida.
- Conservación de progreso e inventario entre sesiones.

### 5) Audio y feedback
- Efectos de sonido del juego y de interacción.
- Control de activación/desactivación de sonido.

### 6) Pantallas de soporte
- Menú principal.
- Reglas del juego.
- Ajustes.
- Gestión de consentimiento y opciones de anuncios.

### 7) Internacionalización
- Recursos localizados en varios idiomas (por ejemplo, inglés, español y catalán).

---

## Notas adicionales

- El proyecto está orientado a mantener animaciones y experiencia visual de juego con foco en eficiencia.
- Para despliegues públicos, se recomienda gestionar IDs/secretos mediante variables locales y nunca hardcodearlos en el código fuente.
