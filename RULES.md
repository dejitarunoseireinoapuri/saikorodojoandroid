# Reglas definitivas — Puzzle de dados con cartas y minijuegos (actualizadas)

---

## 1. Concepto general

Juego tipo **puzzle** con **dados** y **cartas potenciadoras**.

- Cada nivel presenta una **tirada de dados** (visual) y un **objetivo** a cumplir **sobre la tirada final**.
- Los objetivos pueden ser de **combinación, colección, métricas numéricas y propiedades** (ver apartado 4).
- Los niveles son **procedurales** y la dificultad **aumenta progresivamente**.
- **Bucle principal:** al completar un nivel del **juego principal**, se pasa **directamente** al **siguiente nivel principal**.
- Los **minijuegos** existen como modo separado para **conseguir cartas** (ver apartados 8–9).

---

## 2. Niveles procedurales

### 2.1. Dados (tirada fija, pero “visual”)
- En cada nivel, **todos los dados se tiran una vez** (animación).
- El resultado de la tirada **no es aleatorio** a nivel lógico:
    - está **predeterminado** por la seed del nivel.
- Los dados serán siempre **positivos** y pueden ser de **6, 8 o 10 caras** según el nivel.

### 2.2. Objetivo y coherencia del nivel
- El generador crea niveles coherentes (objetivos y valores compatibles con los tipos de dados presentes).

### 2.3. Generación del nivel (idea)
- Cada nivel se define con una **seed** reproducible.
- El generador define:
    - el número de dados,
    - el tipo o mezcla de dados (d6/d8/d10),
    - un objetivo (según etapa),
    - un límite de movimientos,
    - un subconjunto de cartas permitidas (subconjunto del inventario),
    - y, opcionalmente, cartas obligatorias (solo si el jugador ya las posee).

---

## 3. Formato del puzzle (cómo se juega)

### 3.1. Evaluación según objetivo
- **No hay una única operación global.**
- El **objetivo** determina **qué se evalúa** de los dados usados en el intento:
    - **Combinaciones / colecciones:** se evalúa el **multiconjunto** de valores (las repeticiones importan).
    - **Objetivos numéricos:** se evalúa normalmente la **suma** de los dados usados.
    - **Métricas y propiedades:** se evalúan funciones como `máximo − mínimo`, paridad, múltiplos, residuos, etc. (ver apartado 4).

### 3.2. Selección de dados (“dados usados”)
- El jugador puede marcar **cualquier subconjunto** de dados como “usados”.
- **No es obligatorio usar todos los dados**, salvo que el objetivo del nivel lo exija explícitamente.
- El objetivo siempre se evalúa sobre la **tirada final** y **los dados usados** (tras cartas), siguiendo las reglas del apartado 4.

---

## 4. Objetivos de nivel (tipos)

Los niveles pueden pedir uno de estos objetivos (se irán introduciendo con la dificultad).  
El **objetivo siempre se evalúa sobre la tirada final** (tras cartas).

**Dados del nivel**
- En un nivel **no es obligatorio usar todos los dados disponibles**: el objetivo puede cumplirse usando un **subconjunto** de dados (según indique el nivel o según convenga al jugador).
- La **tirada inicial** puede incluir **uno o varios tipos de dado** (d6, d8 y/o d10), según el nivel.

### 4.1. Objetivos de combinación
- **X iguales:** conseguir un **par**, **trío**, **póker**, etc.
- **Doble grupo:** “**dos pares**”, “**par + trío**”, “**dos tríos**” (si el nº de dados lo permite).
- **Full:** **trío + par**.
- **Escalera:** conseguir **N consecutivos** (p. ej. 3 o 4 seguidos).
- **Todos distintos:** ningún valor repetido.

### 4.2. Objetivos de valores exactos (colección / set)
- **Colección exacta:** deben aparecer estos valores (en cualquier orden).
    - Ej.: “aparecen un **1** y un **3**”.
    - Ej.: “aparecen **2, 3, 8 y 9**”.
- **Colección con multiplicidades:** valores concretos con repeticiones exigidas.
    - Ej.: “**dos 4** y un **7**”.
- **Colección parcial:** de una lista dada deben aparecer al menos `K`.
    - Ej.: “de {2,3,8,9} aparecen al menos 3”.
- **Colección prohibida:** no puede aparecer ninguno de {x,y,z}.

