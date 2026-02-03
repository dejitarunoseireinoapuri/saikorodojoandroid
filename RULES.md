# Reglas definitivas — Puzzle de dados con cartas y minijuegos (revisadas)

---

## 1. Concepto general

Juego tipo **puzzle** con **dados** y **cartas potenciadoras**.

- Cada nivel presenta una **tirada de dados** (visual) y un **objetivo** a cumplir **sobre la tirada final**.
- Los objetivos pueden ser de **combinación, colección, métricas numéricas y propiedades** (ver apartado 5).
- Los niveles son **procedurales** y la dificultad **aumenta progresivamente**.
- Los niveles están diseñados para ser **siempre resolubles**.
- **Bucle principal:** al completar un **nivel normal**, se juega **obligatoriamente** **1 minijuego** antes de pasar al siguiente nivel.

---

## 2. Niveles procedurales y resolubilidad

### 2.1. Dados (tirada fija, pero “visual”)
- En cada nivel, **todos los dados se tiran una vez** (animación).
- El resultado de la tirada **no es aleatorio** a nivel lógico:
    - está **predeterminado** por la seed del nivel.
- Los dados serán siempre **positivos** y pueden ser de **6, 8 o 10 caras** según el nivel.

### 2.2. Objetivo siempre resoluble
- Cada nivel se genera de forma que exista **al menos una solución**, teniendo en cuenta:
    - los valores de los dados del nivel,
    - **las cartas disponibles en el inventario del jugador**,
    - y el límite de movimientos del nivel.

### 2.3. Generación del nivel (idea)
- Cada nivel se define con una **seed** reproducible.
- El generador crea una **solución oculta** (ruta de éxito) que:
    - decide qué dados se **usan para evaluar el objetivo** (subconjunto o todos, según convenga),
    - decide si se usa alguna carta o ninguna (al azar),
    - y produce un objetivo acorde a los tipos de objetivos permitidos.
- El nivel se valida como resoluble:
    - existe al menos una solución
    - y la solución cabe dentro del límite de movimientos del nivel.

---

## 3. Formato del puzzle (cómo se juega)

### 3.1. Evaluación según objetivo
- **No hay una única operación global.**
- El **objetivo** determina **qué se evalúa** de los dados usados en el intento:
    - **Combinaciones / colecciones:** se evalúa el **multiconjunto** de valores (las repeticiones importan).
    - **Objetivos numéricos:** se evalúa normalmente la **suma** de los dados usados.
    - **Métricas y propiedades:** se evalúan funciones como `máximo − mínimo`, paridad, múltiplos, residuos, etc. (ver apartado 5).

### 3.2. Selección de dados (“dados usados”)
- El jugador puede marcar **cualquier subconjunto** de dados como “usados” para el intento.
- **No es obligatorio usar todos los dados**, salvo que el objetivo del nivel lo exija explícitamente.
- El **objetivo siempre se evalúa sobre la tirada final** y **los dados usados** (tras re-tiradas y cartas), siguiendo las reglas del apartado 5.

---

## 4. Sistema de movimientos y condición de fallo

### 4.1. Qué cuenta como movimiento
- **Cada dado marcado como “usado” cuenta como 1 movimiento**.
- **Cada carta usada cuenta como 1 movimiento**.

Ejemplo: usar 3 dados para evaluar el objetivo y usar 2 cartas = **5 movimientos**.

### 4.2. Límite de movimientos
- Cada nivel tiene un **límite máximo de movimientos**.
- La solución generada por el dispositivo requerirá **al menos** esos movimientos “exactos” (según la ruta de éxito), aunque podría existir otra solución alternativa.

### 4.3. Cuándo se pierde un intento
- Si el jugador no cumple el objetivo antes de agotar los movimientos, **falla el intento** del nivel.

---

## 5. Objetivos de nivel (tipos)

Los niveles pueden pedir uno de estos objetivos (se irán introduciendo con la dificultad). El **objetivo siempre se evalúa sobre la tirada final** (tras re-tiradas y cartas).

