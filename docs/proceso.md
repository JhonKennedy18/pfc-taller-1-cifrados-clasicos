# Informe de proceso

## Puntos 1 y 2: cifrado César

Este informe muestra cómo se ejecutan `cesar` (recursión lineal) y `cesarCola`
(recursión de cola) con el mensaje `"casa"` y el desplazamiento `3`, con el
estado de la pila de llamados en cada paso.

### Cifrado de una letra

Las dos funciones usan la misma función auxiliar `desplazar`, que cifra una
sola letra. Si `p` es la posición de la letra (`a` = 0, ..., `z` = 25) y `k` el
desplazamiento, la nueva posición es

```math
p' = \big((p + k) \bmod 26 + 26\big) \bmod 26
```

El segundo `+ 26` y el segundo `mod` existen porque en Scala el operador `%`
puede devolver un valor negativo cuando `k < 0`. Así `p'` siempre queda entre 0
y 25. Lo que no es una letra minúscula (espacios, dígitos, signos, mayúsculas)
pasa sin cambio.

Para las letras de `"casa"` con `k = 3`:

| Letra | `p` | `p + k` | `p'` | Resultado |
|:-----:|:---:|:-------:|:----:|:---------:|
| `c`   | 2   | 5       | 5    | `f`       |
| `a`   | 0   | 3       | 3    | `d`       |
| `s`   | 18  | 21      | 21   | `v`       |
| `a`   | 0   | 3       | 3    | `d`       |

## Punto 1: `cesar` (recursión lineal)

```Scala
def cesar(m: Mensaje, k: Int): Mensaje = {
  def desplazar(c: Char): Char = ...
  if (m.isEmpty) ""
  else desplazar(m.head).toString + cesar(m.tail, k)
}
```

* **Caso base:** si el mensaje está vacío, el resultado es vacío.
* **Caso recursivo:** se cifra la primera letra y se **suma** (concatena) con
  el resultado de cifrar el resto.

La suma ocurre **después** de que vuelve la llamada recursiva. Por eso cada
llamada debe quedar esperando en la pila, guardando la letra ya cifrada que le
falta pegar.

### Ejecución de `cesar("casa", 3)`

Fase de **ida**: cada llamada cifra su primera letra y llama con el resto. La
pila crece una capa por letra, más una para el caso base.

| Paso | Llamada que se apila | Pila después del paso (de abajo hacia arriba) |
|:----:|----------------------|-----------------------------------------------|
| 1 | `cesar("casa", 3)` | `cesar("casa", 3)` espera `"f" + _` |
| 2 | `cesar("asa", 3)`  | `cesar("casa", 3)` espera `"f" + _`, `cesar("asa", 3)` espera `"d" + _` |
| 3 | `cesar("sa", 3)`   | las dos anteriores, `cesar("sa", 3)` espera `"v" + _` |
| 4 | `cesar("a", 3)`    | las tres anteriores, `cesar("a", 3)` espera `"d" + _` |
| 5 | `cesar("", 3)`     | las cuatro anteriores, `cesar("", 3)` (caso base) |

En el paso 5 la pila llega a su **altura máxima: 5 capas**.

Fase de **vuelta**: el caso base devuelve el texto vacío y cada capa, de arriba
hacia abajo, hace la suma que tenía pendiente y se libera.

| Paso | Capa que termina | Cálculo | Devuelve | Pila que queda |
|:----:|------------------|---------|:--------:|----------------|
| 6  | `cesar("", 3)`     | caso base    | `""`     | 4 capas |
| 7  | `cesar("a", 3)`    | `"d" + ""`   | `"d"`    | 3 capas |
| 8  | `cesar("sa", 3)`   | `"v" + "d"`  | `"vd"`   | 2 capas |
| 9  | `cesar("asa", 3)`  | `"d" + "vd"` | `"dvd"`  | 1 capa  |
| 10 | `cesar("casa", 3)` | `"f" + "dvd"`| `"fdvd"` | 0 capas |

### Diagrama de llamados de pila con recursión lineal

```mermaid
sequenceDiagram
  participant C0 as cesar(casa, 3)
  participant C1 as cesar(asa, 3)
  participant C2 as cesar(sa, 3)
  participant C3 as cesar(a, 3)
  participant C4 as cesar(vacio, 3)

  C0->>C1: llamada, queda pendiente f + ...
  C1->>C2: llamada, queda pendiente d + ...
  C2->>C3: llamada, queda pendiente v + ...
  C3->>C4: llamada, queda pendiente d + ...
  C4-->>C3: devuelve vacio
  C3-->>C2: devuelve d
  C2-->>C1: devuelve vd
  C1-->>C0: devuelve dvd
```