> Nota: cuando haya mezcla de d6/d8/d10, la lista de valores se genera para que sea compatible con los dados presentes (no se pedirá un 9 si no hay ningún dado que pueda mostrar 9).

### 4.3. Objetivos numéricos y métricas simples
- **Exacto:** alcanzar exactamente `N`.
- **Rango:** el resultado debe estar dentro de `[A, B]`.
- **Comparación:** `> X`, `≥ X`, `< X` o `≤ X`.
- **Diferencia en rango:** `máximo − mínimo` debe estar dentro de `[A, B]`.
- **Distancia objetivo (tolerancia):** quedar a **0–1** (o 0–2) del objetivo `N` (según indique el nivel).

### 4.4. Objetivos con propiedades (siempre con umbral)
- **Paridad + umbral:** `resultado ≥ X` y el resultado es **par** o **impar**.
- **Múltiplo + umbral:** `resultado ≥ X` y el resultado es **múltiplo de Y**.
- **Residuo (módulo):** `resultado mod M = R` (con `resultado ≥ X` o con “resultado en rango” indicado por el nivel).
- **Rango comprimido:** todos los dados usados para el objetivo deben quedar dentro de un intervalo `[A, B]` (p. ej., “todos entre 4 y 7”).

### 4.5. Objetivos de ejecución (dificultad avanzada)
- **Uso limitado de cartas:** “cumple el objetivo usando como máximo `C` cartas” o “usa exactamente 1 carta”.
- **Bloqueo obligatorio:** “tras la 1ª tirada, bloquea al menos `K` dados”.
- **Cambios limitados:** “entre inicio y final solo pueden cambiar `K` dados”.

### 4.6. Límites y coherencia de generación
- Cualquier objetivo numérico se genera respetando límites del conjunto de dados del nivel (p. ej., suma máxima posible con los dados disponibles).
- Los objetivos de colección se generan para que **todos los valores solicitados existan** en los tipos de dados presentes en el nivel.

---

## 5. Cartas potenciadoras (inventario global)

### 5.1. Inventario
- Las cartas son de **inventario global**.
- **No hay límite** de inventario.
- Se permiten **duplicados** (múltiples copias del mismo tipo).
- Las cartas son **de un solo uso**.

### 5.2. Consumo de cartas
- Las cartas **se consumen sólo si se completa el nivel con éxito**.
- Si se reinicia el nivel, se recuperan las cartas usadas en ese intento.

### 5.3. Aplicación de cartas
- Hay cartas que se aplican sobre **un dado concreto** (el jugador selecciona el dado al usar la carta).
- Hay cartas que se aplican sobre **toda la tirada** (afectan a varios dados a la vez).
- No hay deshacer: aplicar una carta es **irreversible** dentro del intento.

### 5.4. Cartas acordadas
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

> Importante: **no existe “re-tirada” como variable de dificultad**. Re-tirar dados solo ocurre mediante las **cartas específicas**.

---

## 6. Reinicio de nivel y anuncios

### 6.1. Carta de reinicio de nivel
- Existe una **carta de reinicio de nivel**.
- Al usarla:
    - se reinicia el nivel completo (mismos dados/objetivo/seed),
    - se restablecen los movimientos,
    - y se recuperan las cartas usadas en el intento fallido.
- La carta de reinicio **se consume al reiniciar**.

### 6.2. Anuncios
- Si el jugador no tiene más cartas de reinicio,
  puede ver un **anuncio** para ganar **1 carta de reinicio**.

---

## 7. Flujo del juego (juego principal + minijuegos)

### 7.1. Juego principal
- El jugador juega niveles del puzzle de forma consecutiva.
- Al completar un nivel, se pasa **directamente** al siguiente nivel principal.

### 7.2. Minijuegos
- Los minijuegos se juegan **de forma independiente** al progreso del nivel principal.
- Su objetivo principal es **conseguir cartas** (ver apartado 8).
- El jugador puede recurrir a minijuegos para:
    - reforzar inventario,
    - buscar cartas por probabilidad,
    - o desbloquear nuevas opciones de resolución.

---

## 8. Minijuegos (modo para conseguir cartas)

### 8.1. Propósito
- Al **ganar** un minijuego, el jugador obtiene **1 carta**.
- Al **perder**, **no** obtiene carta.

### 8.2. Selección del minijuego
- Antes de jugar, el jugador elige entre **2 opciones aleatorias** (2 minijuegos distintos elegidos al azar).

