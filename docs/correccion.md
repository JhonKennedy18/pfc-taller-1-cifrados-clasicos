# Informe de corrección

## Puntos 1 y 2: cifrado César

### Convenciones

* Un `Mensaje` es un conjunto definido recursivamente: o es la cadena vacía
  $\varepsilon$, o es un carácter $c$ seguido de una cadena $m'$, que se escribe
  $c \cdot m'$. En el código, $c$ es `m.head` y $m'$ es `m.tail`.
* El punto $\cdot$ denota la concatenación de cadenas. Es asociativa y
  $\varepsilon$ es su elemento neutro.
* $x \bmod 26$ es el módulo matemático: siempre está en $\{0, \ldots, 25\}$.
* Se supone que $k$ está lejos de los extremos de `Int`, de modo que
  `c - primera + k` no se desborda.

### Especificación

Para un carácter $c$ y un entero $k$, sea $pos(c) = c - \texttt{'a'}$ la
posición de una minúscula. El cifrado de un solo carácter es

```math
\mathrm{cif}_k(c) =
\begin{cases}
\texttt{'a'} + \big((pos(c) + k) \bmod 26\big) & \text{si } c \in [\texttt{'a'}, \texttt{'z'}] \\
c & \text{en otro caso}
\end{cases}
```

y la función que debe calcular el taller, $f : \text{Mensaje} \times \mathbb{Z} \to \text{Mensaje}$, es

```math
f(\varepsilon, k) = \varepsilon \qquad\qquad f(c \cdot m', k) = \mathrm{cif}_k(c) \cdot f(m', k)
```

Es decir, cada letra minúscula se corre $k$ posiciones y todo lo demás se copia
sin cambio, que es lo que pide el enunciado.

### Dos lemas sobre la función auxiliar `desplazar`

Las dos funciones usan esta misma auxiliar:

```Scala
def desplazar(c: Char): Char =
  if (esMinuscula(c)) (primera + ((c - primera + k) % letras + letras) % letras).toChar
  else c
```

**Lema 1.** Para todo $x \in \mathbb{Z}$, `((x % 26) + 26) % 26` $= x \bmod 26$.

*Demostración.* En Scala, $r = x \,\%\, 26$ cumple $|r| < 26$ y $r \equiv x \pmod{26}$.
Luego $r + 26 \in (0, 52)$ es positivo, así que `(r + 26) % 26` está en
$\{0, \ldots, 25\}$. Además $r + 26 \equiv x \pmod{26}$. Un valor en
$\{0, \ldots, 25\}$ congruente con $x$ módulo 26 es $x \bmod 26$. $\blacksquare$

**Lema 2.** Para todo carácter $c$ y todo $k \in \mathbb{Z}$,
`desplazar(c)` $= \mathrm{cif}_k(c)$.

*Demostración.* Dos casos:

* Si `esMinuscula(c)` es falso, `desplazar(c)` devuelve $c$, y por definición
  $\mathrm{cif}_k(c) = c$.
* Si `esMinuscula(c)` es verdadero, entonces $pos(c) = $ `c - primera`. Por el
  Lema 1, `((c - primera + k) % letras + letras) % letras`
  $= (pos(c) + k) \bmod 26$. Entonces `desplazar(c)` es la letra de código
  $\texttt{'a'} + (pos(c) + k) \bmod 26$, que es $\mathrm{cif}_k(c)$. $\blacksquare$

## Punto 1: corrección de `cesar` (recursión lineal)

```Scala
def cesar(m: Mensaje, k: Int): Mensaje = {
  def desplazar(c: Char): Char = ...
  if (m.isEmpty) ""
  else desplazar(m.head).toString + cesar(m.tail, k)
}
```

Como `Mensaje` es un conjunto definido recursivamente, usamos inducción
estructural. Vamos a demostrar el siguiente teorema.

```math
\forall k \in \mathbb{Z},\ \forall m \in \text{Mensaje} : \texttt{cesar}(m, k) == f(m, k)
```

Fijamos un $k \in \mathbb{Z}$ cualquiera.

**Caso base:** $m = \varepsilon$

```math
\texttt{cesar}(\varepsilon, k) \rightarrow \text{if } (\varepsilon.\text{isEmpty})\ \varepsilon \text{ else } \ldots \rightarrow \varepsilon
```

Por otro lado, $f(\varepsilon, k) = \varepsilon$. Entonces
$\texttt{cesar}(\varepsilon, k) == f(\varepsilon, k)$.