## Punto 2: `cesarCola` (recursión de cola)

```Scala
@tailrec
final def cesarCola(m: Mensaje, k: Int, acc: Mensaje = ""): Mensaje = {
  def desplazar(c: Char): Char = ...
  if (m.isEmpty) acc
  else cesarCola(m.tail, k, acc + desplazar(m.head))
}
```

* **Caso base:** si el mensaje está vacío, el resultado es `acc`.
* **Caso recursivo:** se cifra la primera letra, se agrega al acumulador `acc`
  y se llama con el resto.

La llamada recursiva es **lo último** que hace la función: no queda ninguna
operación pendiente después de ella. La anotación `@tailrec` le pide al
compilador que lo verifique y que convierta la recursión en un ciclo que
reutiliza la misma capa de la pila.

### Ejecución de `cesarCola("casa", 3)`

| Paso | Llamada | `m` | `acc` | Qué hace | Pila |
|:----:|---------|:---:|:-----:|----------|------|
| 1 | `cesarCola("casa", 3, "")` | `casa` | `""`   | cifra `c` y la agrega a `acc` | 1 capa |
| 2 | `cesarCola("asa", 3, "f")` | `asa`  | `f`    | cifra `a` y la agrega         | 1 capa |
| 3 | `cesarCola("sa", 3, "fd")` | `sa`   | `fd`   | cifra `s` y la agrega         | 1 capa |
| 4 | `cesarCola("a", 3, "fdv")` | `a`    | `fdv`  | cifra `a` y la agrega         | 1 capa |
| 5 | `cesarCola("", 3, "fdvd")` | vacío  | `fdvd` | caso base: devuelve `acc`     | 1 capa |

Resultado final: `"fdvd"`. La pila nunca pasa de **una capa**: en cada paso la
misma capa se reutiliza con nuevos valores de `m` y `acc`.

### Diagrama de llamados de pila con recursión de cola

```mermaid
sequenceDiagram
  participant Main as cesarCola(casa, 3, vacio)
  participant L1 as cesarCola(asa, 3, f)
  participant L2 as cesarCola(sa, 3, fd)
  participant L3 as cesarCola(a, 3, fdv)
  participant L4 as cesarCola(vacio, 3, fdvd)

  Main->>L1: tail call, acc = f
  L1->>L2: tail call, acc = fd
  L2->>L3: tail call, acc = fdv
  L3->>L4: tail call, acc = fdvd
  L4-->>Main: devuelve fdvd
```

A diferencia del diagrama del Punto 1, aquí no hay flechas de vuelta con
operaciones pendientes: cada llamada reemplaza a la anterior, y el resultado
sale directamente del caso base.

## Comparación: por qué una crece y la otra no

| Aspecto | `cesar` | `cesarCola` |
|---------|---------|-------------|
| Dónde se arma el resultado | Al **volver** de las llamadas | Al **ir**, dentro de `acc` |
| Operación pendiente tras la llamada | Sí: la concatenación `letra + _` | No |
| Capas de pila para `"casa"` | 5 | 1 |
| Capas de pila para $n$ letras | $n + 1$ | 1 |
| Espacio de pila | $O(n)$ | $O(1)$ |
| Mensajes muy largos | Puede desbordar la pila | No desbordan la pila |

* En **`cesar`** cada llamada debe guardar su letra ya cifrada, porque la
  necesita para la suma que hará cuando la llamada interna termine. Mientras
  espera, su capa no se puede liberar, así que la pila crece una capa por letra.
* En **`cesarCola`** lo que cada llamada necesitaría guardar ya viaja en `acc`,
  que es un parámetro. Al hacer la llamada recursiva no queda nada por hacer
  después, y por eso la capa actual se puede reutilizar. Es el proceso que
  haría un ciclo con una variable acumuladora, pero sin variables mutables ni
  `while`.

Esta diferencia es la que comprueba el test `"cesarCola: aguanta un mensaje
largo sin desbordar la pila"`, con un mensaje de 200 000 letras.


## Punto 3: `frecuencias` (recursión de cola)

La función `frecuencias` utiliza la función auxiliar `aux` anotada con `@tailrec`. Esto permite recorrer el mensaje de forma iterativa y mantener un espacio de pila constante.