**Dados del nivel**
- En un nivel **no es obligatorio usar todos los dados disponibles**: el objetivo puede cumplirse usando un **subconjunto** de dados (según indique el nivel o según convenga al jugador).
- La **tirada inicial** puede incluir **uno o varios tipos de dado** (d6, d8 y/o d10), según el nivel.

### 5.1. Objetivos de combinación
- **X iguales:** conseguir un **par**, **trío**, **póker**, etc.
- **Doble grupo:** “**dos pares**”, “**par + trío**”, “**dos tríos**” (si el nº de dados lo permite).
- **Full:** **trío + par**.
- **Escalera:** conseguir **N consecutivos** (p. ej. 3 o 4 seguidos).
- **Todos distintos:** ningún valor repetido.

### 5.2. Objetivos de valores exactos (colección / set)
- **Colección exacta:** deben aparecer estos valores (en cualquier orden).
    - Ej.: “aparecen un **1** y un **3**”.
    - Ej.: “aparecen **2, 3, 8 y 9**”.
- **Colección con multiplicidades:** valores concretos con repeticiones exigidas.
    - Ej.: “**dos 4** y **un 7**”.
- **Colección parcial:** de una lista dada deben aparecer al menos `K`.
    - Ej.: “de {2,3,8,9} aparecen al menos 3”.
- **Colección prohibida:** no puede aparecer ninguno de {x,y,z}.

> Nota: cuando haya mezcla de d6/d8/d10, la lista de valores se genera para que sea compatible con los dados presentes (no se pedirá un 9 si no hay ningún dado que pueda mostrar 9).

### 5.3. Objetivos numéricos y métricas simples
- **Exacto:** alcanzar exactamente `N`.
- **Rango:** el resultado debe estar dentro de `[A, B]`.
- **Comparación:** `> X`, `≥ X`, `< X` o `≤ X`.
- **Diferencia en rango:** `máximo − mínimo` debe estar dentro de `[A, B]`.
- **Distancia objetivo (tolerancia):** quedar a **0–1** (o 0–2) del objetivo `N` (según indique el nivel).

### 5.4. Objetivos con propiedades (siempre con umbral)
- **Paridad + umbral:** `resultado ≥ X` y el resultado es **par** o **impar**.
- **Múltiplo + umbral:** `resultado ≥ X` y el resultado es **múltiplo de Y**.
- **Residuo (módulo):** `resultado mod M = R` (con `resultado ≥ X` o con “resultado en rango” indicado por el nivel).
- **Rango comprimido:** todos los dados usados para el objetivo deben quedar dentro de un intervalo `[A, B]` (p. ej., “todos entre 4 y 7”).

### 5.5. Objetivos de ejecución (dificultad avanzada)
- **Uso limitado de cartas:** “cumple el objetivo usando como máximo `C` cartas” o “usa exactamente 1 carta”.
- **Bloqueo obligatorio:** “tras la 1ª tirada, bloquea al menos `K` dados”.
- **Cambios limitados:** “entre inicio y final solo pueden cambiar `K` dados”.

### 5.6. Límites y coherencia de generación
- Cualquier objetivo numérico se genera respetando límites del conjunto de dados del nivel (p. ej., suma máxima posible con los dados disponibles).
- Los objetivos de colección se generan para que **todos los valores solicitados existan** en los tipos de dados presentes en el nivel.

---

## 6. Cartas potenciadoras (inventario global)

### 6.1. Inventario
- Las cartas son de **inventario global**.
- **No hay límite** de inventario.
- Se permiten **duplicados** (múltiples copias del mismo tipo).
- Las cartas son **de un solo uso**.

### 6.2. Consumo de cartas
- Las cartas **se consumen sólo si se completa el nivel con éxito**.
- Si se reinicia el nivel, se recuperan las cartas usadas en ese intento.

### 6.3. Aplicación de cartas
- Hay cartas que se aplican sobre **un dado concreto** (el jugador selecciona el dado al usar la carta).
- Hay cartas que se aplican sobre **toda la tirada** (afectan a varios dados a la vez).
- No hay deshacer: aplicar una carta es **irreversible** dentro del intento.