**Caso de inducción:** $m = c \cdot m'$. Hay que demostrar:
$\texttt{cesar}(m', k) == f(m', k) \rightarrow \texttt{cesar}(c \cdot m', k) == f(c \cdot m', k)$

Empecemos por calcular qué devuelve `cesar` usando el modelo de sustitución.
Como $m$ no es vacía:

```math
\texttt{cesar}(c \cdot m', k) \rightarrow \texttt{desplazar}(c) \cdot \texttt{cesar}(m', k)
```

Usando el Lema 2 y la hipótesis de inducción (HI):

```math
\rightarrow \mathrm{cif}_k(c) \cdot f(m', k) = f(c \cdot m', k)
```

La última igualdad es la definición de $f$. Por lo tanto,
$\texttt{cesar}(c \cdot m', k) == f(c \cdot m', k)$.

Concluimos por inducción que
$\forall m \in \text{Mensaje} : \texttt{cesar}(m, k) == f(m, k)$. Como $k$ era
arbitrario, el teorema vale para todo $k \in \mathbb{Z}$. $\blacksquare$

## Punto 2: corrección de `cesarCola` (recursión de cola)

```Scala
@tailrec
final def cesarCola(m: Mensaje, k: Int, acc: Mensaje = ""): Mensaje = {
  def desplazar(c: Char): Char = ...
  if (m.isEmpty) acc
  else cesarCola(m.tail, k, acc + desplazar(m.head))
}
```

Esta función es un proceso iterativo. Fijamos $k \in \mathbb{Z}$, un mensaje
inicial $m_0$ y un acumulador inicial $acc_0$ (el valor por defecto es
$acc_0 = \varepsilon$). Formalizamos la iteración:

* Un estado es $s = (m, acc)$.
* El estado inicial es $s_0 = (m_0, acc_0)$.
* Un estado es final si $m = \varepsilon$. La respuesta es
  $\text{respuesta}((m, acc)) = acc$.