* **Caso base:** si la lista de caracteres (`cola`) está vacía, devuelve el mapa acumulador `acc`.
* **Caso recursivo:** evalúa el primer carácter. Si es una letra minúscula, incrementa su conteo en el mapa `acc` y hace el llamado recursivo con el resto de la lista. Si no es una letra minúscula, hace el llamado recursivo dejando el mapa `acc` intacto.

### Ejecución de `aux` para el mensaje `"casa"`

La función convierte el mensaje en una lista de caracteres y la recorre con un mapa vacío como acumulador inicial.

| Paso | Llamada | `cola` | `acc` | Qué hace | Pila |
|:----:|---------|:------:|:-----:|----------|------|
| 1 | `aux(['c','a','s','a'], {})` | `['c','a','s','a']` | `{}` | cuenta `c` y actualiza `acc` | 1 capa |
| 2 | `aux(['a','s','a'], {'c':1})` | `['a','s','a']` | `{'c':1}` | cuenta `a` y actualiza `acc` | 1 capa |
| 3 | `aux(['s','a'], {'c':1, 'a':1})` | `['s','a']` | `{'c':1, 'a':1}` | cuenta `s` y actualiza `acc` | 1 capa |
| 4 | `aux(['a'], {'c':1, 'a':1, 's':1})` | `['a']` | `{'c':1, 'a':1, 's':1}` | cuenta la segunda `a` | 1 capa |
| 5 | `aux([], {'c':1, 'a':2, 's':1})` | vacío | `{'c':1, 'a':2, 's':1}` | caso base: devuelve `acc` | 1 capa |

### Diagrama de llamados de pila con recursión de cola

```mermaid
sequenceDiagram
  participant Main as aux(casa, vacio)
  participant L1 as aux(asa, c:1)
  participant L2 as aux(sa, c:1, a:1)
  participant L3 as aux(a, c:1, a:1, s:1)
  participant L4 as aux(vacio, c:1, a:2, s:1)

  Main->>L1: tail call, acc = c:1
  L1->>L2: tail call, acc = c:1, a:1
  L2->>L3: tail call, acc = c:1, a:1, s:1
  L3->>L4: tail call, acc = c:1, a:2, s:1
  L4-->>Main: devuelve Map(c -> 1, a -> 2, s -> 1)
```

Al finalizar la recolección de los datos, el resultado se convierte a una lista y se ordena. El criterio de ordenamiento prioriza las frecuencias mayores (`n1 > n2`). En caso de empate, resuelve por orden alfabético (`c1 < c2`).

## Punto 4: `desplazamientoProbable` y `romperCesar`

Estas funciones permiten descifrar un texto sin conocer su clave original mediante el análisis de la frecuencia de las letras.

### `desplazamientoProbable`

La función obtiene la lista ordenada de frecuencias del mensaje cifrado. Asume que la letra más frecuente del texto cifrado corresponde a la letra 'e' del texto original.

Si $p_{\text{max}}$ es la posición de la letra más frecuente en el alfabeto y $p_e$ es la posición de la 'e' (4), la distancia (o desplazamiento $k$) se calcula con la siguiente ecuación:

```math
k = (p_{\text{max}} - p_e + 26) \bmod 26
```

El factor `+ 26` garantiza que el valor antes del módulo sea positivo. Esto evita resultados negativos en el cálculo de la distancia.

### `romperCesar`

Una vez obtenido el desplazamiento probable $k$, la función `romperCesar` llama a `cesarCola(m, -k)` para revertir el cifrado y recuperar el mensaje original.

### Ejecución de `romperCesar("hvwh")`

Para comprobar el comportamiento, utilizamos el mensaje `"este"`, cifrado con $k = 3$, lo que genera el texto `"hvwh"`.

1. `desplazamientoProbable("hvwh")` llama a `frecuencias("hvwh")`.
2. `frecuencias` devuelve la letra más repetida: `h` (posición 7).
3. Se calcula la distancia $k$ entre `h` y la letra `e` (posición 4):
   ```math
   k = (7 - 4 + 26) \bmod 26 = 29 \bmod 26 = 3
   ```
4. `romperCesar` llama a `cesarCola("hvwh", -3)`.
5. El desplazamiento de `-3` revierte las letras a su estado original (`h` vuelve a `e`, `v` vuelve a `s`, `w` vuelve a `t`), devolviendo `"este"`.