### 6.4. Cartas acordadas

- **Ajuste ±1** (1 dado)
    - El jugador elige si aumenta o reduce en 1 el valor de un dado (sin salir de `1..caras`).

- **Voltear cara** (1 dado)
    - Convierte `x` en `max+1-x`, según el tipo del dado seleccionado:
        - d6: `1↔6`, `2↔5`, `3↔4`
        - d8: `1↔8`, `2↔7`, `3↔6`, `4↔5`
        - d10: `1↔10`, `2↔9`, `3↔8`, `4↔7`, `5↔6`

- **Re-tirar 1 dado** (1 dado)
    - El jugador selecciona un dado y se re-tira sólo ese dado.

- **Re-tirar todos menos uno** (tirada completa)
    - El jugador selecciona el dado a conservar y se vuelven a tirar todos los demás.

- **Convertir a valor fijo** (1 dado)
    - El jugador elige el valor final del dado dentro de su rango (`1..caras`).
    - Carta muy rara.

- **Volver a utilizar la última carta** (general)
    - Repite exactamente el efecto de la última carta usada,
    - pero permite elegir un **nuevo objetivo** (otro dado o tirada, según el tipo de la carta repetida),
    - como si se tuviese **una segunda copia** de esa carta.

---

## 7. Generación resoluble por construcción (tirada inicial desde el resultado final)

Para garantizar que **todos los niveles son resolubles**, el generador construye los niveles **desde una solución final válida** y deriva la tirada inicial a partir de ella.

1. **Elige el objetivo** del nivel (combinación, colección, numérico, propiedades, etc.).
2. **Construye una tirada final válida** (valores concretos de cada dado) que **cumpla el objetivo**.
3. Define las restricciones del nivel:
- número de **tiradas** (re-tiradas) permitidas,
- y un subconjunto de **cartas disponibles** (pueden incluirse o no **por azar**, entre las que el jugador ya posee).
4. A partir de la tirada final, el generador calcula la **tirada inicial mostrada** “yendo hacia atrás”:
- se generan estados previos compatibles con las re-tiradas y con las cartas elegidas (si se decide usarlas en la construcción),
- de forma que exista al menos un camino que permita al jugador volver a la tirada final (o a cualquier otra tirada final que cumpla el objetivo) dentro de los límites del nivel.
5. **Validación final:** el nivel solo se acepta si, desde la tirada inicial, existe **al menos una secuencia** de re-tiradas y uso de cartas (respetando límites y disponibilidad) que alcanza el objetivo.

Consecuencias:
- La **tirada inicial no es aleatoria pura**: está **derivada** de una solución final.
- Según el nivel, la tirada inicial puede incluir **uno o varios tipos de dado** (d6, d8 y/o d10).
- El jugador **no tiene por qué usar todos los dados disponibles** para cumplir el objetivo: puede resolverlo con un subconjunto si así lo permiten las reglas del objetivo y del nivel.

---

## 8. Reinicio de nivel y anuncios

### 8.1. Carta de reinicio de nivel
- Existe una **carta de reinicio de nivel**.
- Al usarla:
    - se reinicia el nivel completo (mismos dados/objetivo/seed),
    - se restablecen los movimientos,
    - y se recuperan las cartas usadas en el intento fallido.
- La carta de reinicio **se consume al reiniciar**.

### 8.2. Anuncios
- Si el jugador no tiene más cartas de reinicio,
  puede ver un **anuncio** para ganar **1 carta de reinicio**.

---

## 9. Minijuegos (1 después de cada nivel)

### 9.1. Flujo
- Después de completar un nivel normal, se juega **obligatoriamente** un minijuego.
- El jugador obtiene **1 carta** como recompensa del minijuego.
- Si pierde el minijuego, **no obtiene carta**.

### 9.2. Selección del minijuego
- Antes de jugar, el jugador elige entre **2 opciones aleatorias** (2 minijuegos distintos elegidos al azar).