* La transformación, para $m = c \cdot m'$, es
  $\text{transformar}((c \cdot m', acc)) = (m', acc \cdot \mathrm{cif}_k(c))$.
  Por el Lema 2, es exactamente lo que hace el código:
  `cesarCola(m.tail, k, acc + desplazar(m.head))`.
* La invariante de ciclo es

```math
\text{Inv}(m, acc) \equiv acc \cdot f(m, k) = acc_0 \cdot f(m_0, k)
```

Lo que ya cifré más lo que falta por cifrar siempre da el resultado total.

Demostramos los puntos del método:

**1.** $\text{Inv}(s_0)$: el estado inicial cumple la condición invariante.

```math
s_0 = (m_0, acc_0) \implies acc_0 \cdot f(m_0, k) = acc_0 \cdot f(m_0, k)
```

**2.** $(s_i \neq s_f \land \text{Inv}(s_i)) \rightarrow \text{Inv}(\text{transformar}(s_i))$

Sea $s_i = (c \cdot m', acc)$ con $\text{Inv}(s_i)$, es decir,
$acc \cdot f(c \cdot m', k) = acc_0 \cdot f(m_0, k)$. El nuevo estado es
$(m', acc')$ con $acc' = acc \cdot \mathrm{cif}_k(c)$. Entonces:

```math
acc' \cdot f(m', k) = acc \cdot \mathrm{cif}_k(c) \cdot f(m', k) = acc \cdot f(c \cdot m', k) = acc_0 \cdot f(m_0, k)
```

La segunda igualdad es la definición de $f$, y la tercera es $\text{Inv}(s_i)$.
Por lo tanto $\text{Inv}(\text{transformar}(s_i))$.

**3.** $\text{Inv}(s_f) \rightarrow \text{respuesta}(s_f) == acc_0 \cdot f(m_0, k)$

```math
\text{Inv}((\varepsilon, acc)) \rightarrow acc \cdot f(\varepsilon, k) = acc_0 \cdot f(m_0, k) \rightarrow acc = acc_0 \cdot f(m_0, k)
```

porque $f(\varepsilon, k) = \varepsilon$ es el neutro de la concatenación.

**4.** En cada paso, la longitud de $m$ disminuye en 1, porque se pasa de $m$ a
`m.tail`. Como la longitud es un natural, después de $|m_0|$ iteraciones se
llega a $m = \varepsilon$, que es el estado final.

Esto implica que $\texttt{cesarCola}(m_0, k, acc_0) == acc_0 \cdot f(m_0, k)$.
Con el acumulador por defecto $acc_0 = \varepsilon$:

```math
\texttt{cesarCola}(m, k) == f(m, k) == \texttt{cesar}(m, k)
```

para todo $m$ y todo $k$, que es lo que pide el enunciado: la misma función que
el Punto 1, pero como proceso iterativo. $\blacksquare$

## Cómo se encadenan los llamados en la ejecución

**`cesar("casa", 3)`.** Cada llamada deja una suma pendiente. Los paréntesis
muestran las operaciones que esperan, y se resuelven de adentro hacia afuera
cuando el caso base devuelve la cadena vacía:

```
cesar("casa", 3)
→ "f" + cesar("asa", 3)
→ "f" + ("d" + cesar("sa", 3))
→ "f" + ("d" + ("v" + cesar("a", 3)))
→ "f" + ("d" + ("v" + ("d" + cesar("", 3))))
→ "f" + ("d" + ("v" + ("d" + "")))
→ "f" + ("d" + ("v" + "d"))
→ "f" + ("d" + "vd")
→ "f" + "dvd"
→ "fdvd"
```

Esto corresponde a la hipótesis de inducción: cada `cesar(m', k)` interno se
reemplaza por $f(m', k)$ y la suma se resuelve una capa a la vez.

**`cesarCola("casa", 3)`.** Cada llamada reemplaza a la anterior y no deja
nada pendiente. El resultado parcial viaja en `acc`, y cada fila cumple la
invariante $acc \cdot f(m, k) = f(\texttt{"casa"}, 3)$:

```
cesarCola("casa", 3, "")
→ cesarCola("asa", 3, "f")
→ cesarCola("sa", 3, "fd")
→ cesarCola("a", 3, "fdv")
→ cesarCola("", 3, "fdvd")
→ "fdvd"
```

Por ejemplo, en la tercera fila: $acc = \texttt{"fd"}$ y
$f(\texttt{"sa"}, 3) = \texttt{"vd"}$, y $\texttt{"fd"} \cdot \texttt{"vd"} = \texttt{"fdvd"}$.

### Puntos 3 y 4: Conteo de Frecuencias y Romper César

### Convenciones

* Un `Mensaje` $m$ es una secuencia de caracteres $c_1, c_2, \dots, c_n$.
* La función `esMinuscula(c)` devuelve verdadero si el carácter $c$ pertenece al alfabeto $A = \{a, b, \dots, z\}$.
* La función $\text{frec}(c, m)$ representa la cantidad de veces que el carácter $c$ aparece en el mensaje $m$.

### Especificación del Punto 3 (`frecuencias`)

La función que se debe calcular es $F: \text{Mensaje} \to \text{Frecuencias}$, donde $\text{Frecuencias}$ es una lista ordenada de pares $(c, n)$ tal que:
1. $c \in A$ (solo se consideran letras minúsculas).
2. $n = \text{frec}(c, m) > 0$ (solo se incluyen letras que aparecen al menos una vez).
3. La lista está ordenada bajo la relación $\succ$ (Va antes que), donde $(c_1, n_1) \succ (c_2, n_2)$ si $n_1 > n_2$, o si $n_1 = n_2 \land c_1 < c_2$ (orden alfabético).

### Lema 1: Invariante del conteo iterativo

La función `frecuencias` delega el cálculo a una función auxiliar `aux(cola, acc)`. Formalizamos el proceso:

* Un estado es $s = (L, A)$, donde $L$ es la lista de caracteres restantes y $A$ es el mapa de frecuencias acumulado.
* El estado inicial es $s_0 = (m\text{.toList}, \emptyset)$.
* El estado es final cuando $L = \text{Nil}$. La respuesta es $A$.
* La invariante de ciclo establece que la frecuencia acumulada en $A$ sumada a las frecuencias de los caracteres en $L$ es igual a la frecuencia total en el mensaje original $m$:

$$\text{Inv}(L, A) \equiv \forall c \in A, \quad A(c) + \text{frec}(c, L) = \text{frec}(c, m)$$

*Demostración.*

**1. Caso inicial:** En $s_0 = (m\text{.toList}, \emptyset)$, el mapa $A$ está vacío ($A(c) = 0$). Como $L = m$, se cumple que $0 + \text{frec}(c, m) = \text{frec}(c, m)$. Por lo tanto, $\text{Inv}(s_0)$ es verdadera.
**2. Paso inductivo:** Sea un estado intermedio $s_i = (x \cdot L', A)$ que cumple $\text{Inv}(s_i)$.
* Si $esMinuscula(x)$ es falso: el nuevo estado es $(L', A)$. Como $x \notin A$, $\text{frec}(c, L') = \text{frec}(c, x \cdot L')$ para todo $c \in A$, conservando la igualdad.
* Si $esMinuscula(x)$ es verdadero: el nuevo estado es $(L', A')$, donde $A' = A + (x \to A(x) + 1)$. Para la letra $x$, la lista pierde una aparición ($\text{frec}(x, L') = \text{frec}(x, x \cdot L') - 1$), pero el mapa suma 1 ($A'(x) = A(x) + 1$). La suma se mantiene constante. Para cualquier otra letra $c \neq x$, los valores no varían.
  Por lo tanto, $\text{Inv}(\text{transformar}(s_i))$ se cumple.
  **3. Estado final:** En $s_f = (\text{Nil}, A)$, como la lista está vacía, $\text{frec}(c, \text{Nil}) = 0$. La invariante se reduce a:

$$A(c) + 0 = \text{frec}(c, m) \implies A(c) = \text{frec}(c, m)$$

El mapa final contiene las frecuencias exactas de cada letra en $m$.

**4. Terminación:** En cada iteración, la longitud de la lista $L$ se reduce exactamente en un elemento mediante $L\text{.tail}$. Al ser una lista finita, el algoritmo alcanza $L = \text{Nil}$ en $\vert{}m\vert{}$ pasos. $\blacksquare$

### Especificación del Punto 4 (`desplazamientoProbable` y `romperCesar`)

La función $desplazamientoProbable: \text{Mensaje} \to \mathbb{Z}$ estima la clave de cifrado $k$ asumiendo que la letra más frecuente en el texto en claro original fue la letra $\texttt{'e'}$ ($pos(\texttt{'e'}) = 4$).

Formalmente:
```math
desplazamientoProbable(m) =
\begin{cases}
0 & \text{si } frecuencias(m) = \text{Nil} \\
(pos(c_{max}) - pos(\texttt{'e'}) + 26) \bmod 26 & \text{si } frecuencias(m) \neq \text{Nil}
\end{cases}
```

## Punto 5: Demostración matemática del conteo de mensajes $C(n, a)$

Vamos a demostrar la validez de la función de recurrencia con la que se calcula
la cantidad de mensajes distintos de longitud $n$ sobre un alfabeto de $a$
letras, sin que contengan dos caracteres idénticos de manera consecutiva:

```math
C(n, a) =
\begin{cases}
1 & \text{si } n = 0 \\
a & \text{si } n = 1 \\
(a - 1) \cdot C(n - 1, a) & \text{si } n > 1
\end{cases}
```

### Demostración por inducción matemática

#### 1. Casos base

* **Para $n = 0$:** solo existe un mensaje de longitud $0$, que es la cadena
  vacía $\varepsilon$. Por lo tanto, $C(0, a) = 1$.
* **Para $n = 1$:** en un mensaje de un solo símbolo se puede elegir cualquiera
  de las $a$ letras del alfabeto. Por lo tanto, $C(1, a) = a$.

#### 2. Hipótesis inductiva

Asumimos que para una longitud $k \geq 1$, la cantidad de mensajes válidos sin
letras iguales seguidas está dada por $C(k, a)$.

#### 3. Paso inductivo

Queremos determinar el número de mensajes válidos de longitud $k + 1$.

Cualquier mensaje válido de longitud $k + 1$ se puede formar tomando un mensaje
válido de longitud $k$ y anexándole un nuevo carácter al final.

Dado que la regla exige que no existan dos letras iguales seguidas, el último
carácter (en la posición $k + 1$) **no puede ser igual** al carácter de la
posición $k$. Como el alfabeto tiene $a$ letras en total y hay $1$ letra
prohibida (la última letra del mensaje de longitud $k$), quedan exactamente
$(a - 1)$ opciones válidas para la nueva letra.

Por el **principio multiplicativo del conteo**:

```math
C(k + 1, a) = (a - 1) \cdot C(k, a)
```

Esto confirma la relación de recurrencia para todo $n > 1$.

#### 4. Formulación en forma cerrada

Resolviendo la relación de recurrencia para $n \geq 1$:

```math
C(n, a) = a \cdot (a - 1)^{n - 1}
```

Por ejemplo, para $n = 3$ y $a = 26$:
$C(3, 26) = 26 \cdot 25^{2} = 16250$, que coincide con el ejemplo del enunciado.