### 8.3. Dificultad de minijuegos
- La dificultad de los minijuegos **no escala** con el nivel (por ahora).

### 8.4. Recompensas y probabilidades
- La carta obtenida es **aleatoria**, pero:
    - cada minijuego tiene cartas **más probables**,
    - y el resultado del minijuego modifica la probabilidad de recibir cartas “mejores”.

---

## 9. Minijuegos disponibles (4)

### 9.1. Mayor o menor (2d10)
- Se realiza una **tirada inicial** de **2 dados de 10 caras (2d10)**.
- El jugador decide si la **siguiente tirada** será **mayor** o **menor** que la anterior.
- Se realiza una **segunda tirada** de **2d10** y se comparan los resultados.

**Regla de comparación**
- Cada tirada se evalúa por la **suma de los 2d10**.
- Si la suma de la segunda tirada es **mayor** que la primera, gana la opción **“mayor”**.
- Si la suma de la segunda tirada es **menor** que la primera, gana la opción **“menor”**.
- Si la suma es **igual**, el jugador **gana**.

### 9.2. Blackjack con dados (vs banca)
- El jugador suma tiradas intentando llegar a **21** sin pasarse.
- Puede **plantarse** o **seguir tirando**.
- Reglas de banca:
    - Si la banca llega a 21 o supera al jugador sin pasarse, el jugador pierde.
    - Si el jugador se pasa, pierde.
    - Regla exacta de banca por definir (por ejemplo: tirar hasta 17).

### 9.3. Duelos de paridad
- En cada ronda, el jugador elige **par** o **impar**.
- Se tira un dado y se comprueba si acierta.
- Se juegan un número fijo de rondas (por ejemplo 5) y se evalúa el resultado.

### 9.4. Secuencia creciente (6 tiradas, guardas 3)
- Se realizan **6 tiradas** de 1 dado.
- En cada tirada el jugador decide **guardar** o **descartar**.
- Si guarda, el valor debe ser **estrictamente mayor** que el último guardado.
- Máximo guardados: **3**.
- Objetivo: completar una secuencia de longitud **3**.

---

## 10. Guardado y progreso

- Se guarda:
    - `seedBase` de la partida actual,
    - nivel alcanzado,
    - inventario de cartas,
    - y el estado del nivel si estaba en pausa.
- Si el jugador empieza un **nuevo juego desde nivel 1**:
    - se genera una `seedBase` nueva para que los niveles no se repitan.

---

## 11. Sistema de puntuación
- No hay puntuación ni estrellas.
- El objetivo es **superar niveles** y progresar.

---

## 12. Progresión de dificultad (cómo escala nivel a nivel)

La dificultad escala mediante una **puntuación interna de dificultad (D)** que aumenta con el nivel y controla, de forma gradual, estas variables:

- Número de dados lanzados
- Tipo(s) de dados (d6/d8/d10)
- Límite de movimientos
- Subconjunto de cartas permitidas en el nivel (de tu inventario)
- Cartas obligatorias (si existen), calculadas **siempre** en función de las cartas que el jugador **ya posee**
- Tipo de objetivo (apartado 4) y sus parámetros (tamaño de colección, longitud de escalera, nº de repeticiones, etc.)
- Restricciones adicionales (uso de dados/cambios/bloqueos)

> Importante: **no existe “re-tirada” como variable de dificultad**. Re-tirar dados solo ocurre mediante las **cartas específicas**.

### 12.1. Etapas (tiers) por rangos de niveles
Las etapas cambian **cada 15 niveles**:
- Etapa 1: niveles **1–15**
- Etapa 2: niveles **16–30**
- Etapa 3: niveles **31–45**
- Etapa 4: niveles **46–60**
- Etapa 5: niveles **61–75**
- … y así sucesivamente

En cada nueva etapa, la dificultad aumenta principalmente por:
1) más dados (según 12.2),
2) objetivos más exigentes y/o combinados,
3) límites de movimientos más ajustados,
4) más cartas obligatorias o restricciones de ejecución.

### 12.2. Número de dados por etapa (regla fija)
- Etapa 1: **5 dados**
- Etapa 2: **8 dados** (+3)
- Etapa 3: **11 dados** (+3)
- Etapa 4: **14 dados** (+3)
- Etapa 5: **17 dados** (+3)
- Etapa 6 y posteriores: **20 dados** (máximo)