### 9.3. Dificultad de minijuegos
- La dificultad de los minijuegos **no escala** con el nivel (por ahora).

### 9.4. Recompensas
- La carta obtenida es **aleatoria**, pero:
    - cada minijuego tiene cartas **más probables**,
    - y el resultado del minijuego modifica la probabilidad de recibir cartas “mejores”.

---

## 10. Minijuegos disponibles (4)

### 10.1. Mayor o menor (2d10)
- Se realiza una **tirada inicial** de **2 dados de 10 caras (2d10)**.
- A continuación, el jugador debe decidir si la **siguiente tirada** será **mayor** o **menor** que la anterior.
- Se realiza una **segunda tirada** de **2d10** y se comparan los resultados.

**Regla de comparación**
- Cada tirada se evalúa por la **suma de los 2d10**.
- Si la suma de la segunda tirada es **mayor** que la primera, gana la opción **“mayor”**.
- Si la suma de la segunda tirada es **menor** que la primera, gana la opción **“menor”**.
- Si la suma es **igual**, el jugador **gana**.


### 10.2. Blackjack con dados (vs banca)
- El jugador suma tiradas intentando llegar a **21** sin pasarse.
- Puede **plantarse** o **seguir tirando**.
- Reglas de banca:
    - Si la banca llega a 21 o supera al jugador sin pasarse, el jugador pierde.
    - Si el jugador se pasa, pierde.
    - Regla exacta de banca por definir (por ejemplo: tirar hasta 17).

### 10.3. Duelos de paridad
- En cada ronda, el jugador elige **par** o **impar**.
- Se tira un dado y se comprueba si acierta.
- Se juegan un número fijo de rondas (por ejemplo 5) y se evalúa el resultado.

### 10.4. Secuencia creciente (6 tiradas, guardas 3)
- Se realizan **6 tiradas** de 1 dado.
- En cada tirada el jugador decide **guardar** o **descartar**.
- Si guarda, el valor debe ser **estrictamente mayor** que el último guardado.
- Máximo guardados: **3**.
- Objetivo: completar una secuencia de longitud **3**.

---

## 11. Guardado y progreso

- Se guarda:
    - `seedBase` de la partida actual,
    - nivel alcanzado,
    - inventario de cartas,
    - y el estado del nivel si estaba en pausa.
- Si el jugador empieza un **nuevo juego desde nivel 1**:
    - se genera una `seedBase` nueva para que los niveles no se repitan.

---

## 12. Sistema de puntuación
- No hay puntuación ni estrellas.
- El objetivo es **superar niveles** y progresar.

## 13. Progresión de dificultad (cómo escala nivel a nivel)

La dificultad escala mediante una **puntuación interna de dificultad (D)** que aumenta con el nivel y controla, de forma gradual, estas variables:

- Número de dados lanzados
- Tipo(s) de dados (d6/d8/d10)
- Límite de movimientos
- Subconjunto de cartas permitidas en el nivel (de tu inventario)
- Cartas obligatorias (si existen), calculadas **siempre** en función de las cartas que el jugador **ya posee**
- Tipo de objetivo (apartado 5) y sus parámetros (tamaño de colección, longitud de escalera, nº de repeticiones, etc.)
- Restricciones adicionales (uso de dados/cambios/bloqueos)

> Importante: **no existe “re-tirada” como variable de dificultad**. Re-tirar dados solo ocurre mediante las **cartas específicas** (p. ej. “Re-tirar 1 dado”, “Re-tirar todos menos uno”).

### 13.1. Etapas (tiers) por rangos de niveles

Las etapas cambian **cada 15 niveles**:
- Etapa 1: niveles **1–15**
- Etapa 2: niveles **16–30**
- Etapa 3: niveles **31–45**
- Etapa 4: niveles **46–60**
- Etapa 5: niveles **61–75**
- … y así sucesivamente