### Flujo de ejecución de `romperCesar("hvwh")`

Aunque el punto 4 no utiliza recursión propia, coordina el llamado de varias funciones. Aquí detallamos cómo se transforma la información paso a paso al intentar descifrar la palabra `"hvwh"`.

| Paso | Función actual | Llamada interna | Resultado parcial | Acción |
|:----:|----------------|-----------------|-------------------|--------|
| 1 | `romperCesar` | `desplazamientoProbable("hvwh")` | Pendiente | Inicia el proceso de cálculo de clave. |
| 2 | `desplazamientoProbable` | `frecuencias("hvwh")` | Pendiente | Solicita el conteo de letras. |
| 3 | `frecuencias` | `aux(['h','v','w','h'], {})` | `List(('h',2), ('v',1), ('w',1))` | Devuelve la lista ordenada por repetición. |
| 4 | `desplazamientoProbable` | Ninguna | `k = 3` | Extrae la 'h', calcula la distancia con la 'e' y devuelve 3. |
| 5 | `romperCesar` | `cesarCola("hvwh", -3)` | Pendiente | Llama a la función de descifrado con la clave invertida. |
| 6 | `cesarCola` | `desplazar` (interna) | `"este"` | Descifra el mensaje y lo devuelve a `romperCesar`. |

### Diagrama de secuencia de llamados

Este diagrama ilustra cómo `romperCesar` delega las tareas a las demás funciones sin generar recursión propia, manteniendo un flujo de ejecución secuencial.

```mermaid
sequenceDiagram
    participant RC as romperCesar(hvwh)
    participant DP as desplazamientoProbable(hvwh)
    participant F as frecuencias(hvwh)
    participant CC as cesarCola(hvwh, -3)

    RC->>DP: solicita k probable
    DP->>F: frecuencias(hvwh)
    F-->>DP: devuelve List((h,2), (v,1), (w,1))
    DP-->>RC: calcula y devuelve k = 3
    RC->>CC: llama con k invertido (-3)
    CC-->>RC: devuelve "este"
```

## Punto 5: Cifrado Vigenère

### 1. Estado de la Pila de Llamados Paso a Paso

La función `vigenere` utiliza una función auxiliar recursiva de cola llamada `vigenereHelper` para procesar el mensaje sin dejar operaciones pendientes.

Para la ejecución de `vigenere("casa", "sol")`, con los desplazamientos de la clave `sol` ($s = 18$, $o = 14$, $l = 11$):

```text
vigenereHelper("casa", "sol", "")
└── vigenereHelper("asa", "ols", "s")       // 'c' + 18 mod 26 -> 's'
    └── vigenereHelper("sa", "lso", "sh")   // 'a' + 14 mod 26 -> 'h'
        └── vigenereHelper("a", "sol", "shl") // 's' + 11 mod 26 -> 'l'
            └── vigenereHelper("", "ols", "shls") // 'a' + 18 mod 26 -> 's'
                └── "shls" [Caso base alcanzado]
```

### 2. Diagrama de Secuencia de la Pila (Mermaid)

```mermaid
sequenceDiagram
  autonumber
  actor Usuario
  participant V as vigenere("casa", "sol")
  participant H as vigenereHelper

  Usuario->>V: Iniciar cifrado
  V->>H: vigenereHelper("casa", "sol", "")
  Note over H: 'c' + 's' -> 's' | Clave rota a "ols"
  H->>H: vigenereHelper("asa", "ols", "s")
  Note over H: 'a' + 'o' -> 'h' | Clave rota a "lso"
  H->>H: vigenereHelper("sa", "lso", "sh")
  Note over H: 's' + 'l' -> 'l' | Clave rota a "sol"
  H->>H: vigenereHelper("a", "sol", "shl")
  Note over H: 'a' + 's' -> 's' | Clave rota a "ols"
  H->>H: vigenereHelper("", "ols", "shls")
  Note over H: Caso base alcanzado (mensaje vacío)
  H-->>Usuario: Retorna "shls"
```

### 3. Explicación de la Complejidad Espacial y Pila

- **Espacio Constante $O(1)$:** Al estar anotada con `@tailrec`, la llamada recursiva a `vigenereHelper` es el último paso de la función. El compilador de Scala reutiliza un único marco en la pila de llamadas (*stack frame*).
- **Manejo de Caracteres Especiales:** Los espacios, números o signos no consumen letras de la clave `sol` ni desplazan la secuencia de la clave; pasan directo al acumulador.