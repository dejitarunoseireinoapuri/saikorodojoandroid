# Reglas definitivas — Puzzle de dados con cartas y minijuegos

---

## 1. Concepto general

Juego tipo **puzzle** con **dados** y **cartas potenciadoras**.

- Cada nivel presenta una **tirada de dados** (visual) y un **objetivo** matemático/lógico.
- Los niveles son **procedurales** y la dificultad **aumenta progresivamente**.
- Los niveles están diseñados para ser **siempre resolubles**.

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
  - decide qué dados se seleccionan (y por tanto se suman),
  - decide si se usa alguna carta o ninguna (al azar),
  - y produce un objetivo acorde a los tipos de objetivos permitidos.
- El nivel se valida como resoluble:
  - existe al menos una solución
  - y la solución cabe dentro del límite de movimientos del nivel.

---

## 3. Formato del puzzle (cómo se juega)

### 3.1. Operación única: suma
- **Los dados seleccionados siempre se suman.**
- No existen otras operaciones matemáticas (no hay resta, multiplicación ni división).

### 3.2. Selección de dados
- El jugador puede seleccionar **cualquier subconjunto** de dados.
- **No es obligatorio usar todos los dados.**
- El resultado del intento es la **suma** de los dados seleccionados.

---

## 4. Sistema de movimientos y condición de fallo

### 4.1. Qué cuenta como movimiento
- **Cada dado seleccionado cuenta como 1 movimiento.**
- **Cada carta usada cuenta como 1 movimiento.**

Ejemplo: seleccionar 3 dados y usar 2 cartas = **5 movimientos**.

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

### 10.1. Poker reducido (3 tiradas con guardado)
- El jugador realiza **3 tiradas** (con varios dados por tirada).
- En cada tirada puede **guardar** ciertos resultados para formar una mano final.
- Objetivo: conseguir una combinación ganadora (ejemplos):
  - pareja
  - trío
  - escalera corta
  - otras combinaciones reducidas según diseño.

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

---