En cada nueva etapa, la dificultad aumenta principalmente por:
1) más dados (según 13.2),
2) objetivos más exigentes y/o combinados,
3) límites de movimientos más ajustados,
4) más cartas obligatorias o restricciones de ejecución.

### 13.2. Número de dados por etapa (regla fija)

- Etapa 1: **5 dados**
- Etapa 2: **8 dados** (+3)
- Etapa 3: **11 dados** (+3)
- Etapa 4: **14 dados** (+3)
- Etapa 5: **17 dados** (+3)
- Etapa 6 y posteriores: **20 dados** (máximo)

Regla general:
- `numDados(etapa) = min(5 + 3*(etapa-1), 20)`

### 13.3. Tipo(s) de dados por etapa

- **Etapa 1 (1–15):** solo **d6**.
- **Etapa 2 (16–30):** d6 + introducción de **d8** con baja probabilidad.
- **Etapa 3 (31–45):** d6/d8 con mezcla más frecuente + introducción de **d10** con baja probabilidad.
- **Etapa 4 (46–60):** mezcla frecuente de d6/d8/d10.
- **Etapa 5+ (61+):** mezcla libre, seleccionada por el generador para ajustar la dificultad y la coherencia del objetivo.

### 13.4. Cartas obligatorias por etapa (regla fija)

Las **cartas obligatorias** son cartas que el nivel exige que se usen para poder considerarse resuelto.

- **Etapa 1:** hasta **1** carta obligatoria.
- **Etapa 2:** hasta **2** cartas obligatorias.
- **Etapa 3:** hasta **3** cartas obligatorias.
- **Etapa 4 y posteriores:** **sin límite** (puede requerir cualquier número).

Reglas de coherencia:
- El generador **solo puede marcar como obligatorias cartas que el jugador ya tiene** en su inventario.
- Si el jugador no tiene suficientes cartas para cumplir el requisito de la etapa, el nivel:
    - baja el número de cartas obligatorias para ese nivel concreto, o
    - elige otras cartas obligatorias que el jugador sí posea,
    - manteniendo la etapa y el resto de la dificultad.

### 13.5. Límite de movimientos (cómo se endurece)

El límite de movimientos se fija como:
- `movimientos = movimientosRutaExito + margen`

Donde:
- `movimientosRutaExito` es el coste exacto de la ruta de éxito (dados usados + cartas obligatorias y opcionales de la solución).
- El **margen** disminuye con el nivel dentro de cada etapa:

Patrón recomendado dentro de cada bloque de 15 niveles:
- Niveles 1–5 de la etapa: margen **2**
- Niveles 6–10 de la etapa: margen **1**
- Niveles 11–15 de la etapa: margen **0**

Esto hace que los últimos niveles de cada etapa sean más exigentes sin introducir mecánicas nuevas de golpe.

### 13.6. Subconjunto de cartas permitidas (densidad de opciones)

Aunque el inventario del jugador sea global, cada nivel puede restringir qué cartas “están disponibles” para usarse en ese nivel (siempre como subconjunto del inventario real).

- Etapas bajas: subconjunto **amplio** (más libertad).
- Etapas medias: subconjunto **medio**.
- Etapas altas: subconjunto **pequeño** (menos libertad).

Reglas:
- Las cartas **obligatorias** siempre deben estar dentro del subconjunto permitido.
- Las cartas raras (p. ej. “Convertir a valor fijo”) se reservan para etapas altas o para niveles puntuales.

### 13.7. Objetivos “más divertidos” (desbloqueo por niveles)

Los objetivos priorizan **patrones visuales**, **colecciones** y **combinaciones**.  
Los objetivos numéricos se usan poco (como variedad), limitándose principalmente a **suma en rango** y **paridad + umbral**.

#### Etapa 1 (niveles 1–15) — Básicos y muy visuales
- **Pareja / Trío** (al menos uno).
- **Todos distintos**.
- **Colección exacta simple** (2–3 valores).
- **Prohibido X** (o prohibido `{x,y}`).
- **Suma en rango (suave)** (rango amplio).