Regla general:
- `numDados(etapa) = min(5 + 3*(etapa-1), 20)`

### 12.3. Tipo(s) de dados por etapa
- **Etapa 1 (1–15):** solo **d6**.
- **Etapa 2 (16–30):** d6 + introducción de **d8** con baja probabilidad.
- **Etapa 3 (31–45):** d6/d8 con mezcla más frecuente + introducción de **d10** con baja probabilidad.
- **Etapa 4 (46–60):** mezcla frecuente de d6/d8/d10.
- **Etapa 5+ (61+):** mezcla libre, seleccionada por el generador para ajustar la dificultad y la coherencia del objetivo.

### 12.4. Cartas obligatorias por etapa (regla fija)
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

### 12.5. Límite de movimientos (cómo se endurece)

El límite de movimientos se calcula por **etapa**, **tipo de objetivo** y **restricciones**, usando esta estructura:

- `movimientos = base(etapa) + ajusteObjetivo + ajusteRestricciones + ajusteBloque`

**Base por etapa (recomendado)**
- Etapa 1: `base = 6`
- Etapa 2: `base = 8`
- Etapa 3: `base = 10`
- Etapa 4: `base = 12`
- Etapa 5: `base = 14`
- Etapa 6+: `base = 15`

**Ajuste por tipo de objetivo (recomendado)**
- Objetivos básicos (par/trío, colección simple, prohibidos simples): `+0`
- Objetivos medios (dos pares, escalera 3, colección 3–4, parcial): `+1`
- Objetivos altos (full, póker, escalera 4, multiplicidades, espejo): `+2`
- Objetivos combinados / misiones dobles / ejecución avanzada frecuente: `+3`

**Ajuste por restricciones (recomendado)**
- “Usa exactamente / como máximo C cartas”: `+1`
- “Bloqueo obligatorio”: `+1`
- “Cambios limitados”: `+1`
- Si hay 2 o más restricciones a la vez, el ajuste total se capea a `+2` para no disparar el margen.

**Endurecimiento dentro de cada bloque de 15 niveles (ajusteBloque)**
- Niveles 1–5 de la etapa: `+1`
- Niveles 6–10 de la etapa: `+0`
- Niveles 11–15 de la etapa: `-1` (mínimo 1 movimiento)

### 12.6. Subconjunto de cartas permitidas (densidad de opciones)
- Etapas bajas: subconjunto **amplio**.
- Etapas medias: subconjunto **medio**.
- Etapas altas: subconjunto **pequeño**.

Reglas:
- Las cartas **obligatorias** siempre deben estar dentro del subconjunto permitido.
- Cartas raras (p. ej. “Convertir a valor fijo”) se reservan para etapas altas o para niveles puntuales.

### 12.7. Objetivos “más divertidos” (desbloqueo por niveles)

Los objetivos priorizan **patrones visuales**, **colecciones** y **combinaciones**.  
Los objetivos numéricos se usan poco, limitándose principalmente a **suma en rango** y **paridad + umbral**.

#### Etapa 1 (niveles 1–15)
- Pareja / Trío
- Todos distintos
- Colección exacta simple (2–3)
- Prohibido X / {x,y}
- Suma en rango (suave)

#### Etapa 2 (niveles 16–30)
- Dos pares
- Par + trío
- Colección exacta media (3–4)
- Colección parcial
- Escalera 3

#### Etapa 3 (niveles 31–45)
- Full
- Póker
- Escalera 4
- Multiplicidades
- Espejo (por tipo de dado)

#### Etapa 4 (niveles 46–60)
- Doble trío
- Mano “Arcoíris”
- Rango comprimido estrecho (“templo”)
- Prohibida avanzada + combinación
- Paridad + umbral (ocasional)

#### Etapa 5+ (niveles 61+)
- Objetivos dobles combinados
- Mano “Guardianes”
- Colección completa (4–6)
- Espejo perfecto
- Ejecución avanzada (frecuente)

### 12.8. Restricciones adicionales (introducción gradual)
- “Usa exactamente/al menos K dados”: Etapa 2+
- “Cambios limitados” / “bloqueo obligatorio”: Etapa 3+
- “Uso limitado de cartas”: Etapa 4+

### 12.9. Principio de control: 1 fuente nueva a la vez
- En cada etapa se endurece principalmente una variable por nivel.
- Novedades (nuevos objetivos/restricciones) entran al principio con parámetros suaves.

---