#### Etapa 2 (niveles 16–30) — Colecciones y combos reconocibles
- **Dos pares**.
- **Par + trío**.
- **Colección exacta media** (3–4 valores; multiplicidad como mucho muy ligera).
- **Colección parcial** (“de {…} aparecen al menos K”).
- **Escalera corta** (3 consecutivos).

#### Etapa 3 (niveles 31–45) — Retos “de mano” y rarezas suaves
- **Full** (trío + par).
- **Póker** (4 iguales).
- **Escalera larga** (4 consecutivos).
- **Colección con multiplicidades** (p. ej. “dos 4 y un 7”).
- **Espejo** (parejas complementarias por tipo de dado):
    - d6: (1,6), (2,5), (3,4)
    - d8: (1,8), (2,7), (3,6), (4,5)
    - d10: (1,10), (2,9), (3,8), (4,7), (5,6)

#### Etapa 4 (niveles 46–60) — Objetivos temáticos (más exigentes)
- **Doble trío**.
- **Mano “Arcoíris”** (zonas baja/media/alta definidas por el nivel; debe aparecer al menos 1 de cada zona).
- **Todos en el mismo “templo” (rango comprimido)**: todos los dados usados en `[A,B]` con intervalo estrecho.
- **Colección prohibida avanzada + combinación** (p. ej. “sin {x,y,z} y además al menos un par”).
- **Paridad + umbral (ocasional)**: suma par/impar y `≥ X`.

#### Etapa 5+ (niveles 61+) — “Misiones” combinadas y ejecución avanzada
- **Objetivo doble** (dos condiciones visuales juntas), p. ej.:
    - “escalera corta + sin 1”
    - “dos pares + colección parcial”
- **Mano “Guardianes”**: exactamente `K` valores “sagrados” (lista del nivel) y el resto no puede ser de esa lista.
- **Exactitud total (colección completa)**: lista grande de valores exactos (4–6) compatible con los dados del nivel.
- **Espejo perfecto**: todos los dados usados se emparejan en parejas espejo (sin sobrantes).
- **Ejecución avanzada (frecuente)**: se añade además una restricción del 5.5, por ejemplo:
    - “usa exactamente C cartas”
    - “usa como máximo C cartas”
    - “tras la 1ª tirada, bloquea al menos K dados”
    - “entre inicio y final solo pueden cambiar K dados”

### 13.8. Restricciones adicionales (introducción gradual)

Para evitar picos de dificultad, las restricciones de ejecución se introducen progresivamente:

- “Usa exactamente K dados” / “usa al menos K dados”:
    - Etapa 2+ (K bajo al principio; más exigente en etapas altas).
- “Cambios limitados” / “bloqueo obligatorio” (5.5):
    - Etapa 3+ (primero poco restrictivo; luego más estricto).
- “Uso limitado de cartas” (5.5):
    - Etapa 4+ (más frecuente y combinable con objetivos complejos).

### 13.9. Principio de control: 1 fuente nueva a la vez

Para que el incremento sea estable:
- Dentro de una misma etapa, el generador incrementa **principalmente** una variable por nivel (por ejemplo, parámetros del objetivo o margen de movimientos),
- mientras el resto se mantiene estable o cambia mínimamente.
- Las “novedades” (nuevos objetivos o nuevas restricciones) entran al principio de la etapa con parámetros suaves y se endurecen hacia el final.

### 13.10. Garantía de resolubilidad con escalado

Aunque la dificultad suba:
- El generador sigue construyendo desde una **tirada final válida** (apartado 7),
- selecciona cartas obligatorias **solo** entre las que el jugador posee,
- fija el límite de movimientos a partir de la ruta de éxito,
- y valida que desde la tirada inicial exista al menos una secuencia válida que resuelva el nivel.

Si una configuración no es resoluble:
- se regenera el nivel con el mismo nivel `L` ajustando parámetros (margen, objetivo, selección de cartas obligatorias/permitidas),
- sin romper las reglas de etapa (p. ej. manteniendo el número de dados de la etapa).

